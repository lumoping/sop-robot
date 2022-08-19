package com.majun.soprobot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.majun.soprobot.constant.EventType;
import com.majun.soprobot.service.LarkEventService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LarkEventServiceImpl implements LarkEventService {

    private final KafkaTemplate<String, JsonNode> kafkaTemplate;

    public LarkEventServiceImpl(KafkaTemplate<String, JsonNode> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> produceMessage(JsonNode message) {
        String eventType = message.get("header").get("event_type").asText();
        EventType.from(eventType).ifPresent(it -> kafkaTemplate.send(it.toString(), message));
        return Mono.empty();
    }
}
