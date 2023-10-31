package com.weather.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode; // Import Jackson JsonNode
import com.weather.app.models.GeocodeData;

@Service
public class WeatherService {
    // @Value("${google.maps.api.key}")

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

        return weatherJson;
        // Do any other necessary data manipulation, return final result to
        // WeatherController as JSON

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
                // We grab gridId from repsonse object
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

    public JsonNode getHourlyWeather(GeocodeData geoData) {
        
        String gridId = geoData.getGridId();
        int lat = Math.abs((int) geoData.getLat());
        int lng = Math.abs((int) geoData.getLng());
        
        String forecastApi = "https://api.weather.gov/gridpoints/" + gridId + "/" + lat + "," + lng + "/forecast/hourly";

        try {
            // Make the HTTP request to the Open Weather API
            JsonNode response = restTemplate.getForObject(forecastApi, JsonNode.class);

            if (response != null) {
                // We grab gridId from repsonse object
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
}