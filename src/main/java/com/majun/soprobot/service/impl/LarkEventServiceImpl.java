package com.majun.soprobot.service.impl;

import com.majun.soprobot.lark.eventsubscribe.EventMessage;
import com.majun.soprobot.lark.eventsubscribe.event.constant.EventType;
import com.majun.soprobot.service.LarkEventService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LarkEventServiceImpl implements LarkEventService {

    private final KafkaTemplate<String, EventMessage> kafkaTemplate;

    public LarkEventServiceImpl(KafkaTemplate<String, EventMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> produceMessage(EventMessage message) {
        String eventType = message.header().event_type();
        EventType.from(eventType).ifPresent(it -> kafkaTemplate.send(it.getValue(), message));
        return Mono.empty();
    }
}
