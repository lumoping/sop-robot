package com.majun.soprobot.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.majun.soprobot.lark.api.LarkApi;
import com.majun.soprobot.lark.api.LarkException;
import com.majun.soprobot.lark.card.CardGenerator;
import com.majun.soprobot.repo.ChatInfoRepo;
import com.majun.soprobot.repo.SopRepo;
import com.majun.soprobot.repo.po.ChatInfo;
import com.majun.soprobot.repo.po.Sop;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.majun.soprobot.lark.api.LarkApi.*;

@Component
public class LarkMessageConsumer {

    private final LarkApi larkApi;

    private final ChatInfoRepo chatInfoRepo;

    private final SopRepo sopRepo;

    private final CardGenerator cardGenerator;


    private final Mono<TenantAccess> tenantAccess;

    private final Mono<String> rootFolderToken;

    private final Mono<Tuple2<String, String>> tokens;

    public LarkMessageConsumer(@Value("${lark.appId}") String appId,
                               @Value("${lark.appSecret}") String appSecret,
                               LarkApi larkApi,
                               ChatInfoRepo chatInfoRepo,
                               SopRepo sopRepo,
                               CardGenerator cardGenerator) {
        this.larkApi = larkApi;
        tenantAccess = this.larkApi.tenantAccessToken(appId, appSecret)
                .cache(access -> Duration.ofSeconds(access.expire()),
                        throwable -> Duration.ZERO,
                        () -> Duration.ZERO);
        rootFolderToken = tenantAccess.map(TenantAccess::tenant_access_token)
                .flatMap(this.larkApi::rootFolderToken)
                .cache();
        tokens = Mono.defer(() -> Mono.zip(tenantAccess.map(TenantAccess::tenant_access_token), rootFolderToken));
        this.chatInfoRepo = chatInfoRepo;
        this.sopRepo = sopRepo;
        this.cardGenerator = cardGenerator;
    }


    @KafkaListener(topics = "ROBOT_ENTER_CHAT")
    void robotEnterChat(JsonNode message) {
        var chatId = message.get("event").get("chat_id").asText();
        var existsChatInfo = chatInfoRepo.findChatInfoByChatId(chatId);
        var createFolderAndSave = tokens.flatMap(it -> larkApi.createFolder(it.getT1(), it.getT2(), chatId))
                .flatMap(folder -> Mono.just(new ChatInfo(null, chatId, folder.token(), folder.url())).flatMap(chatInfoRepo::save));
        Function<ChatInfo, String> generateCard = (var chatInfo) -> {
            try {
                return cardGenerator.helloCard(new CardGenerator.HelloCardValues(chatId, chatInfo.folderToken()));
            } catch (IOException | TemplateException e) {
                throw new LarkException(e);
            }
        };
        Function<String, Mono<Void>> sendMessage = (var content) -> tokens.flatMap(it -> larkApi.sendMessage(it.getT1(), "chat_id", new SendMessageReq(chatId, content, "interactive")));
        existsChatInfo.switchIfEmpty(createFolderAndSave)
                .map(generateCard)
                .onErrorStop()
                .flatMap(sendMessage)
                .subscribe();
    }

    @KafkaListener(topics = "CREATE_FILE")
    void createFile(JsonNode message) {
        var openId = message.get("open_id").asText();
        var chatId = message.get("action").get("value").get("chat_id").asText();
        var folderToken = message.get("action").get("value").get("folder_token").asText();
        var createFile = tenantAccess.map(TenantAccess::tenant_access_token)
                .flatMap(token -> larkApi.createFile(token, folderToken));
        BiFunction<String, String, Mono<String>> getFileUrl = (var fileToken, var fileType) -> tenantAccess.flatMap(it -> larkApi.fileMeta(it.tenant_access_token(), fileToken, fileType))
                .map(FileMetaResp.Meta::url);
        Function<Sop, Mono<Sop>> save = sopRepo::save;
        BiFunction<String, String, Mono<Void>> subscribeFile = (var fileToken, var fileType) -> tenantAccess.flatMap(it -> larkApi.subscribeFile(it.tenant_access_token(), fileToken, fileType));
        BiFunction<String, PermissionMemberCreateReq, Mono<Void>> createPermission = (var fileToken, var memberCreateReq) ->
                tenantAccess.flatMap(it -> larkApi.createPermissionMember(it.tenant_access_token(), fileToken, "docx", memberCreateReq))
                        .then(Mono.empty());
        Function<String, Mono<String>> getRootBlock = (var fileToken) -> tenantAccess.flatMap(it -> larkApi.getAllBlock(it.tenant_access_token(), fileToken))
                .map(it -> it.items().get(0).block_id());

        BiFunction<String, String, Mono<String>> createHighlightBlock = (var fileToken, var rootBlock) -> Mono.just(Item.callout(19, new Callout(2, 2, null, "pushpin")))
                .flatMap(it -> tenantAccess.flatMap(access -> larkApi.createBlock(access.tenant_access_token(), fileToken, rootBlock, Collections.singletonList(it))))
                .map(it -> it.children().get(0).block_id())
                .flatMap(highlightId ->
                        Mono.just(Item.text(2, new Item.Block(null, Collections.singletonList(new Element(new TextRun("请在此处填写描述"))))))
                                .flatMap(it -> tenantAccess.flatMap(access -> larkApi.createBlock(access.tenant_access_token(), fileToken, highlightId, Collections.singletonList(it))))
                                .map(it -> it.children().get(0).block_id()));


        Function<String, Mono<Void>> sendMessage = (var docUrl) ->
                tenantAccess.flatMap(it -> larkApi.sendMessage(it.tenant_access_token(), "open_id", new SendMessageReq(openId, "{\"text\": \"" + "已创建SOP文档，开始编辑吧✍️ ： " + docUrl + "\"}", "text")));

        createFile
                .doOnNext(it -> getRootBlock.apply(it.documentId()).flatMap(rootBlock -> createHighlightBlock.apply(it.documentId(), rootBlock)).subscribe())
                .doOnNext(it -> subscribeFile.apply(it.documentId(), "docx").subscribe())
                .doOnNext(it -> createPermission.apply(it.documentId(), new PermissionMemberCreateReq("openchat", chatId, "full_access")).subscribe())
                .flatMap(it -> getFileUrl.apply(it.documentId(), "docx")
                        .doOnNext(url -> save.apply(new Sop(null, it.documentId(), url, it.title(), "暂无")))
                        .flatMap(sendMessage))
                .subscribe();
    }


    @KafkaListener(topics = "FILE_TITLE_UPDATE")
    void fileTitleUpdate(JsonNode message) {
        var fileToken = message.get("event").get("file_token").asText();
        tenantAccess.flatMap(it -> larkApi.fileMeta(it.tenant_access_token(), fileToken, "docx"))
                .map(FileMetaResp.Meta::title)
                .flatMap(it -> sopRepo.updateTitleByDocToken(it, fileToken))
                .subscribe();
    }

    @KafkaListener(topics = "FILE_EDIT")
    void fileEdit(JsonNode message) {
        //       var fileToken = message.get("event").get("file_token").asText();
//        tenantAccess.flatMap(it -> larkApi.getAllBlock())
    }

//    @KafkaListener(topics = "MessageReceive")
//    void messageReceive(JsonNode message) {
//    }
//
//
//    @KafkaListener(topics = "FileTitleUpdate")
//    Mono<Void> fileTitleUpdate(JsonNode message) {
//
//    }
//
//    @KafkaListener(topics = "FileEdit")
//    Mono<Void> fileEdit(JsonNode message) {
//
//    }
//
//    @KafkaListener(topics = "FileTrashed")
//    Mono<Void> fileTrashed(JsonNode message) {
//
//    }
//
//    @KafkaListener(topics = "FileDelete")
//    Mono<Void> fileDelete(JsonNode message) {
//
//    }

}
