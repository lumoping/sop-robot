package com.majun.soprobot.support;

import reactor.core.publisher.Mono;

public interface ResponseHandler<T> {

    Mono<Object> handle(T t, ResponseChain<T> chain);
}
