package com.weather.app.models;

import lombok.Data;

@Data
public class GeocodeData {
    private String gridId;
    private double lat;
    private double lng;
}