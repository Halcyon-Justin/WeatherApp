package com.weather.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.weather.app.services.WeatherService;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/{zipCode}")
    public ResponseEntity<JsonNode> getWeather(@PathVariable String zipCode) {
        // Call the WeatherService to get GeocodeData
        JsonNode weather = weatherService.getWeeklyWeather(zipCode);

        if (weather != null) {
            // Return the GeocodeData as JSON with a 200 OK status code
            return ResponseEntity.ok(weather);
        } else {
            // Handle the case when the response is empty or lacks expected data
            return ResponseEntity.notFound().build();
        }
    }
}