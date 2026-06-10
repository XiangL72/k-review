package com.kreview.kreview.messaging;

public class AnalysisJobMessage {

  private Long contractId;
  private String jobId;

  public AnalysisJobMessage() {
  }

  public AnalysisJobMessage(Long contractId, String jobId) {
    this.contractId = contractId;
    this.jobId = jobId;
  }

  public Long getContractId() {
    return contractId;
  }

  public void setContractId(Long contractId) {
    this.contractId = contractId;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }
}