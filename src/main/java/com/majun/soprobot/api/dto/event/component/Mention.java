package com.majun.soprobot.api.dto.event.component;

public record Mention(
        String key,
        UserId user_id,
        String name,
        String tenant_key
) {
}
