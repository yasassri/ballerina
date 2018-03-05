import ballerina.test;
import ballerina.io;

int i = 0;
int j = 0;

function reset () {
    i=0;
}

function init () {
    i = 1;
}

function cleanup () {
    i = 0;
}

@test:config{
    before: "init"
}
function testBefore () {
    test:assertTrue(i == 1, "Expected i to be 1, but i = "+i);
    reset();
}

@test:config{
    before: "init", after: "cleanup"
}
function test1 () {
    test:assertTrue(i == 1, "Expected i to be 1, but i = "+i);
}

@test:config{
    dependsOn: ["test1"]
}
function testAfter () {
    test:assertTrue(i == 0, "Expected i to be 0, but i = "+i);
    reset();
}

@test:config{
    after: "cleanup"
}
function test2 () {
    reset();
    test:assertTrue(i == 0, "Expected i to be 1, but i = "+i);
}

@test:config{
    dependsOn: ["test2"]
}
function testAfterAlone () {
    test:assertTrue(i == 0, "Expected i to be 0, but i = "+i);
    reset();
}

@test:config{
}
function test3 () {
    j = j + 1;
}

@test:config{
}
function test4 () {
    j = j + 2;
}

@test:config{
    dependsOn: ["test3", "test4"]
}
function testDependsOn1 () {
    test:assertTrue(j == 3, "Expected j to be 3, but j = "+j);
}


