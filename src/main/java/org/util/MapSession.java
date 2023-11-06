package org.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import org.json.JSON;

/**
 * Wrapper HttpSession
 */
@SuppressWarnings({"rawtypes","unchecked"})
public
class MapSession implements Map
{
  protected HttpSession session;
  
  public MapSession(HttpSession session)
  {
    this.session = session;
  }
  
  public int size() {
    if(session == null) return 0;
    Enumeration enumeration = session.getAttributeNames();
    int iResult = 0;
    while(enumeration.hasMoreElements()) {
      enumeration.nextElement();
      iResult++;
    }
    return iResult;
  }
  
  public boolean isEmpty() {
    Enumeration enumeration = session.getAttributeNames();
    return !enumeration.hasMoreElements();
  }
  
  public boolean containsKey(Object key) {
    if(session == null) return false;
    String sKey = key.toString();
    Enumeration enumeration = session.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      if(sAttributeName.equals(sKey)) return true;
    }
    return false;
  }
  
  public boolean containsValue(Object value) {
    if(session == null) return false;
    Enumeration enumeration = session.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      Object oAttribute     = session.getAttribute(sAttributeName);
      if(oAttribute != null && oAttribute.equals(value)) return true;
    }
    return false;
  }
  
  public Object get(Object key) {
    if(session == null) return null;
    return session.getAttribute(key.toString());
  }
  
  public Object put(Object key, Object value) {
    if(session == null) return null;
    session.setAttribute(key.toString(), value);
    return value;
  }
  
  public Object remove(Object key) {
    if(session == null) return null;
    String sKey = key.toString();
    Object oAttribute = session.getAttribute(sKey);
    session.removeAttribute(sKey);
    return oAttribute;
  }
  
  public void putAll(Map m) {
    if(session == null) return;
    Iterator iterator = m.entrySet().iterator();
    while(iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      session.setAttribute(entry.getKey().toString(), entry.getValue());
    }
  }
  
  public void clear() {
    if(session == null) return;
    Enumeration enumeration = session.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      session.removeAttribute(sAttributeName);
    }
  }
  
  public Set keySet() {
    if(session == null) return new HashSet(0);
    Set setResult = new HashSet();
    Enumeration enumeration = session.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      setResult.add(sAttributeName);
    }
    return setResult;
  }
  
  public Collection values() {
    if(session == null) return new HashSet(0);
    Set setResult = new HashSet();
    Enumeration enumeration = session.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      setResult.add(session.getAttribute(sAttributeName));
    }
    return setResult;
  }
  
  public Set entrySet() {
    if(session == null) return new HashSet(0);
    Map mapValues = new HashMap();
    Enumeration enumeration = session.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      mapValues.put(sAttributeName, session.getAttribute(sAttributeName));
    }
    return mapValues.entrySet();
  }
  
  public int hashCode() {
    if(session == null) return 0;
    return session.hashCode();
  }
  
  public boolean equals(Object o) {
    if(session == null && o == null) return true;
    if(session == null) return false;
    return session.equals(o);
  }
  
  public String toString() {
    if(session == null) return "null";
    Map mapValues = new HashMap();
    Enumeration enumeration = session.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      mapValues.put(sAttributeName, session.getAttribute(sAttributeName));
    }
    return JSON.stringify(mapValues);
  }
}
