package com.majun.soprobot.api.dto.event;

import com.majun.soprobot.api.dto.EventMessage;
import com.majun.soprobot.api.dto.event.component.UserId;
import com.majun.soprobot.api.dto.event.constant.FileType;

public record FileDeleteEvent(
        String file_token,
        FileType file_type,
        UserId operator_id)
        implements EventMessage.Event {
}
