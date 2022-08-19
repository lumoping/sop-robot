package com.majun.soprobot.lark.eventsubscribe.event;

import com.majun.soprobot.lark.eventsubscribe.EventMessage;
import com.majun.soprobot.lark.eventsubscribe.event.component.UserId;
import com.majun.soprobot.constant.FileType;

public record FileDeleteEvent(
        String file_token,
        FileType file_type,
        UserId operator_id)
        implements EventMessage.Event {
}
