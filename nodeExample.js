const axios = require('axios');
const { truncateGeoData } = require('../helpers/truncateGeoData');
const { decimalToAbsInt } = require('../helpers/decimalToAbsInt.js');
const { dailyHighLows } = require('../helpers/dailyHighLows');

const getLocation = async (req, res) => {
  const { zip } = req.params;
  const apiKey = process.env.GOOGLE_GEOCACHE_KEY;
  //NOTE: I know the .env file should be added to the gitignore. This is a demo and I will delete the key later

  try {
    const response = await axios.get(`https://maps.googleapis.com/maps/api/geocode/json?address=${zip}&key=${apiKey}`);
    const locationResult = response.data;

    if (locationResult.status === 'OK') {
      const { lat, lng } = locationResult.results[0].geometry.location;
      const truncatedCoordinates = truncateGeoData({ lat, lng });
      const intCoords = decimalToAbsInt({ lat, lng });

      const gridId = await getStationId(locationResult);

      if (!gridId) {
        const errorResponse = {
          method: 'getStationId',
          apiCall: `https://api.weather.gov/points/${lat},${lng}`,
          errorCode: 500,
          errorMessage: 'Failed to retrieve gridId',
        };
        res.status(500).json(errorResponse);
        return;
      }

      const hourlyTemps = await getHourlyData(gridId, [intCoords.lat, intCoords.lng]);

      if (!hourlyTemps) {
        const errorResponse = {
          method: 'getHourlyData',
          apiCall: `https://api.weather.gov/gridpoints/${gridId}/${intCoords.lat},${intCoords.lng}/forecast/hourly`,
          errorCode: 500,
          errorMessage: 'Failed to retrieve hourly temperatures',
        };
        res.status(500).json(errorResponse);
        return;
      }

      const dailyTempRanges = dailyHighLows(hourlyTemps);

      const jsonResponse = {
        dailyTempRanges,
      };

      res.status(200).json(jsonResponse);
    } else {
      const errorResponse = {
        method: 'getLocation',
        apiCall: `https://maps.googleapis.com/maps/api/geocode/json?address=${zip}`,
        errorCode: 404,
        errorMessage: 'Location not found',
      };
      res.status(404).json(errorResponse);
    }
  } catch (error) {
    console.error('Error in getLocation:', error);
    const errorResponse = {
      method: 'getLocation',
      apiCall: `https://maps.googleapis.com/maps/api/geocode.json?address=${zip}`,
      errorCode: error.response?.status || 500,
      errorMessage: error.message,
    };
    res.status(error.response?.status || 500).json(errorResponse);
  }
};

const getStationId = async (locationResult) => {
  const { lat, lng } = locationResult.results[0].geometry.location;
  const gridUrl = `https://api.weather.gov/points/${lat},${lng}`;

  try {
    const gridResponse = await axios.get(gridUrl);
    const gridData = gridResponse.data;

    if (gridData.properties && gridData.properties.gridId) {
      return gridData.properties.gridId;
    } else {
      console.error('Grid ID not found in response');
      const errorResponse = {
        method: 'getStationId',
        apiCall: gridUrl,
        errorCode: 500,
        errorMessage: 'Grid ID not found in response',
      };
      return null;
    }
  } catch (error) {
    console.error('Error in getStationId:', error);
    const errorResponse = {
      method: 'getStationId',
      apiCall: gridUrl,
      errorCode: error.response?.status || 500,
      errorMessage: error.message,
    };
    return null;
  }
};

const getHourlyData = async (gridId, intCoords) => {
  try {
    const [x, y] = intCoords;
    const hourlyDataUrl = `https://api.weather.gov/gridpoints/${gridId}/${x},${y}/forecast/hourly`;

    const hourlyResponse = await axios.get(hourlyDataUrl);
    const hourlyData = hourlyResponse.data;

    const dailyWeather = {};

    if (hourlyData.properties && hourlyData.properties.periods) {
      const hourlyPeriods = hourlyData.properties.periods;

      hourlyPeriods.forEach((period) => {
        const date = period.startTime.substr(0, 10);

        if (!dailyWeather[date]) {
          dailyWeather[date] = [];
        }

        dailyWeather[date].push(period.temperature);
      });
    } else {
      console.error('Hourly weather data not found in response');
      const errorResponse = {
        method: 'getHourlyData',
        apiCall: hourlyDataUrl,
        errorCode: 500,
        errorMessage: 'Hourly weather data not found in response',
      };
      return null;
    }

    return dailyWeather;
  } catch (error) {
    console.error('Error in getHourlyData:', error);
    const errorResponse = {
      method: 'getHourlyData',
      apiCall: hourlyDataUrl,
      errorCode: error.response?.status || 500,
      errorMessage: error.message,
    };
    return null;
  }
};

module.exports = { getLocation, getStationId, getHourlyData };