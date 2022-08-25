package com.majun.soprobot.repo;

import com.majun.soprobot.repo.po.SopTodo;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SopTodoRepo extends ReactiveCrudRepository<SopTodo, Integer> {

    @Lock(LockMode.PESSIMISTIC_WRITE)
    Mono<Void> deleteSopTodosByDocToken(String docToken);

    Flux<SopTodo> findSopTodosByDocToken(String docToken);

    Flux<SopTodo> findSopTodosBySopId(Integer sopId);
}
