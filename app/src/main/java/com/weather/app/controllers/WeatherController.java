package com.weather.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.weather.app.exception.WeatherDataException;
import com.weather.app.services.WeatherService;
import com.weather.app.services.WeatherServiceException;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/{zipCode}")
    public ResponseEntity<JsonNode> getWeather(@PathVariable String zipCode) throws Exception {
        try {
            JsonNode weather = weatherService.getWeeklyWeather(zipCode);
            return ResponseEntity.ok(weather);
        } catch (WeatherServiceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}