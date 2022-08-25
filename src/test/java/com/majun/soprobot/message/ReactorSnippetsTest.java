package com.majun.soprobot.message;

import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

class ReactorSnippets {
    private static List<String> words = Arrays.asList(
            "the",
            "quick",
            "brown",
            "fox",
            "jumped",
            "over",
            "the",
            "lazy",
            "dog"
    );

    @Test
    void simpleCreation() {
        Flux<String> fewWords = Flux.just("Hello", "World");
        Flux<String> manyWords = Flux.fromIterable(words);

        fewWords.subscribe(System.out::println);
        System.out.println();
        manyWords.subscribe(System.out::println);
    }


    @Test
    void findingMissingLetter() {
        Flux<String> manyLetters = Flux
                .fromIterable(words)
                .flatMap(word -> Flux.fromArray(word.split("")))
                .distinct()
                .sort()
                .zipWith(Flux.range(1, Integer.MAX_VALUE),
                        (string, count) -> String.format("%2d. %s", count, string));

        manyLetters.subscribe(System.out::println);
    }


    @Test
    void restoringMissingLetter() {
        Mono<String> missing = Mono.just("s");
        Flux<String> allLetters = Flux
                .fromIterable(words)
                .flatMap(word -> Flux.fromArray(word.split("")))
                .concatWith(missing)
                .distinct()
                .sort()
                .zipWith(Flux.range(1, Integer.MAX_VALUE),
                        (string, count) -> String.format("%2d. %s", count, string));

        allLetters.subscribe(System.out::println);
    }

    @Test
    void shortCircuit() throws InterruptedException {
        Flux<String> helloPauseWorld =
                Mono.just("Hello")
                        .concatWith(Mono.just("world")
                                .delaySubscription(Duration.ofMillis(500)));
        helloPauseWorld.subscribe(System.out::println);
    }


    @Test
    public void blocks() {
        Flux<String> helloPauseWorld =
                Mono.just("Hello")
                        .concatWith(Mono.just("world")
                                .delaySubscription(Duration.ofMillis(500)));

        helloPauseWorld.toStream()
                .forEach(System.out::println);
    }


    @Test
    void firstEmitting() {
        Mono<String> a = Mono.just("oops I'm late")
                .delaySubscription(Duration.ofMillis(450));
        Flux<String> b = Flux.just("let's get", "the party", "started")
                .delaySubscription(Duration.ofMillis(400));

        Flux.firstWithValue(a, b)
                .toIterable()
                .forEach(System.out::println);
    }


    @Test
    void generate() {
        Flux.generate(() -> 0, (state, sink) -> {
            sink.next("3 x " + state + " = " + 3 * state);
            if (state == 10) sink.complete();
            return state + 1;
        }, System.out::println).subscribe(System.out::println);
    }

    interface MyEventListener<T> {
        void onDataChunk(List<T> chunk);
    }

    public String alphabet(int letterNumber) {
        if (letterNumber < 1 || letterNumber > 26) {
            return null;
        }
        int letterIndexAscii = 'A' + letterNumber - 1;
        return "" + (char) letterIndexAscii;
    }

    @Test
    void handle() {
        Flux.just(-1, 44, 63, 3, 6, 89, 21)
                .handle((BiConsumer<Integer, SynchronousSink<String>>) (integer, synchronousSink) -> {
                    String s = alphabet(integer);
                    if (s != null) {
                        synchronousSink.next(s);
                    }
                }).subscribe(System.out::println);

        Flux.just(-1, 44, 63, 3, 6, 89, 21)
                .map(this::alphabet)
                .onErrorContinue((e, o) -> {
                })
                .filter(Objects::nonNull)
                .subscribe(System.out::println);
    }

    @Test
    void publishOn() throws InterruptedException {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> {
                    System.out.println("i " + i + " thread " + Thread.currentThread().getName());
                    return 10 + i;
                })
                .publishOn(s)
                .map(i -> "value " + i + " thread :" + Thread.currentThread().getName());

        Thread thread = new Thread(() -> flux.subscribe(System.out::println), " majun ");
        thread.start();
        thread.join();

    }


    @Test
    void subscribeOn() throws InterruptedException {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> {
                    System.out.println("i " + i + " thread " + Thread.currentThread().getName());
                    return 10 + i;
                })
                .subscribeOn(s)
                .map(i -> "value " + i + " thread :" + Thread.currentThread().getName());

        Thread thread = new Thread(() -> flux.subscribe(System.out::println), " majun ");
        thread.start();
        thread.join();

    }

    @Test
    void error() throws InterruptedException {
        AtomicInteger errorCount = new AtomicInteger();
        Flux<String> flux =
                Flux.<String>error(new IllegalArgumentException())
                        .doOnError(e -> errorCount.incrementAndGet())
                        .retryWhen(Retry.from(companion ->
                                companion.map(rs -> {
                                    if (rs.totalRetries() < 3) return rs.totalRetries();
                                    else throw Exceptions.propagate(rs.failure());
                                })));

        flux.subscribe(System.out::println);
        System.out.println(errorCount);
    }
}
