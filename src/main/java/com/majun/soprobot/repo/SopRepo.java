package com.majun.soprobot.repo;

import com.majun.soprobot.repo.po.Sop;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SopRepo extends ReactiveCrudRepository<Sop, Integer> {
}


