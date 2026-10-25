package com.restru.menu.dto;

/**
 * Outlet delivery serviceability response.
 */
public class ServiceabilityResponse {

    private String pincode;
    private Boolean serviceable;
    private Long outletId;
    private String outletName;
    private String message;

    public ServiceabilityResponse() {
    }

    public ServiceabilityResponse(String pincode, Boolean serviceable, Long outletId,
                                   String outletName, String message) {
        this.pincode = pincode;
        this.serviceable = serviceable;
        this.outletId = outletId;
        this.outletName = outletName;
        this.message = message;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Boolean getServiceable() {
        return serviceable;
    }

    public void setServiceable(Boolean serviceable) {
        this.serviceable = serviceable;
    }

    public Long getOutletId() {
        return outletId;
    }

    public void setOutletId(Long outletId) {
        this.outletId = outletId;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
