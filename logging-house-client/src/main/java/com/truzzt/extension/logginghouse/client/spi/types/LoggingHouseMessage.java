/*
 *  Copyright (c) 2024 truzzt GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       truzzt GmbH - Initial implementation
 *
 */

package com.truzzt.extension.logginghouse.client.spi.types;

import java.time.ZonedDateTime;
import java.util.Objects;

public class LoggingHouseMessage {
    private Long id;
    private Object eventToLog;
    private ZonedDateTime createdAt;
    private ZonedDateTime sentAt;

    public Long getId() {
        return id;
    }
    public Object getEventToLog() {
        return eventToLog;
    }
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    public ZonedDateTime getSentAt() {
        return sentAt;
    }

    public static final class Builder {
        private final LoggingHouseMessage event = new LoggingHouseMessage();

        private Builder() {
        }

        public static LoggingHouseMessage.Builder newInstance() {
            return new LoggingHouseMessage.Builder();
        }

        public LoggingHouseMessage.Builder id(Long id) {
            this.event.id = id;
            return this;
        }
        public LoggingHouseMessage.Builder eventToLog(Object eventToLog) {
            this.event.eventToLog = eventToLog;
            return this;
        }
        public LoggingHouseMessage.Builder createdAt(ZonedDateTime createdAt) {
            this.event.createdAt = createdAt;
            return this;
        }
        public LoggingHouseMessage.Builder sentAt(ZonedDateTime sentAt) {
            this.event.sentAt = sentAt;
            return this;
        }

        public LoggingHouseMessage build() {
            Objects.requireNonNull(this.event.eventToLog, "Message eventToLog must not be null");
            Objects.requireNonNull(this.event.createdAt, "Message createdAt must not be null");
            return this.event;
        }
    }
}
