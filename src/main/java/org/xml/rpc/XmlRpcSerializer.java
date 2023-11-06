package org.xml.rpc;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.time.ZoneId;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.rpc.util.Base64Coder;
import org.rpc.util.Mapable;

public
class XmlRpcSerializer
{
  public static
  String serialize(Object object)
  {
    return serialize(object, false);
  }
  
  public static
  String serialize(Object object, boolean boLegacy)
  {
    if(object == null) {
      return boLegacy ? "" : "<value><nil/></value>"; // Estensione
    }
    else
    if(object instanceof String) {
      return "<value>" + normalizeString((String) object) + "</value>";
    }
    else
    if(object instanceof Integer) {
      return "<value><int>" + object + "</int></value>";
    }
    else
    if(object instanceof Double) {
      return "<value><double>" + object + "</double></value>";
    }
    else
    if(object instanceof java.util.Date) {
      return "<value><dateTime.iso8601>" + serializeDate(object) + "</dateTime.iso8601></value>";
    }
    else
    if(object instanceof java.util.Calendar) {
      return "<value><dateTime.iso8601>" + serializeDate(object) + "</dateTime.iso8601></value>";
    }
    else // 1.8+
    if(object instanceof java.time.LocalDate) {
      return "<value><dateTime.iso8601>" + serializeDate(object) + "</dateTime.iso8601></value>";
    }
    else // 1.8+
    if(object instanceof java.time.LocalDateTime) {
      return "<value><dateTime.iso8601>" + serializeDate(object) + "</dateTime.iso8601></value>";
    }
    else
    if(object instanceof Boolean) {
      if(((Boolean) object).booleanValue()) {
        return "<value><boolean>1</boolean></value>";
      }
      else {
        return "<value><boolean>0</boolean></value>";
      }
    }
    else
    if(object instanceof Map) {
      return serializeMap((Map<?, ?>) object, boLegacy);
    }
    else
    if(object instanceof Map.Entry) {
      return serialize(((Map.Entry<?, ?>) object).getValue(), boLegacy);
    }
    else
    if(object instanceof Collection) {
      return serializeCollection((Collection<?>) object, boLegacy);
    }
    else
    if(object instanceof byte[]) {
      return "<value><base64>" + String.valueOf(Base64Coder.encode((byte[]) object))  + "</base64></value>";
    }
    else
    if(object.getClass().isArray()) {
      return serializeArray(object, boLegacy);
    }
    else
    if(object.getClass().isEnum()) {
      return "<value>" + normalizeString(object.toString()) + "</value>";
    }
    else
    if(object instanceof Mapable) {
      Map<String, Object> map = ((Mapable) object).toMap();
      return map != null ? serializeMap(map, boLegacy) : "<value><nil/></value>";
    }
    else
    if(object instanceof Long) {
      return boLegacy ? "<value><int>" + object + "</int></value>" : "<value><long>" + object + "</long></value>"; // Estensione (non standard)
    }
    else
    if(object instanceof Short) {
      return "<value><int>" + object + "</int></value>";
    }
    else
    if(object instanceof Float) {
      return "<value><double>" + object + "</double></value>";
    }
    else
    if(object instanceof BigDecimal) {
      return "<value><double>" + object + "</double></value>";
    }
    else
    if(object instanceof BigInteger) {
      return boLegacy ? "<value><int>" + object + "</int></value>" : "<value><long>" + object + "</long></value>"; // Estensione (non standard)
    }
    else
    if(object instanceof CharSequence) {
      return "<value>" + normalizeString(object.toString()) + "</value>";
    }
    else
    if(object instanceof java.net.URL || object instanceof java.net.URI) {
      return "<value>" + normalizeString(object.toString()) + "</value>";
    }
    else
    if(object instanceof java.io.File) {
      return "<value>" + normalizeString(object.toString()) + "</value>";
    }
    return serializeBean(object, boLegacy);
  }
  
  public static
  String normalizeString(String sValue)
  {
    if(sValue == null) return "null";
    StringBuilder sbResult = new StringBuilder(sValue.length());
    int iLength = sValue.length();
    for(int i = 0; i < iLength; i++) {
      char c = sValue.charAt(i);
      switch(c) {
        case '<':
          sbResult.append("&lt;");
          break;
        case '>':
          sbResult.append("&gt;");
          break;
        case '&':
          sbResult.append("&amp;");
          break;
        default:
          sbResult.append(c);
      }
    }
    return sbResult.toString();
  }
  
  private static
  String serializeMap(Map<?, ?> map, boolean boLegacy)
  {
    StringBuilder sb = new StringBuilder(map.size() * 50 + 50);
    sb.append("<value><struct>");
    Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
    while(iterator.hasNext()) {
      Map.Entry<?, ?> entry = iterator.next();
      Object oKey   = entry.getKey();
      Object oValue = entry.getValue();
      if(oValue != null) {
        String sSerializedValue = serialize(oValue, boLegacy);
        if(sSerializedValue != null && sSerializedValue.length() > 0) {
          sb.append("<member><name>");
          sb.append(oKey.toString());
          sb.append("</name>");
          sb.append(sSerializedValue);
          sb.append("</member>");
        }
      }
      else if(!boLegacy) {
        sb.append("<member><name>");
        sb.append(oKey.toString());
        sb.append("</name><value><nil/></value></member>"); // Estensione
      }
    }
    sb.append("</struct></value>");
    return sb.toString();
  }
  
  private static
  String serializeCollection(Collection<?> collection, boolean boLegacy)
  {
    StringBuilder sb = new StringBuilder(collection.size() * 20 + 50);
    sb.append("<value><array><data>");
    Iterator<?> iterator = collection.iterator();
    while(iterator.hasNext()) {
      sb.append(serialize(iterator.next(), boLegacy));
    }
    sb.append("</data></array></value>");
    return sb.toString();
  }
  
  private static
  String serializeArray(Object array, boolean boLegacy)
  {
    int length = Array.getLength(array);
    StringBuilder sb = new StringBuilder(length * 20 + 50);
    sb.append("<value><array><data>");
    for(int i = 0; i < length; i++) {
      sb.append(serialize(Array.get(array, i), boLegacy));
    }
    sb.append("</data></array></value>");
    return sb.toString();
  }
  
  private static
  String serializeBean(Object bean, boolean boLegacy)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<value><struct>");
    Class<?> klass  = bean.getClass();
    
    String sClassName = klass.getName();
    boolean boValue   = sClassName.indexOf("CodeAndDesc") >= 0 || sClassName.indexOf("NamedParam") >= 0;
    
    // If klass is a System class then set includeSuperClass to false.
    boolean includeSuperClass = klass.getClassLoader() != null;
    Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
    for(int i = 0; i < methods.length; i++) {
      try {
        Method method = methods[i];
        if(Modifier.isPublic(method.getModifiers())) {
          String name = method.getName();
          String key  = "";
          if(name.startsWith("get")) {
            if("getClass".equals(name) || "getDeclaringClass".equals(name)) {
              key = "";
            }
            else {
              key = name.substring(3);
              if(boValue &&(key.equalsIgnoreCase("code") || key.equalsIgnoreCase("value"))) {
                Object oValue = method.invoke(bean,(Object[]) null);
                return serialize(oValue, boLegacy);
              }
            }
          }
          else if(name.startsWith("is")) {
            key = name.substring(2);
          }
          if(key.length() > 0 && key.charAt(0) < 97 && method.getParameterTypes().length == 0) {
            if(key.length() == 1) {
              key = key.toLowerCase();
            }
            else if(!Character.isUpperCase(key.charAt(1))) {
              key = key.substring(0, 1).toLowerCase() + key.substring(1);
            }
            Object oValue = method.invoke(bean,(Object[]) null);
            if(oValue != null) {
              sb.append("<member><name>");
              sb.append(key);
              sb.append("</name>");
              sb.append(serialize(oValue, boLegacy));
              sb.append("</member>");
            }
          }
        }
      }
      catch(Exception ex) {
      }
    }
    sb.append("</struct></value>");
    return sb.toString();
  }
  
  private static
  String serializeDate(Object oDateTime)
  {
    Calendar cal = null;
    if(oDateTime instanceof java.util.Date) {
      cal = Calendar.getInstance();
      cal.setTimeInMillis(((java.util.Date) oDateTime).getTime());
    }
    else if(oDateTime instanceof java.util.Calendar) {
      cal = (Calendar) oDateTime;
    }
    else if(oDateTime instanceof java.time.LocalDate) {
      cal = Calendar.getInstance();
      cal.setTimeInMillis(((java.time.LocalDate) oDateTime).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
    else if(oDateTime instanceof java.time.LocalDateTime) {
      cal = Calendar.getInstance();
      cal.setTimeInMillis(((java.time.LocalDateTime) oDateTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
    
    int iYear  = cal.get(Calendar.YEAR);
    int iMonth = cal.get(Calendar.MONTH) + 1;
    int iDay   = cal.get(Calendar.DATE);
    int iHour  = cal.get(Calendar.HOUR_OF_DAY);
    int iMin   = cal.get(Calendar.MINUTE);
    int iSec   = cal.get(Calendar.SECOND);
    String sYear  = String.valueOf(iYear);
    String sMonth = iMonth < 10 ? "0" + iMonth : String.valueOf(iMonth);
    String sDay   = iDay   < 10 ? "0" + iDay   : String.valueOf(iDay);
    String sHour  = iHour  < 10 ? "0" + iHour  : String.valueOf(iHour);
    String sMin   = iMin   < 10 ? "0" + iMin   : String.valueOf(iMin);
    String sSec   = iSec   < 10 ? "0" + iSec   : String.valueOf(iSec);
    if(iYear < 10) {
      sYear = "000" + sYear;
    }
    else if(iYear < 100) {
      sYear = "00" + sYear;
    }
    else if(iYear < 1000) {
      sYear = "0" + sYear;
    }
    return sYear + sMonth + sDay + "T" + sHour + ":" + sMin + ":" + sSec;
  }
}
