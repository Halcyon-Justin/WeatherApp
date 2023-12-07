package com.weather.app.services;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weather.app.exception.WeatherServiceException;
import com.weather.app.models.GeocodeData;
import com.weather.app.utils.JacksonUtils;

@Service
public class WeatherService {

    public static final String GOOGLE_API_URL = null;

    @Value("${weather.api.key}")
    String apiKey;

    RestTemplate restTemplate = new RestTemplate();

    public JsonNode getWeeklyWeather(String zipCode) {
        try {
            // Initialize GeocodeData empty object
            GeocodeData geoData = zipToGeoCode(zipCode);
            // Make 2nd call, using populated GeocodeData lat and long. Populate GridId.
            geoData = getGridId(geoData);
            // Make 3rd call, passing in populated lat and long and GridId
            JsonNode weatherJson = getHourlyWeather(geoData);
            return sevenDayHighLows(weatherJson);

        } catch (WeatherServiceException ex) {
            return JacksonUtils.createErrorNode(ex.getMessage());
        } catch (HttpClientErrorException ex) {
            return JacksonUtils.createErrorNode("External API request failed: " + ex.getStatusText());
        } catch (Exception ex) {
            return JacksonUtils.createErrorNode("An unexpected error occurred");
        }

    }

    public GeocodeData zipToGeoCode(String zip) {

        GeocodeData geoData = GeocodeData.builder().build();

        // Construct the URL with the provided zip code and API key
        String googleApiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + zip + "&key=" + apiKey;

        // TODO: REMOVE THIS LINE BEFORE PUBLICH APP
        System.out.println(googleApiUrl);

        try {
            // Make the HTTP request to the Google Geocoding API
            JsonNode response = restTemplate.getForObject(googleApiUrl, JsonNode.class);

            if (response == null) {
                throw new WeatherServiceException("API was hit but no data was returned");
            }

            // Check the status in the JSON response
            String status = response.get("status").asText();

            // Check if the status is not equal to "ZERO_RESULTS"
            if ("ZERO_RESULTS".equals(status)) {
                throw new WeatherServiceException("Returned no Usable Data");
            }
            // Extract 'lat' and 'lng' from the JSON response
            JsonNode locationNode = response.at("/results/0/geometry/location");
            double lat = locationNode.get("lat").asDouble();
            double lng = locationNode.get("lng").asDouble();

            geoData.setLat(lat);
            geoData.setLng(lng);

            return geoData;
        } catch (WeatherServiceException e) {
            throw new WeatherServiceException("Could not access weather API and grab Lat and Long values.");
        }

    }

    public GeocodeData getGridId(GeocodeData geoData) {

        String lat = geoData.getLat() + "";
        String lng = geoData.getLng() + "";
        String gridIdApiCall = "https://api.weather.gov/points/" + lat + "," + lng;

        try {
            JsonNode response = restTemplate.getForObject(gridIdApiCall, JsonNode.class);
            if (response != null) {
                JsonNode gridIdNode = response.at("/properties");
                if (gridIdNode != null) {
                    String gridId = gridIdNode.get("gridId").asText();
                    geoData.setGridId(gridId);
                    return geoData;
                } else {
                    throw new WeatherServiceException("Made API call, got a response, but no usable values returned.");
                }
            } else {
                throw new WeatherServiceException("API returned no Data.");
            }
        } catch (WeatherServiceException e) {
            throw new WeatherServiceException("Error getting Grid ID");
        }

    }

    public JsonNode getHourlyWeather(GeocodeData geoData) {

        String gridId = geoData.getGridId();
        int lat = Math.abs((int) geoData.getLat());
        int lng = Math.abs((int) geoData.getLng());

        try {
            String forecastApi = "https://api.weather.gov/gridpoints/" + gridId + "/" + lat + "," + lng + "/forecast/hourly";
            JsonNode response = restTemplate.getForObject(forecastApi, JsonNode.class);
            verifyNotNullData(response);
            return response;
        } catch (WeatherServiceException e) {
            throw new WeatherServiceException("Error getting hourly weather data");
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

    private void verifyNotNullData(JsonNode node) {
        if (node == null) {
            throw new WeatherServiceException("API was hit but no data was returned");
        }
    }
}