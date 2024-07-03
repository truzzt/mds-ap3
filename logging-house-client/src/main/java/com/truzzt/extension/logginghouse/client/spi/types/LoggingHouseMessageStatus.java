package com.truzzt.extension.logginghouse.client.spi.types;

import org.eclipse.edc.spi.EdcException;

public enum LoggingHouseMessageStatus {
    PENDING("P"), SENT("S");

    private final String code;

    LoggingHouseMessageStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static LoggingHouseMessageStatus codeOf(String code) {
        switch(code) {
            case "P":
                return PENDING;
            case "S":
                return SENT;
            default:
                throw new EdcException("Invalid status code " + code);
        }
    }
}
