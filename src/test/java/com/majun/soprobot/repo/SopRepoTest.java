package com.majun.soprobot.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class SopRepoTest {


    @Autowired
    private SopRepo sopRepo;

    @Test
    void searchByDesc() {
        StepVerifier.create(sopRepo.findSopsByChatId("oc_ec46576cca3e83d519c01770f130c2ca"))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(sopRepo.findSopsByChatIdAndDescriptionLike("oc_ec46576cca3e83d519c01770f130c2ca", "%处理案件%"))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(sopRepo.findById(2))
                .expectNextCount(1)
                .verifyComplete();

    }
}