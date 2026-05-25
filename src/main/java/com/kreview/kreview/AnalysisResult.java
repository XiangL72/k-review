package com.kreview.kreview;

import java.util.List;

public class AnalysisResult {
  private List<Clause> clauses;
  private String summary;
  private int overallRiskScore;

  public List<Clause> getClauses() {
    return clauses;
  }

  public void setClauses(List<Clause> clauses) {
    this.clauses = clauses;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public int getOverallRiskScore() {
    return overallRiskScore;
  }

  public void setOverallRiskScore(int overallRiskScore) {
    this.overallRiskScore = overallRiskScore;
  }
}