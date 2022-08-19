package com.majun.soprobot.constant;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum EventType {
    ROBOT_ENTER_CHAT("im.chat.member.bot.added_v1"),
    MESSAGE_RECEIVE("im.message.receive_v1"),
    FILE_TITLE_UPDATE("drive.file.title_updated_v1"),
    FILE_EDIT("drive.file.edit_v1"),
    FILE_TRASHED("drive.file.trashed_v1"),
    FILE_DELETE("drive.file.deleted_v1"),
    ;


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
