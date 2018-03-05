/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.core.entity;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.DebugContext;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * TesterinaFunction entity class.
 */
public class TesterinaFunction {

    private String name;
    private Type type;
    private FunctionInfo bFunction;
    private ProgramFile programFile;
    private boolean runTest = true;

    // Annotation info
    private List<String> groups = new ArrayList<>();
    private List<String[]> valueSet = new ArrayList<>();

    static final String PREFIX_TEST = "TEST";
    static final String PREFIX_BEFORETEST = "BEFORETEST";
    static final String PREFIX_AFTERTEST = "AFTERTEST";
    static final String INIT_SUFFIX = ".<INIT>";

    /**
     * Prefixes for the test function names.
     */
    public enum Type {
        TEST(PREFIX_TEST), BEFORE_TEST(PREFIX_BEFORETEST), AFTER_TEST(PREFIX_AFTERTEST);

        String prefix;

        Type(String prefix) {
            this.prefix = prefix;
        }

        @Override public String toString() {
            return prefix;
        }
    }

    public TesterinaFunction(ProgramFile programFile, FunctionInfo bFunction, Type type) {
        this.name = bFunction.getName();
        this.type = type;
        this.bFunction = bFunction;
        this.programFile = programFile;
    }

    public void invoke() throws BallerinaException {
        invoke(new BValue[] {});
    }

    /**
     * Invokes a ballerina test function, in blocking mode.
     *
     * @param args function arguments
     */
    public void invoke(BValue[] args) {
        Context ctx = new Context(programFile);
        Debugger debugger = new Debugger(programFile);
        initDebugger(ctx, debugger);
        BLangFunctions.invokeFunction(programFile, bFunction, args, ctx);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String[]> getValueSet() {
        return valueSet;
    }

    public void setValueSet(List<String[]> valueSet) {
        this.valueSet = valueSet;
    }

    public boolean getRunTest() {
        return runTest;
    }

    public void setRunTest() {
        this.runTest = false;
    }

    private static void initDebugger(Context bContext, Debugger debugger) {
        bContext.getProgramFile().setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            DebugContext debugContext = new DebugContext();
            bContext.setDebugContext(debugContext);
            debugger.init();
            debugger.addDebugContextAndWait(debugContext);
        }
    }
}
