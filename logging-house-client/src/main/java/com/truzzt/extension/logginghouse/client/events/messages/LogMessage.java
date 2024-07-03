/*
 *  Copyright (c) 2022 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial API and implementation
 *       truzzt GmbH - adjusted for EDC 0.x
 *
 */

package com.truzzt.extension.logginghouse.client.events.messages;

import com.truzzt.extension.logginghouse.client.multipart.ExtendedMessageProtocolClearing;
import org.eclipse.edc.spi.types.domain.message.RemoteMessage;

import java.net.URI;
import java.net.URL;

public record LogMessage(URL clearingHouseLogUrl,
                         URI connectorBaseUrl,
                         Object eventToLog) implements RemoteMessage {
    @Override
    public String getProtocol() {
        return ExtendedMessageProtocolClearing.IDS_EXTENDED_PROTOCOL_CLEARING;
    }

    @Override
    public String getCounterPartyAddress() {
        return clearingHouseLogUrl.toString();
    }
}
