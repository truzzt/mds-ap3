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

import org.eclipse.edc.spi.event.Event;

public abstract class CustomLoggingHouseEvent extends Event {

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns the unique event id of the event.
     *
     * @return the event id
     */
    public abstract String getEventId();

    /**
     * Returns the process id of the event. This is used as the ProcessId in the LoggingHouse.
     *
     * @return the process id
     */
    public abstract String getProcessId();

    /**
     * Returns the message body of the event. This is used as the message body in the LoggingHouse.
     *
     * @return the message body
     */
    public abstract String getMessageBody();
}
