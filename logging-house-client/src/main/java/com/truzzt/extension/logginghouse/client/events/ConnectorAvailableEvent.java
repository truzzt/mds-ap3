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
 *       truzzt GmbH - initial API and implementation
 *
 */

package com.truzzt.extension.logginghouse.client.events;

public class ConnectorAvailableEvent extends CustomLoggingHouseEvent {
    private final String eventId;
    private final String processId;
    private final String messageBody;

    public ConnectorAvailableEvent(String eventId, String processId, String messageBody) {
        this.eventId = eventId;
        this.processId = processId;
        this.messageBody = messageBody;
    }

    @Override
    public String getEventId() {
        return this.eventId;
    }

    @Override
    public String getProcessId() {
        return this.processId;
    }

    @Override
    public String getMessageBody() {
        return this.messageBody;
    }
}
