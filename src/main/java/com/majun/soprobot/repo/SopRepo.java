package com.majun.soprobot.repo;

import com.majun.soprobot.repo.po.Sop;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SopRepo extends ReactiveCrudRepository<Sop, Integer> {

    Mono<Sop> findSopByDocUrl(String docUrl);

    Mono<Sop> findSopByDocToken(String docToken);

    @Modifying
    @Query("update sop set title = :title where doc_token = :docToken")
    Mono<Integer> updateTitleByDocToken(String title, String docToken);

    @Modifying
    @Query("update sop set description = :description where doc_token = :docToken")
    Mono<Integer> updateDescriptionByDocToken(String description, String docToken);
}


