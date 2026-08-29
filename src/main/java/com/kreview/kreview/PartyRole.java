package com.kreview.kreview;

import java.util.Set;

public enum PartyRole {
  LANDLORD,
  TENANT,
  EMPLOYER,
  EMPLOYEE,
  BUYER,
  SELLER,
  PARTY_A,
  PARTY_B,
  PARTY_C,
  OTHER;

  public static final Set<PartyRole> GENERIC_ROLES = Set.of(PARTY_A, PARTY_B, PARTY_C, OTHER);
}
