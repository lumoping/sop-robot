package com.majun.soprobot.service.impl;

import com.majun.soprobot.api.dto.EventMessage;
import com.majun.soprobot.service.LarkEventService;
import reactor.core.publisher.Mono;

public class LarkEventServiceImpl implements LarkEventService {

    @Override
    public Mono<Void> produceMessage(EventMessage message) {
        return null;
    }

}
