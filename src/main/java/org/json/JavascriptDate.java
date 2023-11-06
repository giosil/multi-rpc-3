package org.json;

import java.io.Serializable;

import java.util.Calendar;
import java.util.Date;

public 
class JavascriptDate implements Serializable
{
  private static final long serialVersionUID = -301584778351049252L;
  
  protected Calendar calendar;
  
  public JavascriptDate()
  {
    this.calendar = Calendar.getInstance();
  }
  
  public JavascriptDate(Calendar calValue)
  {
    this.calendar = calValue;
    if(this.calendar == null) {
      this.calendar = Calendar.getInstance();
    }
  }
  
  public JavascriptDate(Date dateValue)
  {
    this.calendar = Calendar.getInstance();
    if(dateValue != null) {
      this.calendar.setTimeInMillis(dateValue.getTime());
    }
  }
  
  public JavascriptDate(String sValue)
  {
    this.calendar = Calendar.getInstance();
    Object oValue = JSONTokener.stringToObject(sValue);
    if(oValue instanceof Date) {
      this.calendar.setTimeInMillis(((Date) oValue).getTime());
    }
  }
  
  public 
  void setCalendarValue(Calendar calValue)
  {
    this.calendar = calValue;
    if(this.calendar == null) {
      this.calendar = Calendar.getInstance();
    }
  }
  
  public 
  Calendar getCalendarValue()
  {
    if(this.calendar == null) {
      this.calendar = Calendar.getInstance();
    }
    return this.calendar;
  }
  
  public boolean equals(Object object) {
    if(object instanceof JavascriptDate) {
      Calendar calendarValue = ((JavascriptDate) object).getCalendarValue();
      if(this.calendar == null) {
        this.calendar = Calendar.getInstance();
      }
      return calendar.equals(calendarValue);
    }
    return false;
  }
  
  public int hashCode() {
    if(this.calendar == null) {
      this.calendar = Calendar.getInstance();
    }
    return this.calendar.hashCode();
  }
  
  public
  String toString()
  {
    if(this.calendar == null) {
      this.calendar = Calendar.getInstance();
    }
    
    return serialize(calendar);
  }
  
  public static
  String serialize(Calendar c)
  {
    if(c == null) return "null";
    
    Calendar cal = Calendar.getInstance(JSONObject.timeZone);
    cal.set(Calendar.YEAR,        c.get(Calendar.YEAR));
    cal.set(Calendar.MONTH,       c.get(Calendar.MONTH));
    cal.set(Calendar.DATE,        c.get(Calendar.DATE));
    cal.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
    cal.set(Calendar.MINUTE,      c.get(Calendar.MINUTE));
    cal.set(Calendar.SECOND,      c.get(Calendar.SECOND));
    cal.set(Calendar.MILLISECOND, c.get(Calendar.MILLISECOND));
    
    int iZoneOffset = cal.get(Calendar.ZONE_OFFSET);
    cal.add(Calendar.MILLISECOND, -iZoneOffset);
    int iDST_Offset = cal.get(Calendar.DST_OFFSET);
    cal.add(Calendar.MILLISECOND, -iDST_Offset);
    
    int iYear  = cal.get(Calendar.YEAR);
    int iMonth = cal.get(Calendar.MONTH);
    int iDay   = cal.get(Calendar.DATE);
    int iHour  = cal.get(Calendar.HOUR_OF_DAY);
    int iMin   = cal.get(Calendar.MINUTE);
    int iSec   = cal.get(Calendar.SECOND);
    int iMill  = cal.get(Calendar.MILLISECOND);
    
    return "new Date(" + iYear + "," + iMonth + "," + iDay + "," + iHour + "," + iMin + "," + iSec + "," + iMill + ")";
  }
  
  public static
  String replaceDateTime(String json)
  {
    if(json == null || json.length() == 0) return json;
    
    int iDelimiter = json.indexOf('"');
    if(iDelimiter < 0) {
      iDelimiter = json.indexOf('\'');
      if(iDelimiter < 0) return json;
    }
    
    StringBuffer sbResult = new StringBuffer(json.length());
    
    boolean stringLiteral   = false;
    char    stringDelimiter = '\0';
    boolean escapeSequence  = false;
    
    StringBuffer sbValue = null;
    for(int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);
      if(c == '"' || c == '\'') {
        if(stringLiteral) {
          if(escapeSequence || stringDelimiter != c) {
            sbValue.append(c);
            escapeSequence = false;
            continue;
          }
          
          stringLiteral   = false;
          escapeSequence  = false;
          stringDelimiter = '\0';
          
          String sValue = sbValue.toString();
          if(JSONTokener.isDateTime(sValue)) {
            JavascriptDate javascriptDate = new JavascriptDate(sValue);
            sbResult.append(javascriptDate.toString());
          }
          else {
            sbResult.append("\"" + sValue + "\"");
          }
        }
        else {
          stringLiteral   = true;
          escapeSequence  = false;
          stringDelimiter = c;
          sbValue = new StringBuffer();
        }
        continue;
      }
      if(c == '\\') {
        if(stringLiteral) {
          sbValue.append(c);
          escapeSequence = !escapeSequence;
        }
        else {
          sbResult.append(c);
        }
        continue;
      }
      if(stringLiteral) {
        sbValue.append(c);
      }
      else {
        sbResult.append(c);
      }
    }
    
    return sbResult.toString();
  }
}
