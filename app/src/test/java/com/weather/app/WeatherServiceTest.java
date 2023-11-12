package com.weather.app;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

// import org.junit.jupiter.api.Test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.app.models.GeocodeData;
import com.weather.app.services.WeatherService;

@RunWith(PowerMockRunner.class) //Tells JUnit to use PowerMockRunner instead of JUnit. Powermock runner extends the JUnit runner.
@PrepareForTest(WeatherService.class) // Tells PowerMock to prepare the class for testing, allowing it to mock  static methods and perform reflection.
public class WeatherServiceTest {

    @InjectMocks //This specifies that this is the class under test. It is used to inject mocks into the real instance of the class,
    // Allowing to test the real behavior of the class while mocking its dependencies.
    // A REAL INSTANCE OF THIS IS BEING CREATED.
    // However some static methods of the class are technically mocked, because PowerMock is reflecting them to make them available for testing
    private WeatherService service;

    @Mock //
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before //This will be executed before each test method in the test class.
    public void init() {
        MockitoAnnotations.openMocks(this); //initializes annotated fields in the test class for mocking. In this case, it initializes the @Mock and @InjectMocks fields.
        Assert.assertNotNull(service); //This line asserts that the service field, annotated with @InjectMocks, is not null after the initialization. The @InjectMocks annotation is used to inject the mocked dependencies into the real instance of the class (WeatherService), and this assertion ensures that the injection was successful.
    }


    @Test
    public void ZipToGeoCodeHappyPath() throws IOException, JsonProcessingException, Exception {
        double expectedLat = 40.0005378;
        double expectedLng = -105.2077798;

        // GIVEN
        GeocodeData geoData = new GeocodeData();

        // Read JSON file
        ClassLoader classLoader = getClass().getClassLoader(); //ClassLoader classLoader = getClass().getClassLoader();: This line obtains the class loader for the class being tested. The class loader is used to locate resources (like files) in the classpath.
        File jsonFile = new File(classLoader.getResource("ZipToGeoCodeHappyPath.json").getFile());
        JsonNode mockResponse = objectMapper.readValue(jsonFile, JsonNode.class);
        //^^these lines are preparing a mock response for the RestTemplate by reading the content of a JSON file and converting it into a JsonNode object. 
        //This allows the test to simulate the behavior of the RestTemplate when the getForObject method is called in the WeatherService class.

        // Mock the restTemplate response for zipToGeoCode method
        PowerMockito.mockStatic(WeatherService.class);
        PowerMockito.when(WeatherService.class, "zipToGeoCode", ArgumentMatchers.eq("80303"), ArgumentMatchers.eq(geoData))
                .thenCallRealMethod(); // Mock the method to call the real implementation

        // Mock the restTemplate response for getHourlyWeather method
        PowerMockito.when(restTemplate.getForObject(ArgumentMatchers.anyString(), ArgumentMatchers.eq(JsonNode.class)))
                .thenReturn(mockResponse);
                //restTemplate.getForObject(ArgumentMatchers.anyString(), ArgumentMatchers.eq(JsonNode.class)): This part specifies the method call that you are setting up expectations for. 
                //In this case, it's the getForObject method of the RestTemplate class. The method is expected to be called with any string as the first argument (URL) and an instance of JsonNode.class as the second argument (response type).
                //mockedResponse is defined in the "Read JSON File" bit.

        // WHEN
        GeocodeData result = (GeocodeData) PowerMockito.method(WeatherService.class, "zipToGeoCode", String.class, GeocodeData.class)
                .invoke(service, "80303", geoData);

        // THEN
        GeocodeData resultData = (GeocodeData) result; //This is redundant, But I leave it in here as a good casting example.
        assertEquals("Expected lat to match", expectedLat, resultData.getLat(), 0.0001); // The delta field used to specify a range of tolerance.
        assertEquals("Expected lng to match", expectedLng, resultData.getLng(), 0); // If you need it to be exact, use zero
    }

    // @Test
    // public void getGridIdHappyPath() throws IOException, JsonProcessingException, Exception {
    //     String expectedGridId = "MPX";

    //     //GIVEN 
    //     GeocodeData inputGeoData = new GeocodeData();
    //     inputGeoData.setLat(44.9398);
    //     inputGeoData.setLng(-93.2533);

    //     GeocodeData expectedGeoData = new GeocodeData();
    //     inputGeoData.setLat(44.9398);
    //     inputGeoData.setLng(-93.2533);
    //     inputGeoData.setGridId("MPX");
        

    //     ClassLoader classLoader = getClass().getClassLoader(); 
    //     File jsonFile = new File(classLoader.getResource("GetGridIdHappyPath.json").getFile());
    //     JsonNode mockResponse = objectMapper.readValue(jsonFile, JsonNode.class);

    //     PowerMockito.mockStatic(WeatherService.class);
    //     PowerMockito.when(WeatherService.class, "getGridId", ArgumentMatchers.eq(inputGeoData))
    //         .thenCallRealMethod();

    //     PowerMockito.when(restTemplate.getForObject(ArgumentMatchers.anyString(), ArgumentMatchers.eq(JsonNode.class)))
    //     .thenReturn(mockResponse);    

    //     // WHEN
    //     GeocodeData actualGeoData = (GeocodeData) PowerMockito.method(WeatherService.class, "getGridId", GeocodeData.class)
    //         .invoke(service, inputGeoData);

    //     GeoCodeDataTestUtils.assertGetGridIdEquals("Expect GeocodeData to match " , expectedGeoData, actualGeoData, 0);
    // }
}

