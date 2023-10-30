
package com.weather.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {



    @GetMapping
    public ResponseEntity<String> getWeather(@RequestParam String apiKey) {
        // Create a JSON response with the string "hi"
        String responseString = "hi";

        // Return the response with a 200 OK status code
        return ResponseEntity.ok(responseString);
    }
}