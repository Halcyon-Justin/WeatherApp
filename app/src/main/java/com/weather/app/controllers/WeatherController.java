package com.weather.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weather.app.exception.WeatherServiceException;
import com.weather.app.response.ResponseHandler;
import com.weather.app.services.WeatherService;


@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/{zipCode}")
    public ResponseEntity<Object> getWeather(@PathVariable String zipCode) {
        try {
            return ResponseHandler.responseBuilder("Request was sent and data has returned", HttpStatus.FOUND, weatherService.getWeeklyWeather(zipCode));
        } catch (WeatherServiceException e) {
            throw new WeatherServiceException("Weather Service Exception");
        }
    }
}