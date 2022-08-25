package com.majun.soprobot.constant;

import java.util.Arrays;
import java.util.Optional;

public enum CardMessageType {
    CREATE_FILE,
    SEARCH_ALL,
    ;

    public static Optional<CardMessageType> of(String value) {
        return Arrays.stream(CardMessageType.values()).filter(it -> it.toString().equalsIgnoreCase(value)).findFirst();
    }
}
