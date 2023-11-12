import static org.junit.Assert.assertEquals;

import java.io.File;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.app.models.GeocodeData;
import com.weather.app.services.WeatherService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WeatherService.class)
public class WeatherServiceTest {

    @InjectMocks
    private WeatherService service;

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        Assert.assertNotNull(service);
    }


    @Test
    public void ZipToGeoCodeHappyPath() throws Exception {
        double expectedLat = 40.0005378;
        double expectedLng = -105.2077798;



        // Given
        GeocodeData geoData = new GeocodeData();
        geoData.setGridId("80303"); // Set the gridId to a valid value

        // Read JSON file
        ClassLoader classLoader = getClass().getClassLoader();
        File jsonFile = new File(classLoader.getResource("ZipToGeoCodeHappyPath.json").getFile());
        JsonNode mockResponse = objectMapper.readValue(jsonFile, JsonNode.class);

        // Mock the restTemplate response for zipToGeoCode method
        PowerMockito.mockStatic(WeatherService.class);
        PowerMockito.when(WeatherService.class, "zipToGeoCode", ArgumentMatchers.anyString(), ArgumentMatchers.any(GeocodeData.class))
                .thenCallRealMethod(); // Mock the method to call the real implementation

        // Mock the restTemplate response for getHourlyWeather method
        PowerMockito.when(restTemplate.getForObject(ArgumentMatchers.anyString(), ArgumentMatchers.eq(JsonNode.class)))
                .thenReturn(mockResponse);

        // When
        GeocodeData result = (GeocodeData) PowerMockito.method(WeatherService.class, "zipToGeoCode", String.class, GeocodeData.class)
                .invoke(service, geoData.getGridId(), new GeocodeData());

        // Then
        GeocodeData resultData = (GeocodeData) result;
        assertEquals("Expected gridId to match", geoData.getGridId(), resultData.getGridId());
        assertEquals("Expected lat to match", expectedLat, resultData.getLat(), 0.001); // adjust delta as needed
        assertEquals("Expected lng to match", expectedLng, resultData.getLng(), 0.001); // adjust delta as needed
    }
}

