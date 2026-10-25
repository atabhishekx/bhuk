package com.restru.delivery.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request to update delivery tracking location.
 */
public class UpdateLocationRequest {

    @NotNull
    private java.math.BigDecimal lat;

    @NotNull
    private java.math.BigDecimal lng;

    private Integer accuracy;
    private Integer heading;
    private Integer speed;

    public java.math.BigDecimal getLat() { return lat; }
    public void setLat(java.math.BigDecimal lat) { this.lat = lat; }
    public java.math.BigDecimal getLng() { return lng; }
    public void setLng(java.math.BigDecimal lng) { this.lng = lng; }
    public Integer getAccuracy() { return accuracy; }
    public void setAccuracy(Integer accuracy) { this.accuracy = accuracy; }
    public Integer getHeading() { return heading; }
    public void setHeading(Integer heading) { this.heading = heading; }
    public Integer getSpeed() { return speed; }
    public void setSpeed(Integer speed) { this.speed = speed; }
}
