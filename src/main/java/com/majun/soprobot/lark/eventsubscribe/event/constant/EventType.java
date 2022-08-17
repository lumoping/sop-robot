package com.majun.soprobot.lark.eventsubscribe.event.constant;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum EventType {

    MessageReceive("im.message.receive_v1"),
    FileDelete("drive.file.trashed_v1"),
    FileEdit("drive.file.edit_v1");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<EventType> from(String value) {
        return Arrays.stream(EventType.values())
                .filter(it -> Objects.equals(it.getValue(), value))
                .findFirst();
    }
}
