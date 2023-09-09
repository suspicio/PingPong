package com.PingPong.lab1.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class JsonParser {
    public static JsonNode fromListOfJsonToJson(List<JsonNode> jsonNodeList) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNodes = mapper.createObjectNode();
        ArrayNode arrayNode = jsonNodes.putArray("configs");
        jsonNodeList.forEach(arrayNode::add);
        return jsonNodes;
    }
}
