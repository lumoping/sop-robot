package com.majun.soprobot.service;

import com.majun.soprobot.api.dto.EventMessage;
import reactor.core.publisher.Mono;

public interface LarkEventService {

    Mono<Void> produceMessage(EventMessage message);
}
