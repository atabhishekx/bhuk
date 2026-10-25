package com.restru.delivery.dto;

import java.time.Instant;

/**
 * Delivery tracking location response.
 */
public class DeliveryTrackingResponse {

    private Long id;
    private Long assignmentId;
    private java.math.BigDecimal lat;
    private java.math.BigDecimal lng;
    private Integer accuracy;
    private Integer heading;
    private Integer speed;
    private Instant recordedAt;

    public DeliveryTrackingResponse() {
    }

    public DeliveryTrackingResponse(Long id, Long assignmentId, java.math.BigDecimal lat, java.math.BigDecimal lng,
                                    Integer accuracy, Integer heading, Integer speed, Instant recordedAt) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.lat = lat;
        this.lng = lng;
        this.accuracy = accuracy;
        this.heading = heading;
        this.speed = speed;
        this.recordedAt = recordedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
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
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
