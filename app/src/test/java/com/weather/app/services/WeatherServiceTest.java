package com.weather.app.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.app.models.GeocodeData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherService.apiKey = "dummyApiKey";
            ReflectionTestUtils.setField(weatherService, "restTemplate", restTemplate);
    }

    @Test
    void zipToGeoCode_HappyPath() throws IOException {
        // Load the JSON response from the file
        Resource resource = new ClassPathResource("ZipToGeoCodeHappyPath.json");
        String jsonResponse = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        JsonNode mockResponse = new ObjectMapper().readTree(jsonResponse);

        // Mock the response from the RestTemplate
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
        .thenReturn(mockResponse);

        // Call the method being tested
        GeocodeData result = weatherService.zipToGeoCode("80303");

        // Verify that the restTemplate was called with the correct URL
       verify(restTemplate).getForObject("https://maps.googleapis.com/maps/api/geocode/json?address=80303&key=dummyApiKey", JsonNode.class);

        // Verify that the result is not null and contains the expected latitude and longitude
        assertNotNull(result);
        assertEquals(40.0005378, result.getLat());
        assertEquals(-105.2077798, result.getLng());
    }

    @Test
    void getGridId_HappyPath() throws IOException {
        //Given

        GeocodeData geo = GeocodeData.builder()
        .lat(40.0005378)
        .lng(-105.20777980)
        .build();

        Resource resource = new ClassPathResource("GetGridIdHappyPath.json");
        String jsonResponse = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        JsonNode mockResponse = new ObjectMapper().readTree(jsonResponse);

        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
        .thenReturn(mockResponse);

        //When
        GeocodeData result = weatherService.getGridId((geo));

        //Then
        verify(restTemplate).getForObject("https://api.weather.gov/points/40.0005378,-105.2077798", JsonNode.class);

        assertEquals("MPX", result.getGridId());
    }

    @Test
    public void getHourlyWeather_HappyPath() throws IOException {
           //Given
        GeocodeData geo = GeocodeData.builder().lat(40.0005378).lng(-105.2077798).gridId("MPX").build();
        geo.setLat(40.0005378);
        geo.setLng(-105.2077798);
        geo.setGridId("MPX");

        JsonNode mockResponse = parseJsonForMock("WeeklyWeatherHappyPath.json");

        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
        .thenReturn(mockResponse);

        //WHEN
        JsonNode result = weatherService.getHourlyWeather(geo);

        //THEN
        verify(restTemplate).getForObject("https://api.weather.gov/gridpoints/MPX,40.0005378-105.2077798/forecast/hourly"
         ,JsonNode.class);

         assertEquals(mockResponse, result);

    }



    JsonNode parseJsonForMock(String jsonFile) throws IOException {
        Resource resource = new ClassPathResource(jsonFile);
        String jsonResponse = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        JsonNode mockResponse = new ObjectMapper().readTree(jsonResponse);
        return mockResponse;
    }

}