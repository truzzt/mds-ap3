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

import com.truzzt.extension.logginghouse.client.spi.types.LoggingHouseMessage;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.transfer.spi.types.DataRequest;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.edc.protocol.dsp.spi.types.HttpMessageProtocol;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.HttpDataAddress;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.CONSUMER_PARTICIPANT_ID;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.COUNTER_PARTY_ADDRESS;
import static com.truzzt.extension.logginghouse.client.tests.TestsConstants.PROVIDER_PARTICIPANT_ID;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getRandomLong;
import static com.truzzt.extension.logginghouse.client.tests.TestsHelper.getRandomUUID;

public class ResponseBuilder {

    public static LoggingHouseMessage.Builder getLoggingHouseMessageBuilder(Class<?> eventType, Object eventToLog, boolean createProcess) {
        return LoggingHouseMessage.Builder.newInstance()
                .id(getRandomLong())
                .eventType(eventType)
                .eventId(getRandomUUID())
                .eventToLog(eventToLog)
                .createProcess(createProcess)
                .processId(getRandomUUID())
                .consumerId(CONSUMER_PARTICIPANT_ID)
                .providerId(PROVIDER_PARTICIPANT_ID)
                .createdAt(ZonedDateTime.now());
    }

    public static LoggingHouseMessage buildLoggingHouseMessage(Class<?> eventType, Object eventToLog, boolean createProcess) {
        return getLoggingHouseMessageBuilder(eventType, eventToLog, createProcess)
                .build();
    }

    public static ContractAgreement buildCContractAgreement(String assetId) {

        var policy = Policy.Builder.newInstance()
                .target(assetId)
                .type(PolicyType.SET)
                .build();

        return ContractAgreement.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .providerId(PROVIDER_PARTICIPANT_ID)
                .consumerId(CONSUMER_PARTICIPANT_ID)
                .contractSigningDate(ZonedDateTime.now().getNano())
                .assetId(assetId)
                .policy(policy)
                .build();
    }

    public static TransferProcess buildInitialTransferProcess(String assetId, String contractAgreementId) {

        var dataDestination = DataAddress.Builder.newInstance()
                .type(HttpDataAddress.HTTP_DATA)
                .property("baseUrl", " http://localhost:4000/api/consumer/store")
                .build();

        var dataRequest = DataRequest.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .processId(UUID.randomUUID().toString())
                .connectorAddress(COUNTER_PARTY_ADDRESS)
                .protocol(HttpMessageProtocol.DATASPACE_PROTOCOL_HTTP)
                .connectorId(PROVIDER_PARTICIPANT_ID)
                .assetId(assetId)
                .contractId(contractAgreementId)
                .dataDestination(dataDestination)
                .build();

        return TransferProcess.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .createdAt(ZonedDateTime.now().getNano())
                .type(TransferProcess.Type.PROVIDER)
                .state(TransferProcessStates.INITIAL.code())
                .stateCount(1)
                .stateTimestamp(ZonedDateTime.now().getNano())
                .updatedAt(ZonedDateTime.now().getNano())
                .dataRequest(dataRequest)
                .build();
    }
}
