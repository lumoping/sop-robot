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
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
public class LarkMessageConsumer {


    private final String baseUrl;

    private final LarkApi larkApi;

    private final ChatInfoRepo chatInfoRepo;

    private final SopRepo sopRepo;

    private final CardGenerator cardGenerator;


    private final Mono<LarkApi.TenantAccess> tenantAccess;

    private final Mono<String> rootFolderToken;

    private final Mono<Tuple2<String, String>> tokens;

    public LarkMessageConsumer(@Value("${lark.baseUrl}") String baseUrl,
                               @Value("${lark.appId}") String appId,
                               @Value("${lark.appSecret}") String appSecret,
                               LarkApi larkApi,
                               ChatInfoRepo chatInfoRepo,
                               SopRepo sopRepo,
                               CardGenerator cardGenerator) {
        this.baseUrl = baseUrl;
        this.larkApi = larkApi;
        tenantAccess = this.larkApi.tenantAccessToken(appId, appSecret)
                .cache(access -> Duration.ofSeconds(access.expire()),
                        throwable -> Duration.ZERO,
                        () -> Duration.ZERO);
        rootFolderToken = tenantAccess.map(LarkApi.TenantAccess::tenant_access_token)
                .flatMap(this.larkApi::rootFolderToken)
                .cache();
        tokens = Mono.defer(() -> Mono.zip(tenantAccess.map(LarkApi.TenantAccess::tenant_access_token), rootFolderToken));
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
        Function<ChatInfo, String> generateCard = (ChatInfo chatInfo) -> {
            try {
                return cardGenerator.helloCard(new CardGenerator.HelloCardValues(chatId, chatInfo.folderToken()));
            } catch (IOException | TemplateException e) {
                throw new LarkException(e);
            }
        };
        Function<String, Mono<Void>> sendMessage = (String content) -> tokens.flatMap(it -> larkApi.sendMessage(it.getT1(), "chat_id", new LarkApi.SendMessageReq(chatId, content, "interactive")));
        existsChatInfo.switchIfEmpty(createFolderAndSave).map(generateCard).onErrorStop().flatMap(sendMessage).subscribe();
    }

    @KafkaListener(topics = "CREATE_FILE")
    void createFile(JsonNode message) {
        var openId = message.get("open_id").asText();
        var chatId = message.get("action").get("value").get("chat_id").asText();
        var folderToken = message.get("action").get("value").get("folder_token").asText();
        Mono<Sop> createFileAndSave = tenantAccess.map(LarkApi.TenantAccess::tenant_access_token)
                .flatMap(token -> larkApi.createFile(token, folderToken))
                .flatMap(it -> sopRepo.save(new Sop(null, it.documentId(), it.title(), "暂无")));
        BiFunction<String, LarkApi.PermissionMemberCreateReq, Mono<Void>> createPermission = (String fileToken, LarkApi.PermissionMemberCreateReq memberCreateReq) ->
                tenantAccess.flatMap(it -> larkApi.createPermissionMember(it.tenant_access_token(), fileToken, "docx", memberCreateReq))
                        .then(Mono.empty());
        Function<String, Mono<Void>> sendMessage = (String docUrl) ->
                tenantAccess.flatMap(it -> larkApi.sendMessage(it.tenant_access_token(), "open_id", new LarkApi.SendMessageReq(openId, "{\"text\": \"" + "已创建SOP文档，开始编辑吧✍️ ： " + docUrl + "\"}", "text")));
        createFileAndSave
                .flatMap(it ->
                        createPermission.apply(it.docToken(), new LarkApi.PermissionMemberCreateReq("openchat", chatId, "full_access"))
                                .then(sendMessage.apply(baseUrl + "/docx/" + it.docToken())))
                .subscribe();
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
