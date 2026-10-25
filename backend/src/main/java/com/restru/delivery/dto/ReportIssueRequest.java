package com.restru.delivery.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to report a delivery issue.
 */
public class ReportIssueRequest {

    @NotBlank
    private String issueType;

    private String description;

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
