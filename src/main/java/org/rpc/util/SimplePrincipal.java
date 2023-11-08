package org.rpc.util;

import java.io.Serializable;
import java.security.Principal;

import java.util.Map;

@SuppressWarnings({"rawtypes"})
public
class SimplePrincipal implements Principal, Serializable
{
  private static final long serialVersionUID = 3583128602597019932L;
  
  protected String name;
  protected Group  roles;
  protected Map attributes;
  
  public SimplePrincipal(String name) {
    this.name = name;
  }
  
  public Map getAttributes() {
    return attributes;
  }
  
  public void setAttributes(Map attributes) {
    this.attributes = attributes;
  }
  
  public String getName() {
    return name;
  }
  
  public Group getRoles() {
    return roles;
  }
  
  public void setRoles(Group roles) {
    this.roles = roles;
  }
  
  public boolean equals(Object object) {
    if(object instanceof Principal) {
      String sName = ((Principal) object).getName();
      if(sName == null && name == null) return true;
      return sName != null && sName.equals(name);
    }
    return false;
  }
  
  public int hashCode() {
    if(name == null) return 0;
    return name.hashCode();
  }
  
  public String toString() {
    return name;
  }
}
