/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.test;

import org.ballerinalang.testerina.core.BTestRunner;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is responsible of testing the ballerina samples
 */
public class TesterinaSamplesTest {

    private final String userDir = System.getProperty("user.dir");

    @BeforeClass
    public void setUserDir() {
        // This is comming from the pom
        String testerinaRoot = System.getProperty("testerina.root");
        System.setProperty("user.dir", testerinaRoot + "/samples");
    }

    // /samples/functionTest
    @Test
    public void functionTestSampleTest() {
        cleanup();
        BTestRunner runner = new BTestRunner();
        runner.runTest(new Path[] { Paths.get("functionTest") }, new ArrayList<>());
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary("functionTest", "passed"), 6);
    }

    // /samples/features/assertions.bal
    @Test
    public void assertSampleTest() {
        cleanup();
        BTestRunner runner = new BTestRunner();
        runner.runTest(new Path[] { Paths.get("features/assertions.bal") }, new ArrayList<>());
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "passed"), 14);
    }

    // /samples/features/assertions.bal
    @Test
    public void dataProviderSampleTest() {
        cleanup();
        BTestRunner runner = new BTestRunner();
        runner.runTest(new Path[] { Paths.get("features/data-providers.bal") }, new ArrayList<>());
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "passed"), 4);
    }

    private void cleanup() {
        TesterinaRegistry.getInstance().setProgramFiles(new ArrayList<>());
        TesterinaRegistry.getInstance().setTestSuites(new HashMap<>());
    }

    @AfterClass
    public void resetUserDir() {
        System.setProperty("user.dir", userDir);
    }
}
