package a.b;

import c.d;

function testJsonToStruct() (d:Person, error) {
    json j = {"name":"John", "age":30, "adrs": {"street": "20 Palm Grove", "city":"Colombo 03", "country":"Sri Lanka"}};
    var p, e = <d:Person> j;
    return p, e;
}