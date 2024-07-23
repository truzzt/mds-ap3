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

package com.truzzt.extension.logginghouse.client.events.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truzzt.extension.logginghouse.client.multipart.ids.jsonld.JsonLd;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.CalendarUtil;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.IdsConstants;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.IdsMultipartParts;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.MultipartResponse;
import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.MultipartSenderDelegate;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageImpl;
import de.fraunhofer.iais.eis.RequestMessageBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class CreateProcessMessageSender implements MultipartSenderDelegate<CreateProcessMessage, CreateProcessReceipt> {

    public CreateProcessMessageSender() {
    }

    @Override
    public Message buildMessageHeader(CreateProcessMessage createProcessMessage, DynamicAttributeToken token) {
        return new RequestMessageBuilder()
                ._modelVersion_(IdsConstants.INFORMATION_MODEL_VERSION)
                ._issued_(CalendarUtil.gregorianNow())
                ._securityToken_(token)
                ._issuerConnector_(createProcessMessage.connectorBaseUrl())
                ._senderAgent_(createProcessMessage.connectorBaseUrl())
                .build();
    }

    @Override
    public String buildMessagePayload(CreateProcessMessage createProcessMessage) {
        var jo = new JSONObject();
        jo.put("owners", createProcessMessage.processOwners());
        return jo.toString();
    }

    @Override
    public MultipartResponse<CreateProcessReceipt> getResponseContent(IdsMultipartParts parts) throws Exception {
        return parseCreateProcessReceiptResponse(parts, JsonLd.getObjectMapper());
    }

    @Override
    public List<Class<? extends Message>> getAllowedResponseTypes() {
        return List.of(MessageProcessedNotificationMessageImpl.class);
    }

    @Override
    public Class<CreateProcessMessage> getMessageType() {
        return CreateProcessMessage.class;
    }

    private MultipartResponse<CreateProcessReceipt> parseCreateProcessReceiptResponse(IdsMultipartParts parts, ObjectMapper objectMapper) throws IOException {
        var header = objectMapper.readValue(parts.getHeader(), Message.class);

        CreateProcessReceipt payload = null;
        if (parts.getPayload() != null) {
            payload = objectMapper.readValue(parts.getPayload(), CreateProcessReceipt.class);
        }

        return new MultipartResponse<>(header, payload);
    }
}
