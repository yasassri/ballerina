/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.testerina.test;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.test package.
 */
public class AssertTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("assert-test/assert-test.bal");
    }

    @Test
    public void testAssertIntEquals() {
        BValue[] args = {new BInteger(3), new BInteger(5)};
        BRunUtil.invoke(compileResult, "testAssertIntEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*intAdd function " +
            "failed : expected 8 but found 6.*")
    public void testAssertIntEqualsFail() {
        BValue[] args = {new BInteger(3), new BInteger(3)};
        BRunUtil.invoke(compileResult, "testAssertIntEquals", args);
    }

    @Test
    public void testAssertFloatEquals() {
        BValue[] args = {new BFloat(10.000), new BFloat(20.050)};
        BRunUtil.invoke(compileResult, "testAssertFloatEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*floatAdd function " +
            "failed : expected 30.05 but found 21.05.*")
    public void testAssertFloatEqualsFail() {
        BValue[] args = {new BFloat(1), new BFloat(20.050)};
        BRunUtil.invoke(compileResult, "testAssertFloatEquals", args);
    }

    @Test
    public void testAssertStringEquals() {
        BValue[] args = {new BString("John"), new BString("Doe")};
        BRunUtil.invoke(compileResult, "testAssertStringEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*string concatenation" +
            " failed : expected JohnDoe but found DoeJohn.*")
    public void testAssertStringEqualsFail() {
        BValue[] args = {new BString("Doe"), new BString("John")};
        BRunUtil.invoke(compileResult, "testAssertStringEquals", args);
    }

    @Test
    public void testAssertTrue() {
        BValue[] args = {new BBoolean(true)};
        BRunUtil.invoke(compileResult, "testAssertTrue", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*assertTrue failed.*")
    public void testAssertTrueFail() {
        BValue[] args = {new BBoolean(false)};
        BRunUtil.invoke(compileResult, "testAssertTrue", args);
    }

    @Test
    public void testAssertFalse() {
        BValue[] args = {new BBoolean(false)};
        BRunUtil.invoke(compileResult, "testAssertFalse", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*assertFalse failed.*")
    public void testAssertFalseFail() {
        BValue[] args = {new BBoolean(true)};
        BRunUtil.invoke(compileResult, "testAssertFalse", args);
    }

    @Test
    public void testAssertBooleanEquals() {
        BValue[] args = {new BBoolean(false), new BBoolean(false)};
        BRunUtil.invoke(compileResult, "testAssertBooleanEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*assertBooleanEquals" +
            " failed : expected false but found true.*")
    public void testAssertBooleanEqualsFail() {
        BValue[] args = {new BBoolean(true), new BBoolean(false)};
        BRunUtil.invoke(compileResult, "testAssertBooleanEquals", args);
    }

    @Test
    public void testNoAssertFail() {
        BValue[] args = {new BBoolean(true)};
        BRunUtil.invoke(compileResult, "testAssertFail", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*assertFailed.*")
    public void testAssertFail() {
        BValue[] args = {new BBoolean(false)};
        BRunUtil.invoke(compileResult, "testAssertFail", args);
    }

    @Test
    public void testAssertStringArrayEquals0() {
        BValue[] args = {new BInteger(0)};
        BRunUtil.invoke(compileResult, "testAssertStringArrayEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*failed : expected \\[\"A\", \"B\", \"C\"\\] but found \\[\"A\", \"B\"," +
                  " \"C\", \"D\"\\].*")
    public void testAssertStringArrayEquals1() {
        BValue[] args = {new BInteger(1)};
        BRunUtil.invoke(compileResult, "testAssertStringArrayEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*failed : expected \\[\"A\", \"b\", \"C\"\\] but found \\[\"A\", \"B\"," +
                  " \"C\"\\].*")
    public void testAssertStringArrayEquals2() {
        BValue[] args = {new BInteger(2)};
        BRunUtil.invoke(compileResult, "testAssertStringArrayEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*failed : expected " +
            "\\[\"A\", \"b\", \"C\"\\] but found \\[\"A\", \"B\", \"C\"\\].*")
    public void testAssertStringArrayEquals3() {
        BValue[] args = {new BInteger(3)};
        BRunUtil.invoke(compileResult, "testAssertStringArrayEquals", args);
    }

    @Test
    public void testAssertIntArrayEquals0() {
        BValue[] args = {new BInteger(0)};
        BRunUtil.invoke(compileResult, "testAssertIntArrayEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*failed : expected \\[1, 2, 3\\] but found \\[1, 2, 3, 4\\].*")
    public void testAssertIntArrayEquals1() {
        BValue[] args = {new BInteger(1)};
        BRunUtil.invoke(compileResult, "testAssertIntArrayEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*failed : expected \\[1, 5, 3\\] but found \\[1, 2, 3\\].*")
    public void testAssertIntArrayEquals2() {
        BValue[] args = {new BInteger(2)};
        BRunUtil.invoke(compileResult, "testAssertIntArrayEquals", args);
    }

    @Test
    public void testAssertFloatArrayEquals0() {
        BValue[] args = {new BInteger(0)};
        BRunUtil.invoke(compileResult, "testAssertFloatArrayEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*failed : expected \\[1.1, 2.2, 3.3\\] but found \\[1.1, 2.2, 3.3, " +
                  "4.4\\].*")
    public void testAssertFloatArrayEquals1() {
        BValue[] args = {new BInteger(1)};
        BRunUtil.invoke(compileResult, "testAssertFloatArrayEquals", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*failed : expected \\[1.1, 2.22, 3.3\\] but found \\[1.1, 2.2, 3.3\\].*")
    public void testAssertFloatArrayEquals2() {
        BValue[] args = {new BInteger(2)};
        BRunUtil.invoke(compileResult, "testAssertFloatArrayEquals", args);
    }
}
