package com.majun.soprobot.lark.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class LarkApi {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://open.feishu.cn")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();


    private final String appId;


    private final String appSecret;

    public LarkApi(@Value("${lark.appId}") String appId, @Value("${lark.appSecret}") String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
    }

    public Mono<String> tenantAccessToken() {
        return webClient.post()
                .uri("open-apis/auth/v3/tenant_access_token/internal")
                .body(Mono.just(new AppCert(appId, appSecret)), AppCert.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TenantAccessResp.class)
                .flatMap(it -> it.success() ? Mono.just(it.tenant_access_token) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }


    public Mono<String> rootFolderToken(String tenantAccessToken) {
        return webClient.get()
                .uri("open-apis/drive/explorer/v2/root_folder/meta")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAccessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(RootFolderMetaResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data.token) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }


    public Flux<String> getFilesToken(String tenantAccessToken, String folder_token) {
        return webClient.get()
                .uri("open-apis/drive/v1/files?folder_token={folder_token}", folder_token)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAccessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FolderFilesResp.typeRef)
                .flatMapMany(it -> it.success() ? Flux.just(it.data.files) : Flux.error(new LarkException(it.code + ":" + it.msg)))
                .map(FolderFilesResp.FileMeta::token);
    }

    public Mono<FolderCreateResp> createFolder(String tenantAccessToken, String folder_token, String name) {
        return webClient.post()
                .uri("/open-apis/drive/v1/files/create_folder")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAccessToken)
                .body(Mono.just(new FolderCreateReq(name, folder_token)), FolderCreateReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FolderCreateResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<FileCreateResp> createFile(String tenantAccessToken, String folder_token) {
        return webClient.post()
                .uri("open-apis/doc/v2/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAccessToken)
                .body(Mono.just(new FileCreateReq(folder_token)), FileCreateReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FileCreateResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<PermissionMemberCreateResp> createPermissionMember(String tenantAccessToken, String fileToken, String fileType, String member_type, String member_id, String perm) {
        return webClient.post()
                .uri("open-apis/drive/v1/permissions/{fileToken}/members?type={fileType}", fileToken, fileType)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAccessToken)
                .body(Mono.just(new PermissionMemberCreateReq(member_type, member_id, perm)), PermissionMemberCreateReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PermissionMemberCreateResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }


    public Mono<Boolean> permitted(String user_access_token, String fileToken, String fileType, String perm) {
        return webClient.post()
                .uri("open-apis/drive/permission/member/permitted")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user_access_token)
                .body(Mono.just(new PermissionMemberPermittedReq(fileToken, fileType, perm)), PermissionMemberPermittedReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PermissionMemberPermittedResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data.is_permitted) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }


    public Mono<BlockGetAllResp> getAllBlock(String tenantAccessToken, String documentId) {
        return webClient.get()
                .uri("open-apis/docx/v1/documents/{document_id}/blocks", documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAccessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BlockGetAllResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<BlockGetResp> getBlock(String tenantAccessToken, String documentId, String blockId) {
        return webClient.get()
                .uri("open-apis/docx/v1/documents/{document_id}/blocks/{block_id}", documentId, blockId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAccessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BlockGetResp.typeRef)
                .flatMap(it -> it.success() ? Mono.just(it.data) : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }

    public Mono<Void> sendMessage(String tenantAccessToken, String receiveIdType, String receiveId, String content, String msgType) {
        return webClient.post()
                .uri("open-apis/im/v1/messages?receive_id_type={receive_id_type}", receiveIdType)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAccessToken)
                .body(Mono.just(new SendMessageReq(receiveId, content, msgType)), SendMessageReq.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(LarkResponse.class)
                .flatMap(it -> it.success() ? Mono.empty() : Mono.error(new LarkException(it.code + ":" + it.msg)));
    }


    public record AppCert(String app_id, String app_secret) {
    }

    record TenantAccessResp(int code, String msg, String tenant_access_token, int expire) {
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

    record FolderFilesResp(
            FileMeta[] files,
            String next_page_token,
            boolean has_more
    ) implements LarkResponseData {

        static ParameterizedTypeReference<LarkResponse<FolderFilesResp>> typeRef = new ParameterizedTypeReference<>() {
        };

        record FileMeta(
                String token,
                String name,
                String type,
                String parent_token,
                String url
        ) {

        }

    }

    record FolderCreateReq(String name, String folder_token) {

    }

    record FolderCreateResp(String token, String url) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<FolderCreateResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    record FileCreateReq(String FolderToken) {
    }

    record FileCreateResp(String objToken, String url) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<FileCreateResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    record PermissionMemberCreateReq(
            String member_type,
            String member_id,
            String perm
    ) {

    }

    record PermissionMemberCreateResp() implements LarkResponseData {
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


    record BlockGetAllResp(Item[] items) implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<BlockGetAllResp>> typeRef = new ParameterizedTypeReference<>() {
        };


        record Item(
                String block_id,
                String parent_id,
                String[] children,
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
                Block todo
        ) {

            record Block(Style style,
                         Element[] elements) {
                record Style(
                        int align,
                        boolean done,
                        boolean folded,
                        int language,
                        boolean wrap
                ) {
                }

                record Element(TextRun textRun,
                               MentionUser mention_user,
                               MentionDoc mention_doc,
                               Reminder reminder,
                               File file
                ) {

                    record TextRun(String content) {
                        record TextElementStyle(
                                boolean bold,
                                boolean italic,
                                boolean strikethrough,
                                boolean underline,
                                boolean inline_code,
                                int background_color,
                                int text_color,
                                Link link
                        ) {
                            record Link(String url) {
                            }
                        }
                    }

                    record MentionUser(String user_id) {
                    }

                    record MentionDoc(
                            String token,
                            int obj_type,
                            String url,
                            String title
                    ) {
                    }

                    record Reminder(
                            String create_user_id,
                            boolean is_notify,
                            boolean is_whole_day,
                            String expire_time,
                            String notify_time
                    ) {
                    }

                    record File(String file_token, String source_block_id) {

                    }

                }
            }


        }
    }

    record BlockGetResp() implements LarkResponseData {
        static ParameterizedTypeReference<LarkResponse<BlockGetResp>> typeRef = new ParameterizedTypeReference<>() {
        };
    }

    record SendMessageReq(
            String receive_id,
            String content,
            String msg_type
    ) {
    }
}
