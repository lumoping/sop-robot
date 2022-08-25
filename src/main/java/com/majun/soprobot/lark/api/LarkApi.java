package com.majun.soprobot.lark.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.handler.logging.LogLevel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.List;

@Component
public class LarkApi {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://open.feishu.cn")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create().wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
            ))
            .build();


    public Mono<TenantAccess> tenantAccessToken(String appId, String appSecret) {
        return webClient.post()
                .uri("open-apis/auth/v3/tenant_access_token/internal")
                .body(Mono.just(new AppCert(appId, appSecret)), AppCert.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TenantAccess.class)
                .flatMap(it -> it.success() ? Mono.just(it) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }


    public Mono<String> rootFolderToken(String tenantAccessToken) {
        return webClient.get()
                .uri("open-apis/drive/explorer/v2/root_folder/meta")
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(RootFolderMetaResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data.token) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Flux<String> getFilesToken(String tenantAccessToken, String folderToken) {
        return webClient.get()
                .uri("open-apis/drive/v1/files?folder_token={folder_token}", folderToken)
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FolderFilesResp.typeRef)
                .flatMapMany(it -> it.success() ? Flux.fromIterable(it.data.files) : Flux.error(new LarkException(it.code + ":" + it.msg)))
                .map(it -> it.token);
    }

    public Mono<FolderCreateResp> createFolder(String tenantAccessToken, String folderToken, String name) {
        return webClient.post()
                .uri("/open-apis/drive/v1/files/create_folder")
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .body(Mono.just(new FolderCreateReq(name, folderToken)), FolderCreateReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FolderCreateResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<FileCreateResp> createFile(String tenantAccessToken, String folderToken) {
        return webClient.post()
                .uri("open-apis/docx/v1/documents")
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .body(Mono.just(new FileCreateReq(folderToken)), FileCreateReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FileCreateResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<PermissionMemberCreateResp> createPermissionMember(String tenantAccessToken, String fileToken, String fileType, PermissionMemberCreateReq param) {
        return webClient.post()
                .uri("open-apis/drive/v1/permissions/{fileToken}/members?type={fileType}", fileToken, fileType)
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .body(Mono.just(param), PermissionMemberCreateReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PermissionMemberCreateResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<Void> subscribeFile(String tenantAccessToken, String fileToken, String fileType) {
        return webClient.post()
                .uri("open-apis/drive/v1/files/{fileToken}/subscribe?file_type={fileType}", fileToken, fileType)
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SubscribeFileResp.typeRef)
                .flatMap(it -> it.success() ? Mono.empty() : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<FileMetaResp.Meta> fileMeta(String tenantAccessToken, String fileToken, String fileType) {
        return webClient.post()
                .uri("open-apis/drive/v1/metas/batch_query")
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .body(Mono.just(new FileMetaReq(List.of(new FileMetaReq.Doc(fileToken, fileType)), true)), FileMetaReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FileMetaResp.typeRef)
                .flatMap(it -> it.success() ? Mono.from(Flux.fromIterable(it.data.metas)) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }


    public Mono<Boolean> permitted(String userAccessToken, PermissionMemberPermittedReq param) {
        return webClient.post()
                .uri("open-apis/drive/permission/member/permitted")
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(userAccessToken))
                .body(Mono.just(param), PermissionMemberPermittedReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PermissionMemberPermittedResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data.is_permitted) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }


    public Mono<BlockGetAllResp> getAllBlock(String tenantAccessToken, String documentId) {
        return webClient.get()
                .uri("open-apis/docx/v1/documents/{document_id}/blocks", documentId)
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BlockGetAllResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<BlockGetChildrenResp> getChildrenBlock(String tenantAccessToken, String documentId, String parentBlockId) {
        return webClient.get()
                .uri("open-apis/docx/v1/documents/{document_id}/blocks/{block_id}/children", documentId, parentBlockId)
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BlockGetChildrenResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<BlockCreateResp> createBlock(String tenantAccessToken, String documentId, String blockId, List<Item> blocks) {
        return webClient.post()
                .uri("open-apis/docx/v1/documents/{document_id}/blocks/{block_id}/children", documentId, blockId)
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .body(Mono.just(new BlockCreateReq(blocks, 0)), BlockCreateReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BlockCreateResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<BlockGetResp> getBlock(String tenantAccessToken, String documentId, String blockId) {
        return webClient.get()
                .uri("open-apis/docx/v1/documents/{document_id}/blocks/{block_id}", documentId, blockId)
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BlockGetResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<Void> sendMessage(String tenantAccessToken, String receiveIdType, SendMessageReq param) {
        return webClient.post()
                .uri("open-apis/im/v1/messages?receive_id_type={receive_id_type}", receiveIdType)
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .body(Mono.just(param), SendMessageReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SendMessageResp.typeRef)
                .flatMap(it -> it.success() ? Mono.empty() : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<Void> sendPersonalMessage(String tenantAccessToken, SendPersonalMessageReq param) {
        return webClient.post()
                .uri("open-apis/ephemeral/v1/send")
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .body(Mono.just(param), SendPersonalMessageReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(SendPersonalMessageResp.typeRef)
                .flatMap(it -> it.success() ? Mono.empty() : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<TaskCreateResp> createTask(String tenantAccessToken, TaskCreateReq param) {
        return webClient.post()
                .uri("open-apis/task/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, prefixBearer(tenantAccessToken))
                .body(Mono.just(param), TaskCreateReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TaskCreateResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }


    private String prefixBearer(String tenantAccessToken) {
        return "Bearer " + tenantAccessToken;
    }

    public record AppCert(String app_id, String app_secret) {
    }

    public record TenantAccess(int code, String msg, String tenant_access_token, int expire) {
        boolean success() {
            return code == 0;
        }
    }


    interface LarkResponseData {
    }

    record LarkResponse<T extends LarkResponseData>(
            int code,
            String msg,
            T data
    ) {
        boolean success() {
            return code == 0;
        }
    }

    record RootFolderMetaResp(String token,
                              String id,
                              String user_id) implements LarkResponseData {

        static ParameterizedTypeReference<LarkResponse<RootFolderMetaResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    public record FolderFilesResp(
            List<FileMeta> files,
            String next_page_token,
            boolean has_more
    ) implements LarkResponseData {

        static ParameterizedTypeReference<LarkResponse<FolderFilesResp>> typeRef = new ParameterizedTypeReference<>() {
        };

        public record FileMeta(
                String token,
                String name,
                String type,
                String parent_token,
                String url
        ) {

        }


    }

    public record FolderCreateReq(String name, String folder_token) {

    }

    public record FolderCreateResp(String token, String url) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<FolderCreateResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    record FileCreateReq(String FolderToken) {
    }

    public record FileCreateResp(Document document) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<FileCreateResp>> typeRef = new ParameterizedTypeReference<>() {
        };

        public String documentId() {
            return document.document_id;
        }

        public String revisionId() {
            return document.revision_id;
        }

        public String title() {
            return document.title;
        }

        record Document(String document_id,
                        String revision_id,
                        String title) {
        }

    }

    record SubscribeFileResp() implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<SubscribeFileResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }


    record FileMetaReq(
            List<Doc> request_docs,
            boolean with_url
    ) {
        record Doc(
                String doc_token,
                String doc_type
        ) {
        }
    }

    public record FileMetaResp(
            List<Meta> metas,
            List<Fail> failed_list
    ) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<FileMetaResp>> typeRef = new ParameterizedTypeReference<>() {
        };

        public record Meta(
                String doc_token,
                String doc_type,
                String title,
                String owner_id,
                String create_time,
                String latest_modify_user,
                String latest_modify_time,
                String url
        ) {
        }

        record Fail(String token,
                    int code) {

        }
    }


    public record PermissionMemberCreateReq(
            String member_type,
            String member_id,
            String perm
    ) {

    }

    public record PermissionMemberCreateResp() implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<PermissionMemberCreateResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    record PermissionMemberPermittedReq(
            String token,
            String type,
            String perm
    ) {

    }

    record PermissionMemberPermittedResp(boolean is_permitted) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<PermissionMemberPermittedResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }


    record BlockCreateReq(List<Item> children, int index) {

    }

    public record BlockCreateResp(List<Item> children) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<BlockCreateResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    public record BlockGetChildrenResp(List<Item> items) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<BlockGetChildrenResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }


    public record BlockGetAllResp(List<Item> items) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<BlockGetAllResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    public record Item(
            String block_id,
            String parent_id,
            List<String> children,
            int block_type,
            Block page,
            Block text,
            Block heading1,
            Block heading2,
            Block heading3,
            Block heading4,
            Block heading5,
            Block heading6,
            Block heading7,
            Block heading8,
            Block heading9,
            Block bullet,
            Block ordered,
            Block code,
            Block quote,
            Block todo,
            Callout callout
    ) {

        public static Item text(int blockType, Block text) {
            return new Item(null, null, null, blockType, null, text, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        public static Item callout(int blockType, Callout callout) {
            return new Item(null, null, null, blockType, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, callout);
        }

        public record Block(Style style,
                            List<Element> elements) {
            public record Style(
                    int align,
                    boolean done,
                    boolean folded,
                    int language,
                    boolean wrap
            ) {
            }

        }
    }

    public record Element(TextRun text_run,
                          MentionUser mention_user,
                          MentionDoc mention_doc,
                          Reminder reminder,
                          File file
    ) {

        public Element(TextRun textRun) {
            this(textRun, null, null, null, null);
        }
    }


    public record MentionUser(String user_id) {
    }

    public record File(String file_token, String source_block_id) {
    }

    public record Reminder(
            String create_user_id,
            boolean is_notify,
            boolean is_whole_day,
            String expire_time,
            String notify_time
    ) {
    }


    public record MentionDoc(
            String token,
            int obj_type,
            String url,
            String title
    ) {
    }

    public record TextRun(String content) {

    }

    public record Callout(
            Integer background_color,
            Integer border_color,
            Integer text_color,
            String emoji_id
    ) {

    }


    record BlockGetResp() implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<BlockGetResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    public record SendMessageReq(
            String receive_id,
            String content,
            String msg_type
    ) {
    }

    public record SendMessageResp() implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<SendMessageResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    public record SendPersonalMessageReq(
            String chat_id,
            String open_id,
            String msg_type,
            JsonNode card
    ) {
    }

    public record SendPersonalMessageResp() implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<SendPersonalMessageResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }


    public record TaskCreateReq(
            String summary,
            String description,
            String extra,
            Due due,
            Origin origin,
            boolean can_edit,
            String custom,
            List<String> collaborator_ids,
            List<String> follower_ids,
            String repeat_rule,
            String rich_summary,
            String rich_description
    ) {

        public static TaskCreateReq simple(String summary, Due due, Origin origin, boolean canEdit, List<String> collaboratorIds) {
            return new TaskCreateReq(summary, null, null, due, origin, canEdit, null, collaboratorIds, null, null, null, null);
        }
    }


    public record TaskCreateResp(
    ) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<TaskCreateResp>> typeRef = new ParameterizedTypeReference<>() {
        };

        record Task(
                String id,
                String summary,
                String description,
                String complete_time,
                String creator_id,
                String extra,
                String create_time,
                String update_time,
                Due due,
                Origin origin,
                boolean can_edit,
                String custom,
                int source,
                List<Follower> followers,
                List<Collaborator> collaborators,
                List<String> collaborator_ids,
                List<String> follower_ids,
                String repeat_rule,
                String rich_summary,
                String rich_description
        ) {
        }
    }

    record Follower(String id, List<String> id_list) {

    }

    record Collaborator(String id, List<String> id_list) {

    }

    public record Due(String time, String timezone, boolean is_all_day) {
    }

    public record Origin(String platform_i18n_name, Href href) {
        public record Href(String url, String title) {
        }
    }
}
