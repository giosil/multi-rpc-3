package org.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.json.JSON;

/**
 * Wrapper HttpServletRequest
 */
@SuppressWarnings({"rawtypes","unchecked"})
public
class MapRequest implements Map
{
  protected HttpServletRequest request;
  
  public MapRequest(HttpServletRequest request)
  {
    this.request = request;
  }
  
  public int size() {
    Enumeration enumeration = request.getAttributeNames();
    int iResult = 0;
    while(enumeration.hasMoreElements()) {
      enumeration.nextElement();
      iResult++;
    }
    return iResult;
  }
  
  public boolean isEmpty() {
    Enumeration enumeration = request.getAttributeNames();
    return !enumeration.hasMoreElements();
  }
  
  public boolean containsKey(Object key) {
    String sKey = key.toString();
    Enumeration enumeration = request.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      if(sAttributeName.equals(sKey)) return true;
    }
    return false;
  }
  
  public boolean containsValue(Object value) {
    Enumeration enumeration = request.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      Object oAttribute     = request.getAttribute(sAttributeName);
      if(oAttribute != null && oAttribute.equals(value)) return true;
    }
    return false;
  }
  
  public Object get(Object key) {
    return request.getAttribute(key.toString());
  }
  
  public Object put(Object key, Object value) {
    request.setAttribute(key.toString(), value);
    return value;
  }
  
  public Object remove(Object key) {
    String sKey = key.toString();
    Object oAttribute = request.getAttribute(sKey);
    request.removeAttribute(sKey);
    return oAttribute;
  }
  
  public void putAll(Map m) {
    Iterator iterator = m.entrySet().iterator();
    while(iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      request.setAttribute(entry.getKey().toString(), entry.getValue());
    }
  }
  
  public void clear() {
    Enumeration enumeration = request.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      request.removeAttribute(sAttributeName);
    }
  }
  
  public Set keySet() {
    Set setResult = new HashSet();
    Enumeration enumeration = request.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      setResult.add(enumeration.nextElement());
    }
    return setResult;
  }
  
  public Collection values() {
    Set setResult = new HashSet();
    Enumeration enumeration = request.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      setResult.add(request.getAttribute(sAttributeName));
    }
    return setResult;
  }
  
  public Set entrySet() {
    Map mapValues = new HashMap();
    Enumeration enumeration = request.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      mapValues.put(sAttributeName, request.getAttribute(sAttributeName));
    }
    return mapValues.entrySet();
  }
  
  public int hashCode() {
    return request.hashCode();
  }
  
  public boolean equals(Object o) {
    return request.equals(o);
  }
  
  public String toString() {
    Map mapValues = new HashMap();
    Enumeration enumeration = request.getAttributeNames();
    while(enumeration.hasMoreElements()) {
      String sAttributeName = (String) enumeration.nextElement();
      mapValues.put(sAttributeName, request.getAttribute(sAttributeName));
    }
    return JSON.stringify(mapValues);
  }
}
