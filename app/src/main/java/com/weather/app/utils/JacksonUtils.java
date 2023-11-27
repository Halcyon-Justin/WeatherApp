package com.weather.app.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonUtils {

    public static JsonNode createErrorNode(String errorMessage) {
        ObjectNode errorNode = JsonNodeFactory.instance.objectNode();
        errorNode.put("error", errorMessage);
        return errorNode;
    }
    
}
