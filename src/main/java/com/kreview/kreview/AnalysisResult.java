package com.kreview.kreview;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class AnalysisResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "contract_id")
  private Contract contract;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "analysis_result_id")
  private List<Clause> clauses;

  private String summary;
  private int overallRiskScore;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Contract getContract() {
    return contract;
  }

  public void setContract(Contract contract) {
    this.contract = contract;
  }

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