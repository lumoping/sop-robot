package com.majun.soprobot.lark.eventsubscribe.event.component;

public record Message(
        String message_id,
        String root_id,
        String parent_id,
        String create_time,
        String chat_id,
        String chat_type,
        String message_type,
        String content,
        Mention[] mentions
) {
}
