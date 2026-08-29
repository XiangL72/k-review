package com.kreview.kreview;

import jakarta.validation.constraints.NotBlank;

public class ContractRequest {

  @NotBlank(message = "Contract content must not be blank")
  private String content;

  private ContractType contractType;

  private PartyRole partyRole;

  private String partyRoleCustomLabel;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
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
