package com.weather.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.weather.app.models.GeocodeData;
import com.fasterxml.jackson.databind.JsonNode; // Import Jackson JsonNode

@Service
public class WeatherService {
    //@Value("${google.maps.api.key}")

    @Value("${weather.api.key}")
    private String apiKey;

    private RestTemplate restTemplate = new RestTemplate();

    public GeocodeData zipToGeoCode(String zip) {
        // Construct the URL with the provided zip code and API key
        String apiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + zip + "&key=" + apiKey;

        try {
            // Make the HTTP request to the Google Geocoding API
            JsonNode response = restTemplate.getForObject(apiUrl, JsonNode.class);

            if (response != null) {
                // Extract 'lat' and 'lng' from the JSON response
                JsonNode locationNode = response.at("/results/0/geometry/location");
                double lat = locationNode.get("lat").asDouble();
                double lng = locationNode.get("lng").asDouble();

                // Create a new GeocodeData instance with the extracted values
                GeocodeData geocodeData = new GeocodeData();
                geocodeData.setLat(lat);
                geocodeData.setLng(lng);

                return geocodeData;
            } else {
                return null; // Handle the case when the response is empty or lacks expected data
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle or log the error
            return null;
        }
    }
}