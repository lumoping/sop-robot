package com.majun.soprobot.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.majun.soprobot.lark.api.LarkApi;
import com.majun.soprobot.lark.api.LarkException;
import com.majun.soprobot.lark.card.CardGenerator;
import com.majun.soprobot.repo.ChatInfoRepo;
import com.majun.soprobot.repo.SopRepo;
import com.majun.soprobot.repo.SopTodoRepo;
import com.majun.soprobot.repo.po.ChatInfo;
import com.majun.soprobot.repo.po.Sop;
import com.majun.soprobot.repo.po.SopTodo;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.time.Duration;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.majun.soprobot.lark.api.LarkApi.*;

@Component
public class LarkMessageConsumer {

    private final LarkApi larkApi;

    private final ChatInfoRepo chatInfoRepo;

    private final SopRepo sopRepo;

    private final SopTodoRepo sopTodoRepo;

    private final CardGenerator cardGenerator;


    private final Mono<TenantAccess> tenantAccess;

    private final Mono<String> rootFolderToken;

    private final Mono<Tuple2<String, String>> tokens;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LarkMessageConsumer(@Value("${lark.appId}") String appId,
                               @Value("${lark.appSecret}") String appSecret,
                               LarkApi larkApi,
                               ChatInfoRepo chatInfoRepo,
                               SopRepo sopRepo,
                               CardGenerator cardGenerator, SopTodoRepo sopTodoRepo) {
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
        this.sopTodoRepo = sopTodoRepo;
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
        var chatId = message.get("action").get("value").get("chatId").asText();
        var createFile = tenantAccess.map(TenantAccess::tenant_access_token)
                .flatMap(token -> chatInfoRepo.findChatInfoByChatId(chatId).flatMap(it -> larkApi.createFile(token, it.folderToken())));
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
                        .doOnNext(url -> save.apply(new Sop(null, chatId, it.documentId(), url, it.title(), "暂无")).subscribe())
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
        var fileToken = message.get("event").get("file_token").asText();
        var itemFlux = tenantAccess.flatMapMany(it -> larkApi.getAllBlock(it.tenant_access_token(), fileToken).flatMapIterable(BlockGetAllResp::items));
        var desc = itemFlux.filter(item -> item.block_type() == 19)
                .map(Item::children)
                .flatMap(children -> itemFlux.filter(item -> children.contains(item.block_id()) && item.block_type() == 2))
                .map(item -> item.text().elements().stream().map(Element::text_run).map(TextRun::content).reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append))
                .reduce(new StringBuilder(), (sb1, sb2) -> sb1.append("\n").append(sb2))
                .map(StringBuilder::toString)
                .map(String::trim);

        Mono<Sop> findSop = sopRepo.findSopByDocToken(fileToken);

        Function<Sop, Flux<SopTodo>> findTodos = (Sop sop) -> itemFlux.filter(item -> item.block_type() == 17)
                .map(item -> item.todo().elements().stream().filter(it -> it.text_run() != null).findFirst().map(Element::text_run).map(TextRun::content).orElse(""))
                .map(it -> new SopTodo(null, sop.id(), fileToken, it));
        var todos = findSop.flatMapMany(findTodos);

        desc.flatMap(it -> sopRepo.updateDescriptionByDocToken(it, fileToken))
                .then(sopTodoRepo.deleteSopTodosByDocToken(fileToken))
                .thenMany(sopTodoRepo.saveAll(todos))
                .subscribe();
    }

    @KafkaListener(topics = "MESSAGE_RECEIVE")
    void messageReceive(JsonNode message) throws JsonProcessingException {
        var chatId = message.get("event").get("message").get("chat_id").asText();
        var originalContent = message.get("event").get("message").get("content").asText();
        var originalText = objectMapper.readTree(originalContent).get("text").asText();
        var text = originalText.substring(originalText.lastIndexOf("@") + 8).trim();
        Function<List<Sop>, String> generateCard = (List<Sop> sops) -> {
            try {
                return cardGenerator.searchPageCard(new CardGenerator.SearchPageCardValues(chatId, sops, false, text));
            } catch (IOException | TemplateException e) {
                throw new LarkException(e);
            }
        };
        Function<String, Mono<Void>> sendPersonalMessage = (String card) -> tenantAccess.flatMap(it -> larkApi.sendMessage(it.tenant_access_token(), "chat_id", new SendMessageReq(chatId, card, "interactive")));
        sopRepo.findSopsByChatIdAndTitleLike(chatId, "%" + text + "%")
                .switchIfEmpty(sopRepo.findSopsByChatIdAndDescriptionLike(chatId, "%" + text + "%"))
                .collectList()
                .map(generateCard)
                .flatMap(sendPersonalMessage)
                .subscribe();
    }

    @KafkaListener(topics = "SEARCH_ALL")
    void searchAll(JsonNode message) {
        var openId = message.get("open_id").asText();
        var chatId = message.get("action").get("value").get("chatId").asText();
        Function<List<Sop>, JsonNode> generateCard = (List<Sop> sops) -> {
            try {
                return objectMapper.readTree(cardGenerator.searchPageCard(new CardGenerator.SearchPageCardValues(chatId, sops, true, null)));
            } catch (IOException | TemplateException e) {
                throw new LarkException(e);
            }
        };
        Function<JsonNode, Mono<Void>> sendPersonalMessage = (JsonNode card) -> tenantAccess.flatMap(it -> larkApi.sendPersonalMessage(it.tenant_access_token(), new SendPersonalMessageReq(chatId, openId, "interactive", card)));

        sopRepo.findSopsByChatId(chatId)
                .collectList()
                .map(generateCard)
                .flatMap(sendPersonalMessage)
                .subscribe();
    }

    @KafkaListener(topics = "DETAIL")
    void detail(JsonNode message) {
        var openId = message.get("open_id").asText();
        var sopId = message.get("action").get("value").get("sopId").asInt();
        var chatId = message.get("action").get("value").get("chatId").asText();
        var sop = sopRepo.findById(sopId);
        var todos = sop.flatMapMany(it -> sopTodoRepo.findSopTodosByDocToken(it.docToken())).collectList();
        var generateCard = Mono.zip(sop, todos)
                .map(it -> {
                    try {
                        return objectMapper.readTree(cardGenerator.detailCard(new CardGenerator.DetailCardValues(it.getT1(), it.getT2())));
                    } catch (IOException | TemplateException e) {
                        throw new LarkException(e);
                    }
                });
        Function<JsonNode, Mono<Void>> sendPersonalMessage = (JsonNode card) -> tenantAccess.flatMap(it -> larkApi.sendPersonalMessage(it.tenant_access_token(), new SendPersonalMessageReq(chatId, openId, "interactive", card)));
        generateCard.flatMap(sendPersonalMessage).subscribe();
    }

    @KafkaListener(topics = "START_TODO")
    void startTodo(JsonNode message) {
        var openId = message.get("open_id").asText();
        var sopId = message.get("action").get("value").get("sopId").asInt();
        var findSop = sopRepo.findById(sopId);
        var findTodos = sopTodoRepo.findSopTodosBySopId(sopId);
        AtomicInteger index = new AtomicInteger(1);
        BiFunction<Sop, SopTodo, TaskCreateReq> buildTaskCreateReq = (Sop sop, SopTodo todo) ->
        {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, index.getAndIncrement() * 15);
            return TaskCreateReq.simple(
                    todo.description(),
                    new Due(String.valueOf((calendar.getTimeInMillis() / 1000)), null, false),
                    new Origin("""
                            "{"zh_cn": "SOP", "en_us": "SOP"}"
                            """, new Origin.Href(sop.docUrl(), sop.title())),
                    true,
                    Collections.singletonList(openId));
        };
        findSop.flatMapMany(sop -> findTodos.map(todo -> buildTaskCreateReq.apply(sop, todo)))
                .flatMap(it -> tenantAccess.flatMap(access -> larkApi.createTask(access.tenant_access_token(), it)))
                .subscribe();
    }

}
