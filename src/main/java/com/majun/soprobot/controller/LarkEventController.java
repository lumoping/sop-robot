package com.majun.soprobot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.majun.soprobot.service.LarkEventService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("lark")
public class LarkEventController {

    private final LarkEventService larkEventService;

    public LarkEventController(LarkEventService larkEventService) {
        this.larkEventService = larkEventService;
    }

    @PostMapping("/event")
    public Mono<VerifyResp> subscribeLarkEvent(@RequestBody JsonNode message) {
        if ("url_verification".equalsIgnoreCase(Optional.ofNullable(message.get("type")).map(JsonNode::asText).orElse(""))) {
            return Mono.just(new VerifyResp(message.get("challenge").asText()));
        } else {
            return larkEventService.produceMessage(message).flatMap(it -> Mono.just(new VerifyResp("")));
        }
    }

    record VerifyResp(String challenge) {
    }


}
