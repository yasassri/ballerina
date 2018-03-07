import ballerina.net.http;
import ballerina.mime;

function testGetContentLength (http:InResponse res) (int) {
    int length = res.getContentLength();
    return length;
}

function testGetHeader (http:InResponse res, string key) (string) {
    string contentType = res.getHeader(key);
    return contentType;
}

function testGetHeaders (http:InResponse res, string key) (string[]) {
    return res.getHeaders(key);
}

function testGetJsonPayload (http:InResponse res) (json, mime:EntityError) {
    return res.getJsonPayload();
}

function testGetProperty (http:InResponse res, string propertyName) (string) {
    string payload = res.getProperty(propertyName);
    return payload;
}

function testGetStringPayload (http:InResponse res) (string, mime:EntityError) {
    return res.getStringPayload();
}

function testGetBinaryPayload (http:InResponse res) (blob, mime:EntityError) {
    return res.getBinaryPayload();
}

function testGetXmlPayload (http:InResponse res) (xml, mime:EntityError) {
    return res.getXmlPayload();
}

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/11"
    }
    resource echo1 (http:Connection conn, http:InRequest req) {
        http:InResponse res = {};
        _ = conn.forward(res);
    }
}
