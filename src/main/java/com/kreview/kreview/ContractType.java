package com.kreview.kreview;

import java.util.Set;

public enum ContractType {
  LEASE {
    @Override
    public Set<PartyRole> validRoles() {
      return Set.of(PartyRole.LANDLORD, PartyRole.TENANT);
    }
  },
  EMPLOYMENT {
    @Override
    public Set<PartyRole> validRoles() {
      return Set.of(PartyRole.EMPLOYER, PartyRole.EMPLOYEE);
    }
  },
  SALES {
    @Override
    public Set<PartyRole> validRoles() {
      return Set.of(PartyRole.BUYER, PartyRole.SELLER);
    }
  };

  public abstract Set<PartyRole> validRoles();
}
