package com.majun.soprobot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.majun.soprobot.constant.CardMessageType;
import com.majun.soprobot.constant.EventType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("lark")
public class LarkEventController {


    private final KafkaTemplate<String, JsonNode> kafkaTemplate;


    public LarkEventController(KafkaTemplate<String, JsonNode> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/event")
    public Mono<VerifyResp> subscribeLarkEvent(@RequestBody JsonNode message) {
        if ("url_verification".equalsIgnoreCase(Optional.ofNullable(message.get("type")).map(JsonNode::asText).orElse(""))) {
            return Mono.just(new VerifyResp(message.get("challenge").asText()));
        }
        return Mono.just(message)
                .map(it -> it.get("header").get("event_type").asText())
                .map(EventType::from)
                .doOnSuccess(eventType -> eventType.ifPresent(it -> kafkaTemplate.send(it.toString(), message)))
                .then(Mono.empty());

    }

    @PostMapping("/card")
    public Mono<VerifyResp> cardMessage(@RequestBody JsonNode message) {
        if ("url_verification".equalsIgnoreCase(Optional.ofNullable(message.get("type")).map(JsonNode::asText).orElse(""))) {
            return Mono.just(new VerifyResp(message.get("challenge").asText()));
        }
        return Mono.justOrEmpty(Optional.ofNullable(message.get("action"))
                        .map(it -> it.get("value"))
                        .map(it -> it.get("type"))
                        .map(JsonNode::asText)
                        .flatMap(CardMessageType::of)
                        .map(Enum::toString))
                .flatMap(messageType -> Mono.just(message)
                        .doOnSuccess(it -> kafkaTemplate.send(messageType, message))
                        .then(Mono.empty()));
    }

    record VerifyResp(String challenge) {
    }


}
