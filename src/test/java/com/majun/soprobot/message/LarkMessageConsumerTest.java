package com.majun.soprobot.message;

import com.majun.soprobot.SopRobotApplication;
import com.majun.soprobot.lark.api.LarkApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

@SpringBootTest(classes = SopRobotApplication.class)
class LarkMessageConsumerTest {


    @Autowired
    LarkApi larkApi;

    @Test
    void message() {
        larkApi.tenantAccessToken("cli_a270a6db85fad00b", "LZxrWWcHKavl6xyGLN0behga8y0neDku")
                .cache(access -> Duration.ofSeconds(access.expire()),
                        throwable -> Duration.ZERO,
                        () -> Duration.ZERO)
                .map(LarkApi.TenantAccess::tenant_access_token)
                .flatMap(token -> larkApi.sendMessage(token, "chat_id", new LarkApi.SendMessageReq("oc_ec46576cca3e83d519c01770f130c2ca", LarkMessageConsumer.CONTENT, "interactive")))
                .subscribe(value -> {
                }, System.err::println, () -> System.out.println("finish"));
    }
}