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

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function ballerina.lo#readBytes.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "read",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "numberOfBytes", type = TypeKind.INT),
                @Argument(name = "offset", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.BLOB),
                @ReturnType(type = TypeKind.INT),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class Read extends BlockingNativeCallableUnit {

    /**
     * Specifies the index which holds the number of bytes in ballerina.lo#readBytes.
     */
    private static final int NUMBER_OF_BYTES_INDEX = 0;

    /**
     * Specifies the offset of the array to read bytes.
     */
    private static final int OFFSET_INDEX = 1;

    /**
     * Specifies the index which contains the byte channel in ballerina.lo#readBytes.
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    private static final Logger log = LoggerFactory.getLogger(Read.class);

    /*
     * Function which will be notified on the response obtained after the async operation.
     *
     * @param result context of the callback.
     * @return Once the callback is processed we further return back the result.
     */
    /*private static EventResult readResponse(EventResult<Integer, EventContext> result) {
        *//*
         * The async response should go here
         *//*
        BStruct errorStruct;
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        if (null != error) {
            errorStruct = IOUtils.createError(context, error.getMessage());
        }
        byte[] content = (byte[]) eventContext.getProperties().get(ReadBytesEvent.CONTENT_PROPERTY);
        Integer numberOfBytes = result.getResponse();
        return result;
    }*/

    /**
     * <p>
     * Reads bytes from a given channel.
     * </p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BBlob readByteBlob = null;
        BInteger numberOfReadBytes = null;
        BStruct errorStruct = null;
        try {
            BStruct channel = (BStruct) context.getRefArgument(BYTE_CHANNEL_INDEX);
            int numberOfBytes = (int) context.getIntArgument(NUMBER_OF_BYTES_INDEX);
            int offset = (int) context.getIntArgument(OFFSET_INDEX);
            Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            byte[] content = new byte[numberOfBytes];
            EventContext eventContext = new EventContext(context);
            int nBytes = IOUtils.readFull(byteChannel, content, offset, eventContext);
            numberOfReadBytes = new BInteger(nBytes);
            readByteBlob = new BBlob(content);
            //TODO When async function is available the following should be executed.
            //IOUtils.read(byteChannel,content,offset,eventContext,Read::readResponse);
        } catch (Throwable e) {
            String message = "Error occurred while reading bytes:" + e.getMessage();
            log.error(message);
            errorStruct = IOUtils.createError(context, message);
        }
        context.setReturnValues(readByteBlob, numberOfReadBytes, errorStruct);
    }
}
