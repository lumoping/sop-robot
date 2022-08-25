package com.majun.soprobot.repo;

import com.majun.soprobot.repo.po.SopTodo;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SopTodoRepo extends ReactiveCrudRepository<SopTodo, Integer> {
    Mono<Void> deleteSopTodosByDocToken(String docToken);
}
