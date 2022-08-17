package com.majun.soprobot.service;

import com.majun.soprobot.lark.eventsubscribe.EventMessage;
import reactor.core.publisher.Mono;

public interface LarkEventService {

    Mono<Void> produceMessage(EventMessage message);
}
