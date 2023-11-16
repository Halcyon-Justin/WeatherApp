package com.weather.app.models;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class GeocodeData {
    private String gridId;
    private double lat;
    private double lng;
}