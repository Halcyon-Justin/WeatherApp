import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.weather.app.models.GeocodeData;
import com.weather.app.services.WeatherService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WeatherService.class)
public class WeatherServiceTest {

    @InjectMocks
    public WeatherService service;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        Assert.assertNotNull(service);
    }

    @Test
    public void zipToGeoCodeHappyPath() throws Exception {
        // Given
        String zipCode = "80303";
        GeocodeData geo = new GeocodeData();
        geo.setLat(0);
        geo.setLng(0);

        PowerMockito.mockStatic(WeatherService.class);
        
        // When
        WeatherService spy = PowerMockito.spy(service);
        PowerMockito.doReturn(geo).when(spy, "zipToGeoCode", ArgumentMatchers.any());
        
        // Then
        assertEquals(geo, spy);
    }
}


