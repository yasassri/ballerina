package src.test.resources.servicemocktest2;

import ballerina.net.http;

public function hadleGetEvents () (http:Response res) {

    // Here we need to call the Event Service
    res = {};
    // Need to improve to handle error case
    json j = getEvents();
    res.setJsonPayload(j);
    return;
}
