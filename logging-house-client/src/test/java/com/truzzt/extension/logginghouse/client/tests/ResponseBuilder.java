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

package com.truzzt.extension.logginghouse.client.tests;

import com.truzzt.extension.logginghouse.client.multipart.ids.multipart.CalendarUtil;
import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessage;
import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessageStatus;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.edc.protocol.dsp.spi.types.HttpMessageProtocol;
import org.json.JSONObject;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.*;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getRandomLong;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getRandomUuid;

public class ResponseBuilder {

    public static LoggingHouseMessage.Builder getLoggingHouseMessageBuilder(String eventType, String eventToLog, boolean createProcess) {
        return LoggingHouseMessage.Builder.newInstance()
                .id(getRandomLong())
                .eventType(eventType)
                .eventId(getRandomUuid())
                .eventToLog(eventToLog)
                .status(LoggingHouseMessageStatus.PENDING)
                .createProcess(createProcess)
                .processId(getRandomUuid())
                .consumerId(CONSUMER_PARTICIPANT_ID)
                .providerId(PROVIDER_PARTICIPANT_ID)
                .createdAt(ZonedDateTime.now());
    }

    public static LoggingHouseMessage buildLoggingHouseMessage(String eventType, String eventToLog, boolean createProcess) {
        return getLoggingHouseMessageBuilder(eventType, eventToLog, createProcess)
                .build();
    }

    public static JSONObject buildContractAgreementAsJSON(String assetId) {
        var json = new JSONObject();

        var policy = Policy.Builder.newInstance().target(assetId).type(PolicyType.SET).build();

        json.put("Timestamp", CalendarUtil.gregorianNow().toString());
        json.put("ConnectorId", CONSUMER_PARTICIPANT_ID);

        json.put("AssetId", assetId);
        json.put("AssetName", ASSET_NAME);
        json.put("AssetDescription", ASSET_DESCRIPTION);
        json.put("AssetVersion", ASSET_VERSION);
        json.put("AssetContentType", ASSET_CONTENT_TYPE);
        json.put("AssetProperties", ASSET_PROPERTIES);

        json.put("ContractAgreementId", UUID.randomUUID().toString());
        json.put("ContractProviderId", PROVIDER_PARTICIPANT_ID);
        json.put("ContractConsumerId", CONSUMER_PARTICIPANT_ID);
        json.put("ContractSigningDate", CalendarUtil.gregorianNow().toString());
        json.put("ContractPolicy", policy);

        return json;
    }

    public static JSONObject buildInitialTransferProcessAsJSON(String assetId, String contractAgreementId) {
        var json = new JSONObject();

        json.put("Timestamp", CalendarUtil.gregorianNow().toString());
        json.put("ConnectorId", CONSUMER_PARTICIPANT_ID);

        json.put("TransferProcessId", UUID.randomUUID().toString());
        json.put("TransferState", TransferProcessStates.INITIAL);
        json.put("TransferProtocol", HttpMessageProtocol.DATASPACE_PROTOCOL_HTTP);
        json.put("TransferContractId", contractAgreementId);
        json.put("TransferConnectorId", PROVIDER_PARTICIPANT_ID);
        json.put("TransferAssetId", assetId);

        return json;
    }
}
