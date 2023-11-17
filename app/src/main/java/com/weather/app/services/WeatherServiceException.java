package com.weather.app.services;

public class WeatherServiceException extends Exception {
    private int errorCode;
    
    // Constructors
    public WeatherServiceException(int errorCode) {
        super(getErrorMessage(errorCode));
        this.errorCode = errorCode;
    }

    public WeatherServiceException(int errorCode, Throwable cause) {
        super(getErrorMessage(errorCode), cause);
        this.errorCode = errorCode;
    }

    // Getter for errorCode
    public int getErrorCode() {
        return errorCode;
    }

    // Utility method to map error codes to user-friendly error messages
    private static String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case 400:
                return "Bad Request: The request to the weather service is malformed or invalid.";
            case 402:
                return "Null Value in Response: API returned a 200 OK status code with no data.";
            case 401:
                return "Unauthorized: Authentication failed when accessing the weather service.";
            case 403:
                return "Forbidden: The request is valid, but the server refuses to respond.";
            case 404:
                return "Not Found: The requested resource from the weather service was not found.";
            case 500:
                return "Internal Server Error: The requested resource could not be found";

            default:
                return "Unknown Weather Service Error: An unexpected error occurred.";
        }
    }
}

