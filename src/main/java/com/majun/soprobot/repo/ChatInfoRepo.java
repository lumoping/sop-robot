package com.majun.soprobot.repo;

import com.majun.soprobot.repo.po.ChatInfo;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ChatInfoRepo extends ReactiveCrudRepository<ChatInfo, Integer> {

    Mono<ChatInfo> findChatInfoByChatId(String chatId);
}
