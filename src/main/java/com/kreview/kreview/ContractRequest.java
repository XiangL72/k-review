package com.kreview.kreview;

import jakarta.validation.constraints.NotBlank;

public class ContractRequest {

  @NotBlank(message = "Contract content must not be blank")
  private String content;


  public String getContent() {
    return content;
  }


  public void setContent(String content) {
    this.content = content;
  }

}
