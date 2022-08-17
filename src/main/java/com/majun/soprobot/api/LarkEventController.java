package com.majun.soprobot.api;

import com.majun.soprobot.lark.eventsubscribe.EventMessage;
import com.majun.soprobot.service.LarkEventService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("lark")
public class LarkEventController {

    private final LarkEventService larkEventService;

    public LarkEventController(LarkEventService larkEventService) {
        this.larkEventService = larkEventService;
    }

    @PostMapping("/event")
    public Mono<Void> subscribeLarkEvent(EventMessage message) {
        return larkEventService.produceMessage(message);
    }

}
