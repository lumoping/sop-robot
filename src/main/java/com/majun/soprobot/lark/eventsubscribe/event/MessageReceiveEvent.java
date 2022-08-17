package com.majun.soprobot.lark.eventsubscribe.event;

import com.majun.soprobot.lark.eventsubscribe.EventMessage;
import com.majun.soprobot.lark.eventsubscribe.event.component.Message;
import com.majun.soprobot.lark.eventsubscribe.event.component.Sender;

public record MessageReceiveEvent(
        Sender sender,
        Message message)
        implements EventMessage.Event {
}
