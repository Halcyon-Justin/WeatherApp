package com.weather.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.weather.app.services.WeatherService;
import com.weather.app.models.GeocodeData;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/{zipCode}")
    public ResponseEntity<GeocodeData> getWeather(@PathVariable String zipCode) {
        // Call the WeatherService to get GeocodeData
        GeocodeData geocodeData = weatherService.zipToGeoCode(zipCode);
                System.out.println("GeocodeData: " + geocodeData.toString());

        if (geocodeData != null) {
            // Return the GeocodeData as JSON with a 200 OK status code
            return ResponseEntity.ok(geocodeData);
        } else {
            // Handle the case when the response is empty or lacks expected data
            return ResponseEntity.notFound().build();
        }
    }
}