package com.weather.app.services;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weather.app.models.GeocodeData;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private RestTemplate restTemplate = new RestTemplate();

    public JsonNode getWeeklyWeather(String zipCode) {
        // Initialize GeocodeData empty object
        GeocodeData geoData = new GeocodeData();

        geoData = zipToGeoCode(zipCode, geoData);

        // Make 2nd call, using populated GeocodeData lat and long. Populate GridId.
        geoData = getGridId(geoData);

        // Make 3rd call, passing in populated lat and long and GridId
        JsonNode weatherJson = getHourlyWeather(geoData);

        JsonNode sevenDayForecast = sevenDayHighLows(weatherJson);

        return sevenDayForecast;

    }

    private GeocodeData zipToGeoCode(String zip, GeocodeData geoData) {
        // Construct the URL with the provided zip code and API key
        String googleApiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + zip + "&key=" + apiKey;

        try {
            // Make the HTTP request to the Google Geocoding API
            JsonNode response = restTemplate.getForObject(googleApiUrl, JsonNode.class);

            if (response != null) {
                // Extract 'lat' and 'lng' from the JSON response
                JsonNode locationNode = response.at("/results/0/geometry/location");
                double lat = locationNode.get("lat").asDouble();
                double lng = locationNode.get("lng").asDouble();

                geoData.setLat(lat);
                geoData.setLng(lng);

                return geoData;
            } else {
                return null; // Handle the case when the response is empty or lacks expected data
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle or log the error
            return null;
        }
    }

    private GeocodeData getGridId(GeocodeData geoData) {

        String gridId;
        String lat = geoData.getLat() + "";
        String lng = geoData.getLng() + "";
        String gridIdApiCall = "https://api.weather.gov/points/" + lat + "," + lng;

        try {
            // Make the HTTP request to the Open Weather API
            JsonNode response = restTemplate.getForObject(gridIdApiCall, JsonNode.class);

            if (response != null) {
                // Grab gridId from repsonse object
                JsonNode gridIdNode = response.at("/properties");
                gridId = gridIdNode.get("gridId").asText();
                geoData.setGridId(gridId);

                return geoData;
            } else {
                return null; // Handle the case when the response is empty or lacks expected data
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle or log the error
            return null;
        }
    }

    private JsonNode getHourlyWeather(GeocodeData geoData) {

        String gridId = geoData.getGridId();
        int lat = Math.abs((int) geoData.getLat());
        int lng = Math.abs((int) geoData.getLng());

        String forecastApi = "https://api.weather.gov/gridpoints/" + gridId + "/" + lat + "," + lng
                + "/forecast/hourly";

        try {
            // Make the HTTP request to the Open Weather API
            JsonNode response = restTemplate.getForObject(forecastApi, JsonNode.class);

            if (response != null) {
                // Grab gridId from repsonse object
                JsonNode weatherNode = response;

                return weatherNode;
            } else {
                return null; // Handle the case when the response is empty or lacks expected data
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle or log the error
            return null;
        }
    }

    public JsonNode sevenDayHighLows(JsonNode hourlyWeatherData) {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        Map<String, List<Integer>> dayTemperatures = StreamSupport.stream(
                hourlyWeatherData.get("properties").get("periods").spliterator(), false)
                .map(period -> new AbstractMap.SimpleEntry<>(
                        period.get("startTime").asText().substring(0, 10),
                        period.get("temperature").asInt()))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        ArrayNode highLows = dayTemperatures.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> {
                    int maxTemp = Collections.max(entry.getValue());
                    int minTemp = Collections.min(entry.getValue());

                    ObjectNode dayHighLow = factory.objectNode();
                    dayHighLow.put("day", entry.getKey());
                    dayHighLow.put("highTemperature", maxTemp);
                    dayHighLow.put("lowTemperature", minTemp);

                    return dayHighLow;
                })
                .collect(factory::arrayNode, ArrayNode::add, ArrayNode::addAll);

        return factory.objectNode().set("highLows", highLows);
    }

}