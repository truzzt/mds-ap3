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

    private LoggingHouseMessage() {
    }

    private Long id;
    private Class<?> eventType;
    private String eventId;
    private Object eventToLog;
    private boolean createProcess;
    private String processId;
    private String consumerId;
    private String providerId;
    private ZonedDateTime createdAt;

    public Long getId() {
        return id;
    }
    public Class<?> getEventType() {
        return eventType;
    }
    public String getEventId() {
        return eventId;
    }
    public Object getEventToLog() {
        return eventToLog;
    }
    public boolean getCreateProcess() {
        return createProcess;
    }
    public String getProcessId() {
        return processId;
    }
    public String getConsumerId() {
        return consumerId;
    }
    public String getProviderId() {
        return providerId;
    }
    public ZonedDateTime getCreatedAt() {
        return createdAt;
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
        public LoggingHouseMessage.Builder eventType(Class<?> eventType) {
            this.event.eventType = eventType;
            return this;
        }
        public LoggingHouseMessage.Builder eventId(String eventId) {
            this.event.eventId = eventId;
            return this;
        }
        public LoggingHouseMessage.Builder eventToLog(Object eventToLog) {
            this.event.eventToLog = eventToLog;
            return this;
        }
        public LoggingHouseMessage.Builder createProcess(boolean createProcess) {
            this.event.createProcess = createProcess;
            return this;
        }
        public LoggingHouseMessage.Builder processId(String processId) {
            this.event.processId = processId;
            return this;
        }
        public LoggingHouseMessage.Builder consumerId(String consumerId) {
            this.event.consumerId = consumerId;
            return this;
        }
        public LoggingHouseMessage.Builder providerId(String providerId) {
            this.event.providerId = providerId;
            return this;
        }
        public LoggingHouseMessage.Builder createdAt(ZonedDateTime createdAt) {
            this.event.createdAt = createdAt;
            return this;
        }

        public LoggingHouseMessage build() {
            Objects.requireNonNull(this.event.eventType, "Message eventType must not be null");
            Objects.requireNonNull(this.event.eventId, "Message eventId must not be null");
            Objects.requireNonNull(this.event.eventToLog, "Message eventToLog must not be null");
            Objects.requireNonNull(this.event.createProcess, "Message createProcess must not be null");
            Objects.requireNonNull(this.event.processId, "Message processId must not be null");
            Objects.requireNonNull(this.event.createdAt, "Message createdAt must not be null");
            return this.event;
        }
    }
}
