package com.majun.soprobot.service;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public interface LarkEventService {

    Mono<Void> produceMessage(JsonNode message);
}
