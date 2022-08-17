package com.majun.soprobot.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.majun.soprobot.lark.eventsubscribe.EventMessage;
import com.majun.soprobot.lark.eventsubscribe.event.constant.EventType;
import com.majun.soprobot.service.LarkEventService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class LarkEventServiceImpl implements LarkEventService {

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    public LarkEventServiceImpl(KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> produceMessage(Map<String, Object> message) {
        EventMessage.Header header = mapper.convertValue(message.get("header"), EventMessage.Header.class);
        String eventType = header.event_type();
        EventType.from(eventType).ifPresent(it -> kafkaTemplate.send(it.toString(), message));
        return Mono.empty();
    }
}
