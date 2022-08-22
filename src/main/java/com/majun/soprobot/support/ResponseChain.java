package com.majun.soprobot.support;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ResponseChain<T> {

    List<ResponseHandler<T>> allHandlers;

    @Nullable
    private final ResponseHandler<T> currentHandler;

    @Nullable
    private final ResponseChain<T> chain;


    public ResponseChain(List<ResponseHandler<T>> handlers) {
        this.allHandlers = Collections.unmodifiableList(handlers);
        ResponseChain<T> initChain = initChain(handlers);
        this.currentHandler = initChain.currentHandler;
        this.chain = initChain;
    }

    private static <T> ResponseChain<T> initChain(List<ResponseHandler<T>> handlers) {
        ResponseChain<T> chain = new ResponseChain<>(handlers, null, null);
        ListIterator<? extends ResponseHandler<T>> iterator = handlers.listIterator(handlers.size());
        while (iterator.hasPrevious()) {
            chain = new ResponseChain<>(handlers, iterator.previous(), chain);
        }
        return chain;
    }

    private ResponseChain(List<ResponseHandler<T>> allHandlers,
                          @Nullable ResponseHandler<T> currentHandler, @Nullable ResponseChain<T> chain) {

        this.allHandlers = allHandlers;
        this.currentHandler = currentHandler;
        this.chain = chain;
    }

    public Mono<Object> doProcess(T t) {
        return Mono.defer(() ->
                this.currentHandler != null && this.chain != null ?
                        this.currentHandler.handle(t, this.chain) :
                        Mono.empty());
    }
}
