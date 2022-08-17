package com.majun.soprobot.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface LarkEventService {

    Mono<Void> produceMessage(Map<String, Object> message);
}
