package com.kreview.kreview;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class Contract {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(columnDefinition = "TEXT")
  private String content;
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  private ContractType contractType;

  @Enumerated(EnumType.STRING)
  private PartyRole partyRole;

  @Column(columnDefinition = "TEXT")
  private String partyRoleCustomLabel;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getContent() {

    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public ContractType getContractType() {
    return contractType;
  }

  public void setContractType(ContractType contractType) {
    this.contractType = contractType;
  }

  public PartyRole getPartyRole() {
    return partyRole;
  }

  public void setPartyRole(PartyRole partyRole) {
    this.partyRole = partyRole;
  }

  public String getPartyRoleCustomLabel() {
    return partyRoleCustomLabel;
  }

  public void setPartyRoleCustomLabel(String partyRoleCustomLabel) {
    this.partyRoleCustomLabel = partyRoleCustomLabel;
  }
}