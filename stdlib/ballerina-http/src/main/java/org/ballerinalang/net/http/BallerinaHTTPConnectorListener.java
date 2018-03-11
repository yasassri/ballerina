/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.http;

import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP connector listener for Ballerina.
 */
public class BallerinaHTTPConnectorListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaHTTPConnectorListener.class);
    private static final String HTTP_RESOURCE = "httpResource";

    private final HTTPServicesRegistry httpServicesRegistry;

    public BallerinaHTTPConnectorListener(HTTPServicesRegistry httpServicesRegistry) {
        this.httpServicesRegistry = httpServicesRegistry;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
        try {
            HttpResource httpResource;
            if (accessed(httpCarbonMessage)) {
                httpResource = (HttpResource) httpCarbonMessage.getProperty(HTTP_RESOURCE);
                extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
                return;
            }
            httpResource = HttpDispatcher.findResource(httpServicesRegistry, httpCarbonMessage);
            if (HttpDispatcher.isDiffered(httpResource)) {
                httpCarbonMessage.setProperty(HTTP_RESOURCE, httpResource);
                return;
            }
            extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
        } catch (BallerinaException ex) {
            HttpUtil.handleFailure(httpCarbonMessage, new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error in http server connector" + throwable.getMessage(), throwable);
    }

    private void extractPropertiesAndStartResourceExecution(HTTPCarbonMessage httpCarbonMessage,
                                                            HttpResource httpResource) {
        Map<String, Object> properties = collectRequestProperties(httpCarbonMessage);
        properties.put(HttpConstants.REMOTE_ADDRESS, httpCarbonMessage.getProperty(HttpConstants.REMOTE_ADDRESS));
        properties.put(HttpConstants.ORIGIN_HOST, httpCarbonMessage.getHeader(HttpConstants.ORIGIN_HOST));
        BValue[] signatureParams = HttpDispatcher.getSignatureParameters(httpResource, httpCarbonMessage);
        CallableUnitCallback callback = new HttpCallableUnitCallback(httpCarbonMessage);
        //TODO handle BallerinaConnectorException
        Executor.submit(httpResource.getBalResource(), callback, properties, signatureParams);
    }

    private boolean accessed(HTTPCarbonMessage httpCarbonMessage) {
        return httpCarbonMessage.getProperty(HTTP_RESOURCE) != null;
    }

    private Map<String, Object> collectRequestProperties(HTTPCarbonMessage httpCarbonMessage) {
        Map<String, Object> properties = new HashMap<>();
        if (httpCarbonMessage.getProperty(HttpConstants.SRC_HANDLER) != null) {
            Object srcHandler = httpCarbonMessage.getProperty(HttpConstants.SRC_HANDLER);
            properties.put(HttpConstants.SRC_HANDLER, srcHandler);
        }
        if (httpCarbonMessage.getHeader(HttpConstants.HEADER_X_XID) == null ||
                httpCarbonMessage.getHeader(HttpConstants.HEADER_X_REGISTER_AT_URL) == null) {
            return properties;
        }
        properties.put(Constants.GLOBAL_TRANSACTION_ID, httpCarbonMessage.getHeader(HttpConstants.HEADER_X_XID));
        properties.put(Constants.TRANSACTION_URL, httpCarbonMessage.getHeader(HttpConstants.HEADER_X_REGISTER_AT_URL));
        return properties;
    }
}
