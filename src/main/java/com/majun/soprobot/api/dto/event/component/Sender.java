package com.majun.soprobot.api.dto.event.component;

public record Sender(
        UserId sender_id,
        String sender_type,
        String tenant_key
) {
}
