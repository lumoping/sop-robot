package com.majun.soprobot.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.majun.soprobot.lark.api.LarkApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class LarkMessageConsumer {

    private final LarkApi larkApi;

    private final Mono<LarkApi.TenantAccess> tenantAccess;

    private final Mono<String> rootFolderToken;

    public static final String CONTENT = """
            {
              "config": {
                "wide_screen_mode": true
              },
              "header": {
                "title": {
                  "tag": "plain_text",
                  "content": "Standard Operating Procedure"
                },
                "template": "blue"
              },
              "elements": [
                {
                  "tag": "markdown",
                  "content": "[标准操作程序（SOP）](https://workflowautomation.net/blog/standard-operating-procedure-sop)是一套详细的分步说明，描述如何执行任何给定过程"
                },
                {
                  "tag": "hr"
                },
                {
                  "tag": "markdown",
                  "content": "🥳**开始创建SOP文档吧**"
                },
                {
                  "tag": "action",
                  "actions": [
                    {
                      "tag": "button",
                      "text": {
                        "tag": "plain_text",
                        "content": "创建SOP文档"
                      },
                      "type": "primary"
                    }
                  ]
                }
              ]
            }
            """;


    public LarkMessageConsumer(@Value("${lark.appId}") String appId, @Value("${lark.appSecret}") String appSecret, LarkApi larkApi) {
        this.larkApi = larkApi;
        tenantAccess = larkApi.tenantAccessToken(appId, appSecret)
                .cache(access -> Duration.ofSeconds(access.expire()),
                        throwable -> Duration.ZERO,
                        () -> Duration.ZERO);
        rootFolderToken = tenantAccess.map(LarkApi.TenantAccess::tenant_access_token)
                .flatMap(larkApi::rootFolderToken)
                .cache();
    }

    record Pair<L, R>(L left, R right) {

    }


    @KafkaListener(topics = "ROBOT_ENTER_CHAT")
    void robotEnterChat(JsonNode message) {
        String chatId = message.get("event").get("chat_id").asText();
        tenantAccess.map(LarkApi.TenantAccess::tenant_access_token)
                .flatMap(token -> larkApi.sendMessage(token, "chat_id", new LarkApi.SendMessageReq(chatId, CONTENT, "interactive")))
                .subscribe();
    }
//
//    @KafkaListener(topics = "MessageReceive")
//    Mono<Void> messageReceive(JsonNode message) {
//
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
