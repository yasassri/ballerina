import ballerina.test;
import ballerina.log;

@test:config{
    disabled:false
}
function testFunc3 () {
    log:printInfo("This is a Test Info log");
    log:printError("This is a Test Error log");
    log:printWarn("This is a Test Warn log");
    test:assertFalse(false, "errorMessage");
}