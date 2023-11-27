package com.weather.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WeatherDataExceptionHandler {
    
    //THIS CAN ALSO BE REFERRED TO AS A CONTROLLER

    @ExceptionHandler(value =  {WeatherDataNotFoundException.class})
    public ResponseEntity<Object> handleWeatherDataNotFoundException
    (WeatherDataNotFoundException weatherDataNotFoundException) 
    {
        WeatherDataException weatherDataException = new WeatherDataException(
            weatherDataNotFoundException.getMessage(), 
            weatherDataNotFoundException, 
            HttpStatus.NOT_FOUND
            );
            return new ResponseEntity<>(weatherDataException, HttpStatus.NOT_FOUND);
    }
}
