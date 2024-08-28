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
 *
 */

package com.truzzt.extension.logginghouse.client.events.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truzzt.extension.logginghouse.client.multipart.ids.jsonld.JsonLd;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.CalendarUtil;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.IdsConstants;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.IdsMultipartParts;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.MultipartResponse;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.MultipartSenderDelegate;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageImpl;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.IOException;
import java.util.List;

public class LogMessageSender implements MultipartSenderDelegate<LogMessage, LogMessageReceipt> {

    Monitor monitor;

    public LogMessageSender(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public Message buildMessageHeader(LogMessage logMessage, DynamicAttributeToken token) {
        return new LogMessageBuilder()
                ._modelVersion_(IdsConstants.INFORMATION_MODEL_VERSION)
                ._issued_(CalendarUtil.gregorianNow())
                ._securityToken_(token)
                ._issuerConnector_(logMessage.connectorBaseUrl())
                ._senderAgent_(logMessage.connectorBaseUrl())
                .build();
    }

    @Override
    public String buildMessagePayload(LogMessage logMessage) {
        return logMessage.eventToLog();
    }

    @Override
    public MultipartResponse<LogMessageReceipt> getResponseContent(IdsMultipartParts parts) throws Exception {
        return parseLogMessageReceiptResponse(parts, JsonLd.getObjectMapper());
    }

    @Override
    public List<Class<? extends Message>> getAllowedResponseTypes() {
        return List.of(MessageProcessedNotificationMessageImpl.class);
    }

    @Override
    public Class<LogMessage> getMessageType() {
        return LogMessage.class;
    }

    public static MultipartResponse<LogMessageReceipt> parseLogMessageReceiptResponse(IdsMultipartParts parts, ObjectMapper objectMapper) throws IOException {
        var header = objectMapper.readValue(parts.getHeader(), Message.class);

        LogMessageReceipt payload = null;
        if (parts.getPayload() != null) {
            payload = objectMapper.readValue(parts.getPayload(), LogMessageReceipt.class);
        }

        return new MultipartResponse<>(header, payload);
    }
}