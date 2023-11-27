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
    void zipToGeoCode_HappyPath() throws IOException, WeatherServiceException {
        // Load the JSON response from the file
        JsonNode mockResponse = parseJsonForMock("ZipToGeoCodeHappyPath.json");

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
    void getGridId_HappyPath() throws IOException, WeatherServiceException {
        //Given

        GeocodeData geo = GeocodeData.builder()
        .lat(40.0005378)
        .lng(-105.20777980)
        .build();

        JsonNode mockResponse = parseJsonForMock("GetGridIdHappyPath.json");

        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
        .thenReturn(mockResponse);

        //When
        GeocodeData result = weatherService.getGridId((geo));

        //Then
        verify(restTemplate).getForObject("https://api.weather.gov/points/40.0005378,-105.2077798", JsonNode.class);

        assertEquals("MPX", result.getGridId());
    }

    @Test
    public void getHourlyWeather_HappyPath() throws IOException, WeatherServiceException {
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
        verify(restTemplate).getForObject("https://api.weather.gov/gridpoints/MPX/40,105/forecast/hourly"
         ,JsonNode.class);

         assertEquals(mockResponse, result);
    }

    @Test
    public void sevenDayHighLows_HappyPath() throws IOException {
        // Given
        JsonNode mockHourlyWeatherResponse = parseJsonForMock("HourlyWeatherHappyPath.json");
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
                .thenReturn(mockHourlyWeatherResponse);

        // When
        JsonNode result = weatherService.sevenDayHighLows(mockHourlyWeatherResponse);

        // Then
        assertNotNull(result);

        // Add more assertions based on your specific JSON structure
        // For example, if you know the structure of your JSON, you can verify specific values
        // Here, we're assuming a structure where each day has "day", "highTemperature", and "lowTemperature"
        assertEquals("2023-11-20", result.get("highLows").get(0).get("day").asText());
        assertEquals(22, result.get("highLows").get(0).get("highTemperature").asInt());
        assertEquals(16, result.get("highLows").get(0).get("lowTemperature").asInt());
    }

    @Test
    public void getWeeklyWeather_HappyPath() throws IOException, WeatherServiceException {
        // Given
        JsonNode mockZipToGeoCodeResponse = parseJsonForMock("ZipToGeoCodeHappyPath.json");
        when(restTemplate.getForObject(eq("https://maps.googleapis.com/maps/api/geocode/json?address=80303&key=dummyApiKey"), eq(JsonNode.class)))
                .thenReturn(mockZipToGeoCodeResponse);
        
        JsonNode mockGetGridIdResponse = parseJsonForMock("GetGridIdHappyPath.json");
        when(restTemplate.getForObject(eq("https://api.weather.gov/points/40.0005378,-105.2077798"), eq(JsonNode.class)))
                .thenReturn(mockGetGridIdResponse);
        
        JsonNode mockHourlyWeatherResponse = parseJsonForMock("HourlyWeatherHappyPath.json");
        when(restTemplate.getForObject(eq("https://api.weather.gov/gridpoints/MPX/40,105/forecast/hourly"), eq(JsonNode.class)))
                .thenReturn(mockHourlyWeatherResponse);

        // When
        JsonNode result = weatherService.getWeeklyWeather("80303");

        // Then
        assertNotNull(result);
        assertEquals("2023-11-20", result.get("highLows").get(0).get("day").asText());
        assertEquals(22, result.get("highLows").get(0).get("highTemperature").asInt());
        assertEquals(16, result.get("highLows").get(0).get("lowTemperature").asInt());

        verify(restTemplate).getForObject("https://maps.googleapis.com/maps/api/geocode/json?address=80303&key=dummyApiKey", JsonNode.class);
        verify(restTemplate).getForObject("https://api.weather.gov/points/40.0005378,-105.2077798", JsonNode.class);
        verify(restTemplate).getForObject("https://api.weather.gov/gridpoints/MPX/40,105/forecast/hourly", JsonNode.class);
    }


    //Helper method
    JsonNode parseJsonForMock(String jsonFile) throws IOException {
        Resource resource = new ClassPathResource(jsonFile);
        String jsonResponse = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        JsonNode mockResponse = new ObjectMapper().readTree(jsonResponse);
        return mockResponse;
    }

}