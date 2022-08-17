package com.majun.soprobot.lark.api;

import java.io.Serial;

public class LarkException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5733105578180853596L;

    public LarkException() {
    }

    public LarkException(String message) {
        super(message);
    }

    public LarkException(String message, Throwable cause) {
        super(message, cause);
    }

    public LarkException(Throwable cause) {
        super(cause);
    }
}
