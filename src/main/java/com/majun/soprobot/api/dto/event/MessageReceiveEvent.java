package com.majun.soprobot.api.dto.event;

import com.majun.soprobot.api.dto.EventMessage;
import com.majun.soprobot.api.dto.event.component.Message;
import com.majun.soprobot.api.dto.event.component.Sender;

public record MessageReceiveEvent(
        Sender sender,
        Message message)
        implements EventMessage.Event {
}
