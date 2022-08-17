package com.majun.soprobot.lark.eventsubscribe;

import com.majun.soprobot.lark.eventsubscribe.event.component.UserId;

public record EventMessage(String schema,
                           Header header,
                           Event event) {

    public record Header(
            String event_id,
            String token,
            String create_time,
            String event_type,
            String tenant_key,
            String app_id,
            String resource_id,
            UserId[] user_list
    ) {
    }

    public interface Event {
    }
}
