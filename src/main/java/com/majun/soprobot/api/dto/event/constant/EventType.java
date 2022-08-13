package com.majun.soprobot.api.dto.event.constant;

public enum EventType {

    MessageReceive("im.message.receive_v1"),
    FileDelete("drive.file.trashed_v1");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
