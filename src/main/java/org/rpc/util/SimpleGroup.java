package org.rpc.util;

import java.io.Serializable;

import java.security.Principal;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

@SuppressWarnings({"rawtypes","unchecked"})
public
class SimpleGroup implements Group, Serializable
{
  private static final long serialVersionUID = -4249088458317329680L;
  
  protected String name;
  protected Vector members;
  
  public SimpleGroup(String name) {
    this.name = name;
    this.members = new Vector();
  }
  
  public boolean addMember(Principal user) {
    members.add(user);
    return true;
  }
  
  public boolean removeMember(Principal user) {
    int index = members.indexOf(user);
    if(index < 0) return false;
    members.remove(index);
    return true;
  }
  
  public boolean isMember(Principal member) {
    int index = members.indexOf(member);
    return index >= 0;
  }
  
  public Enumeration members() {
    return members.elements();
  }
  
  public String getName() {
    return name;
  }
  
  public boolean equals(Object object) {
    if(object instanceof Group) {
      String sName = ((Group) object).getName();
      if(sName == null && name == null)
      return true;
      return sName != null && sName.equals(name);
    }
    return false;
  }
  
  public int hashCode() {
    if(name == null)
    return 0;
    return name.hashCode();
  }
  
  public String toString() {
    return name + members;
  }
  
  public static
  SimpleGroup create(String name, Collection colMembers)
  {
    SimpleGroup result = new SimpleGroup(name);
    if(colMembers != null) {
      Iterator iterator = colMembers.iterator();
      while(iterator.hasNext()) {
        Object member = iterator.next();
        if(member == null) continue;
        result.addMember(new SimplePrincipal(member.toString()));
      }
    }
    return result;
  }
  
  public static
  SimpleGroup create(String name, String[] asMembers)
  {
    SimpleGroup result = new SimpleGroup(name);
    if(asMembers != null) {
      for(int i = 0; i < asMembers.length; i++) {
        result.addMember(new SimplePrincipal(asMembers[i]));
      }
    }
    return result;
  }
  
  public static
  SimpleGroup create(String name, String member)
  {
    SimpleGroup result = new SimpleGroup(name);
    if(member != null) {
      result.addMember(new SimplePrincipal(member));
    }
    return result;
  }
}
