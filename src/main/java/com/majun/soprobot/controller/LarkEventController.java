package com.majun.soprobot.controller;

import com.majun.soprobot.service.LarkEventService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("lark")
public class LarkEventController {

    private final LarkEventService larkEventService;

    public LarkEventController(LarkEventService larkEventService) {
        this.larkEventService = larkEventService;
    }

    @PostMapping("/event")
    public Mono<VerifyResp> subscribeLarkEvent(@RequestBody Map<String, Object> message) {
        if ("url_verification".equalsIgnoreCase((String) message.get("type"))) {
            return Mono.just(new VerifyResp((String) message.get("challenge")));
        }
        larkEventService.produceMessage(message);
        return Mono.empty();
    }

    record VerifyResp(String challenge) {
    }

}
