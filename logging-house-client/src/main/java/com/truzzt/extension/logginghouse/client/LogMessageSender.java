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

package com.truzzt.extension.logginghouse.client;

import com.truzzt.extension.logginghouse.client.ids.jsonld.JsonLd;
import com.truzzt.extension.logginghouse.client.ids.multipart.CalendarUtil;
import com.truzzt.extension.logginghouse.client.ids.multipart.IdsConstants;
import com.truzzt.extension.logginghouse.client.ids.multipart.IdsMultipartParts;
import com.truzzt.extension.logginghouse.client.ids.multipart.MultipartResponse;
import com.truzzt.extension.logginghouse.client.ids.multipart.MultipartSenderDelegate;
import com.truzzt.extension.logginghouse.client.ids.multipart.ResponseUtil;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageImpl;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.monitor.Monitor;
import org.json.JSONObject;

import java.util.List;

public class LogMessageSender implements MultipartSenderDelegate<LogMessage, String> {

    Monitor monitor;
    String connectorId;
    AssetIndex assetIndex;

    public LogMessageSender(Monitor monitor, AssetIndex assetIndex, String connectorId) {
        this.monitor = monitor;
        this.assetIndex = assetIndex;
        this.connectorId = connectorId;
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
        if (logMessage.eventToLog() instanceof ContractAgreement contractAgreement) {
            return buildContractAgreementPayload(contractAgreement);
        } else if (logMessage.eventToLog() instanceof TransferProcess transferProcess) {
            return buildTransferProcessPayload(transferProcess);
        } else {
            throw new EdcException(String.format("ObjectType %s not supported in LogMessageSender",
                    logMessage.eventToLog().getClass()));
        }
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
    public Class<LogMessage> getMessageType() {
        return LogMessage.class;
    }

    private String buildContractAgreementPayload(ContractAgreement contractAgreement) {
        assert contractAgreement != null;

        var jo = new JSONObject();

        jo.put("Timestamp", CalendarUtil.gregorianNow().toString());
        jo.put("ConnectorId", connectorId);

        // Check if connector is the provider
        if (contractAgreement.getProviderId().equals(connectorId)) {
            // In case of the provider, log asset information
            var asset = assetIndex.findById(contractAgreement.getAssetId());

            if (asset == null) {
                monitor.warning("Asset with id " + contractAgreement.getAssetId() + " not found in asset index.");
            } else {
                jo.put("AssetId", asset.getId());
                jo.put("AssetName", asset.getName());
                jo.put("AssetDescription", asset.getDescription());
                jo.put("AssetVersion", asset.getVersion());
                jo.put("AssetContentType", asset.getContentType());
                jo.put("AssetProperties", asset.getProperties());
            }
        }

        jo.put("ContractAgreementId", contractAgreement.getId());
        jo.put("ContractProviderId", contractAgreement.getProviderId());
        jo.put("ContractConsumerId", contractAgreement.getConsumerId());
        jo.put("ContractSigningDate", contractAgreement.getContractSigningDate());
        jo.put("ContractPolicy", contractAgreement.getPolicy());
        return jo.toString();
    }

    private String buildTransferProcessPayload(TransferProcess transferProcess) {
        var jo = new JSONObject();

        jo.put("Timestamp", CalendarUtil.gregorianNow().toString());
        jo.put("ConnectorId", connectorId);

        jo.put("TransferProcessId", transferProcess.getId());
        jo.put("TransferState", transferProcess.stateAsString());
        jo.put("TransferProtocol", transferProcess.getProtocol());
        jo.put("TransferContractId", transferProcess.getContractId());
        jo.put("TransferConnectorId", transferProcess.getConnectorId());
        jo.put("TransferAssetId", transferProcess.getAssetId());

        return jo.toString();
    }
}