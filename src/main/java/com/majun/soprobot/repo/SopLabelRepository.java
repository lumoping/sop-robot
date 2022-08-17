package com.majun.soprobot.repo;

import com.majun.soprobot.repo.po.SopLabel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SopLabelRepository extends ReactiveCrudRepository<SopLabel, Integer> {
}
