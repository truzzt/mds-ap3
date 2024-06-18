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

package com.truzzt.extension.logginghouse.client.messages;

import com.truzzt.extension.logginghouse.client.ids.jsonld.JsonLd;
import com.truzzt.extension.logginghouse.client.ids.multipart.CalendarUtil;
import com.truzzt.extension.logginghouse.client.ids.multipart.IdsConstants;
import com.truzzt.extension.logginghouse.client.ids.multipart.IdsMultipartParts;
import com.truzzt.extension.logginghouse.client.ids.multipart.MultipartResponse;
import com.truzzt.extension.logginghouse.client.ids.multipart.MultipartSenderDelegate;
import com.truzzt.extension.logginghouse.client.ids.multipart.ResponseUtil;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageImpl;
import de.fraunhofer.iais.eis.RequestMessageBuilder;
import org.json.JSONObject;

import java.util.List;

public class CreateProcessMessageSender implements MultipartSenderDelegate<CreateProcessMessage, String> {

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
    public MultipartResponse<String> getResponseContent(IdsMultipartParts parts) throws Exception {
        return ResponseUtil.parseMultipartStringResponse(parts, JsonLd.getObjectMapper());
    }

    @Override
    public List<Class<? extends Message>> getAllowedResponseTypes() {
        return List.of(MessageProcessedNotificationMessageImpl.class);
    }

    @Override
    public Class<CreateProcessMessage> getMessageType() {
        return CreateProcessMessage.class;
    }
}
