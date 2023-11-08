import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.weather.app.models.GeocodeData;
import com.weather.app.services.WeatherService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.weather.app.services.*")
public class WeatherServiceTest {

    @InjectMocks
    private WeatherService service;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    public void zipToGeoCodeHappyPath () {
        //Given
        String zipCode = "80303";
        GeocodeData geo = new GeocodeData();
        geo.setLat(0);
        geo.setLng(0);

        PowerMockito.mockStatic(WeatherService.class);
        
        //When
        when(WeatherService)

        //Then
    }

}
