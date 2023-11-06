package org.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.json.JSON;

/**
 * Wrapper List
 */
@SuppressWarnings({"rawtypes","unchecked"})
public
class WList implements List
{
  protected List list;
  
  public WList()
  {
    list = new ArrayList();
  }
  
  public WList(boolean boLegacy)
  {
    if(boLegacy) {
      list = new Vector();
    }
    else {
      list = new ArrayList();
    }
  }
  
  public WList(List l)
  {
    list = l != null ? l : new ArrayList();
  }
  
  public WList(String sJSONArray)
  {
    if(sJSONArray == null || sJSONArray.length() < 2 || !sJSONArray.startsWith("[")) {
      list = new ArrayList();
    }
    else {
      Object object = JSON.parse(sJSONArray);
      if(object instanceof List) {
        list = (List) object;
      }
      else {
        list = new ArrayList();
      }
    }
  }
  
  public WList(String sList, char sep)
  {
    list = new ArrayList();
    if(sList == null || sList.length() == 0) return;
    int iIndexOf = 0;
    int iBegin   = 0;
    iIndexOf = sList.indexOf(sep);
    while(iIndexOf >= 0) {
      list.add(sList.substring(iBegin, iIndexOf));
      iBegin = iIndexOf + 1;
      iIndexOf = sList.indexOf(sep, iBegin);
    }
    list.add(sList.substring(iBegin));
  }
  
  public WList(String sList, char sep, int iLength, Object oFiller)
  {
    list = new ArrayList();
    if(sList != null && sList.length() > 0) {
      int iIndexOf = 0;
      int iBegin   = 0;
      iIndexOf = sList.indexOf(sep);
      while(iIndexOf >= 0) {
        list.add(sList.substring(iBegin, iIndexOf));
        iBegin = iIndexOf + 1;
        iIndexOf = sList.indexOf(sep, iBegin);
      }
      list.add(sList.substring(iBegin));
    }
    int iDiff = iLength - list.size();
    if(iDiff <= 0) return;
    for(int i = 0; i < iDiff; i++) list.add(oFiller);
  }
  
  public int size() {
    return list.size();
  }
  
  public boolean isEmpty() {
    return list.isEmpty();
  }
  
  public boolean contains(Object o) {
    return list.contains(o);
  }
  
  public Iterator iterator() {
    return list.iterator();
  }
  
  public Object[] toArray() {
    return list.toArray();
  }
  
  public Object[] toArray(Object[] a) {
    return list.toArray(a);
  }
  
  public boolean add(Object e) {
    return list.add(e);
  }
  
  public boolean remove(Object o) {
    return list.remove(o);
  }
  
  public boolean containsAll(Collection c) {
    return list.containsAll(c);
  }
  
  public boolean addAll(Collection c) {
    return list.addAll(c);
  }
  
  public boolean addAll(int index, Collection c) {
    return list.addAll(index, c);
  }
  
  public boolean removeAll(Collection c) {
    return list.removeAll(c);
  }
  
  public boolean retainAll(Collection c) {
    return list.retainAll(c);
  }
  
  public void clear() {
    list.clear();
  }
  
  public Object get(int index) {
    return list.get(index);
  }
  
  public Object set(int index, Object element) {
    return list.set(index, element);
  }
  
  public void add(int index, Object element) {
    list.add(index, element);
  }
  
  public Object remove(int index) {
    return list.remove(index);
  }
  
  public int indexOf(Object o) {
    return list.indexOf(o);
  }
  
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }
  
  public ListIterator listIterator() {
    return list.listIterator();
  }
  
  public ListIterator listIterator(int index) {
    return list.listIterator(index);
  }
  
  public List subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }
  
  public int hashCode() {
    return list.hashCode();
  }
  
  public boolean equals(Object o) {
    return list.equals(o);
  }
  
  public String toString() {
    return JSON.stringify(list);
  }
  
  public Vector toVector() {
    if(list instanceof Vector) return (Vector) list;
    Vector vector = new Vector(list.size());
    Iterator iterator = list.iterator();
    while(iterator.hasNext()) {
      vector.add(iterator.next());
    }
    return vector;
  }
  
  public ArrayList toArrayList() {
    if(list instanceof ArrayList) return (ArrayList) list;
    ArrayList arrayList = new ArrayList(list.size());
    Iterator iterator = list.iterator();
    while(iterator.hasNext()) {
      arrayList.add(iterator.next());
    }
    return arrayList;
  }
  
  public List<Map<String,Object>> toListOfMapObject() {
    return WUtil.toListOfMapObject(list);
  }
  
  public List<Map<String,Object>> toListOfMapObject(boolean replaceNonStringKeys) {
    return WUtil.toListOfMapObject(list, replaceNonStringKeys);
  }
  
  public 
  List<List<Object>> toListOfListObject() {
    return WUtil.toListOfListObject(list);
  }
  
  public List<String> toListOfString() {
    return WUtil.toListOfString(list);
  }
  
  public List<Number> toListOfNumber() {
    return WUtil.toListOfNumber(list);
  }
  
  public <T> List<T> toList(Object key, Class<T> itemsClass) {
    return WUtil.toList(list, itemsClass, null);
  }
  
  // get methods
  
  public Object getFirst() {
    if(list == null || list.size() == 0) return null;
    return list.get(0);
  }
  
  public Object getLast() {
    if(list == null || list.size() == 0) return null;
    return list.get(list.size()-1);
  }
  
  public int getInt(int index) {
    return WUtil.toInt(get(index), 0);
  }
  
  public int getInt(int index, int iDefault) {
    if(list.size() <= index) return iDefault;
    return WUtil.toInt(get(index), iDefault);
  }
  
  public Integer getInteger(int index) {
    return WUtil.toInteger(get(index), null);
  }
  
  public Integer getInteger(int index, Integer oDefault) {
    if(list.size() <= index) return oDefault;
    return WUtil.toInteger(get(index), oDefault);
  }
  
  public double getDouble(int index) {
    return WUtil.toDouble(get(index), 0.0d);
  }
  
  public double getDouble(int index, double dDefault) {
    if(list.size() <= index) return dDefault;
    return WUtil.toDouble(get(index), dDefault);
  }
  
  public double getDouble(int index, double dDefault, int decimal) {
    if(list.size() <= index) return WUtil.round(dDefault, decimal);
    double dValue = WUtil.toDouble(get(index), dDefault);
    return WUtil.round(dValue, decimal);
  }
  
  public Double getDoubleObj(int index) {
    return WUtil.toDoubleObj(get(index), null);
  }
  
  public Double getDoubleObj(int index, Double oDefault) {
    if(list.size() <= index) return oDefault;
    return WUtil.toDoubleObj(get(index), oDefault);
  }
  
  public Double getDoubleObj(int index, Double oDefault, int decimal) {
    if(list.size() <= index) return oDefault;
    Double oValue = WUtil.toDoubleObj(get(index), oDefault);
    if(oValue == null) return null;
    return new Double(WUtil.round(oValue.doubleValue(), decimal));
  }
  
  public long getLong(int index) {
    return WUtil.toLong(get(index), 0);
  }
  
  public long getLong(int index, long lDefault) {
    if(list.size() <= index) return lDefault;
    return WUtil.toLong(get(index), lDefault);
  }
  
  public Long getLongObj(int index) {
    return WUtil.toLongObj(get(index), null);
  }
  
  public Long getLongObj(int index, Long oDefault) {
    if(list.size() <= index) return oDefault;
    return WUtil.toLongObj(get(index), oDefault);
  }
  
  public BigDecimal getBigDecimal(int index) {
    return WUtil.toBigDecimal(get(index), null);
  }
  
  public BigDecimal getBigDecimal(int index, BigDecimal oDefault) {
    if(list.size() <= index) return oDefault;
    return WUtil.toBigDecimal(get(index), oDefault);
  }
  
  public BigInteger getBigInteger(int index) {
    return WUtil.toBigInteger(get(index), null);
  }
  
  public BigInteger getBigInteger(int index, BigInteger oDefault) {
    if(list.size() <= index) return oDefault;
    return WUtil.toBigInteger(get(index), oDefault);
  }
  
  public boolean getBoolean(int index) {
    return WUtil.toBoolean(get(index), false);
  }
  
  public boolean getBoolean(int index, boolean boDefault) {
    if(list.size() <= index) return boDefault;
    return WUtil.toBoolean(get(index), boDefault);
  }
  
  public Boolean getBooleanObj(int index) {
    return WUtil.toBooleanObj(get(index), null);
  }
  
  public Boolean getBooleanObj(int index, Boolean oDefault) {
    if(list.size() <= index) return oDefault;
    return WUtil.toBooleanObj(get(index), oDefault);
  }
  
  public String getString(int index) {
    return WUtil.toString(get(index), null);
  }
  
  public String getString(int index, String sDefault) {
    if(list.size() <= index) return sDefault;
    return WUtil.toString(get(index), sDefault);
  }
  
  public String getString(int index, int iMaxLength) {
    return WUtil.toString(get(index), null, iMaxLength);
  }
  
  public String getString(int index, String sDefault, int iMaxLength) {
    if(list.size() <= index) return WUtil.toString(sDefault, null, iMaxLength);
    return WUtil.toString(get(index), sDefault, iMaxLength);
  }
  
  public String getUpperString(int index) {
    return WUtil.toUpperString(get(index), null);
  }
  
  public String getUpperString(int index, String sDefault) {
    if(list.size() <= index) return WUtil.toUpperString(sDefault, null);
    return WUtil.toUpperString(get(index), sDefault);
  }
  
  public String getUpperString(int index, String sDefault, int iMaxLength) {
    if(list.size() <= index) return WUtil.toUpperString(sDefault, null, iMaxLength);
    return WUtil.toUpperString(get(index), sDefault, iMaxLength);
  }
  
  public String getLowerString(int index) {
    return WUtil.toLowerString(get(index), null);
  }
  
  public String getLowerString(int index, String sDefault) {
    if(list.size() <= index) return WUtil.toLowerString(sDefault, null);
    return WUtil.toLowerString(get(index), sDefault);
  }
  
  public String getLowerString(int index, String sDefault, int iMaxLength) {
    if(list.size() <= index) return WUtil.toLowerString(sDefault, null, iMaxLength);
    return WUtil.toLowerString(get(index), sDefault, iMaxLength);
  }
  
  public String getEscapedText(int index, String sDefault) {
    if(list.size() <= index) return sDefault;
    return WUtil.toEscapedText(get(index), sDefault);
  }
  
  public String getHTMLText(int index, String sDefault) {
    if(list.size() <= index) return sDefault;
    return WUtil.toHTMLText(get(index), sDefault);
  }
  
  // 1.8+
  public java.time.LocalDate getLocalDate(int index) {
    return WUtil.toLocalDate(get(index), null);
  }
  
  // 1.8+
  public java.time.LocalDate getLocalDate(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toLocalDate(oDefault, null);
    return WUtil.toLocalDate(get(index), oDefault);
  }
  
  // 1.8+
  public java.time.LocalDateTime getLocalDateTime(int index) {
    return WUtil.toLocalDateTime(get(index), null);
  }
  
  // 1.8+
  public java.time.LocalDateTime getLocalDateTime(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toLocalDateTime(oDefault, null);
    return WUtil.toLocalDateTime(get(index), oDefault);
  }
  
  public java.util.Date getDate(int index) {
    return WUtil.toDate(get(index), null);
  }
  
  public java.util.Date getDate(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toDate(oDefault, null);
    return WUtil.toDate(get(index), oDefault);
  }
  
  public java.util.Date getDate(int index, Object oDefault, Object oTime) {
    if(list.size() <= index) {
      java.util.Date result = WUtil.toDate(oDefault, null);
      return WUtil.setTime(result, oTime);
    }
    java.util.Date result = WUtil.toDate(get(index), oDefault);
    return WUtil.setTime(result, oTime);
  }
  
  public java.util.Date getTime(int index) {
    return WUtil.toTime(get(index), null);
  }
  
  public java.util.Date getTime(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toTime(oDefault, null);
    return WUtil.toTime(get(index), oDefault);
  }
  
  public java.util.Calendar getCalendar(int index) {
    return WUtil.toCalendar(get(index), null);
  }
  
  public java.util.Calendar getCalendar(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toCalendar(oDefault, null);
    return WUtil.toCalendar(get(index), oDefault);
  }
  
  public java.util.Calendar getCalendar(int index, Object oDefault, Object oTime) {
    if(list.size() <= index) {
      java.util.Calendar result = WUtil.toCalendar(oDefault, null);
      return WUtil.setTime(result, oTime);
    }
    java.util.Calendar result = WUtil.toCalendar(get(index), oDefault);
    return WUtil.setTime(result, oTime);
  }
  
  public java.sql.Date getSQLDate(int index) {
    return WUtil.toSQLDate(get(index), null);
  }
  
  public java.sql.Date getSQLDate(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toSQLDate(oDefault, null);
    return WUtil.toSQLDate(get(index), oDefault);
  }
  
  public java.sql.Date getSQLDate(int index, Object oDefault, Object oTime) {
    if(list.size() <= index) {
      java.sql.Date result = WUtil.toSQLDate(oDefault, null);
      return WUtil.setTime(result, oTime);
    }
    java.sql.Date result = WUtil.toSQLDate(get(index), oDefault);
    return WUtil.setTime(result, oTime);
  }
  
  public java.sql.Time getSQLTime(int index) {
    return WUtil.toSQLTime(get(index), null);
  }
  
  public java.sql.Time getSQLTime(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toSQLTime(oDefault, null);
    return WUtil.toSQLTime(get(index), oDefault);
  }
  
  public java.sql.Timestamp getSQLTimestamp(int index) {
    return WUtil.toSQLTimestamp(get(index), null);
  }
  
  public java.sql.Timestamp getSQLTimestamp(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toSQLTimestamp(oDefault, null);
    return WUtil.toSQLTimestamp(get(index), oDefault);
  }
  
  public java.sql.Timestamp getSQLTimestamp(int index, Object oDefault, Object oTime) {
    if(list.size() <= index) {
      java.sql.Timestamp result = WUtil.toSQLTimestamp(oDefault, null);
      return (java.sql.Timestamp) WUtil.setTime(result, oTime);
    }
    java.sql.Timestamp result = WUtil.toSQLTimestamp(get(index), oDefault);
    return (java.sql.Timestamp) WUtil.setTime(result, oTime);
  }
  
  public int getIntDate(int index) {
    return WUtil.toIntDate(get(index), 0);
  }
  
  public int getIntDate(int index, int iDate) {
    if(list.size() <= index) return iDate;
    return WUtil.toIntDate(get(index), iDate);
  }
  
  public int getIntTime(int index) {
    return WUtil.toIntTime(get(index), 0);
  }
  
  public int getIntTime(int index, int iTime) {
    if(list.size() <= index) return iTime;
    return WUtil.toIntTime(get(index), iTime);
  }
  
  public String getStringTime(int index) {
    return WUtil.toStringTime(get(index), null);
  }
  
  public String getStringTime(int index, String sDefault) {
    if(list.size() <= index) return WUtil.toStringTime(sDefault, null);
    return WUtil.toStringTime(get(index), sDefault);
  }
  
  public String getFormattedDate(int index, Object locale) {
    return WUtil.formatDate(get(index), locale);
  }
  
  public String getFormattedTime(int index, boolean second, boolean millis) {
    return WUtil.formatTime(get(index), second, millis);
  }
  
  public String getFormattedDateTime(int index, Object locale, boolean second) {
    return WUtil.formatDateTime(get(index), locale, second);
  }
  
  public String getFormattedCurrency(int index, Object locale, String left, String right) {
    return WUtil.formatCurrency(get(index), locale, left, right);
  }
  
  public Vector getVector(int index) {
    return WUtil.toVector(get(index), false);
  }
  
  public Vector getVector(int index, boolean notNull) {
    if(list.size() <= index && notNull) return new Vector();
    return WUtil.toVector(get(index), notNull);
  }
  
  public Vector getVector(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toVector(oDefault, null);
    return WUtil.toVector(get(index), oDefault);
  }
  
  public List getList(int index) {
    return WUtil.toList(get(index), false);
  }
  
  public List getList(int index, boolean notNull) {
    if(list.size() <= index && notNull) return new ArrayList();
    return WUtil.toList(get(index), notNull);
  }
  
  public List getList(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toList(oDefault, null);
    return WUtil.toList(get(index), oDefault);
  }
  
  public <T> List<T> getList(int index, Class<T> itemsClass, Object oDefault) {
    if(list.size() <= index) return WUtil.toList(oDefault, itemsClass, null);
    return WUtil.toList(get(index), itemsClass, oDefault);
  }
  
  public List<Map<String,Object>> getListOfMapObject(int index) {
    if(list.size() <= index) return new ArrayList<Map<String,Object>>();
    return WUtil.toListOfMapObject(get(index));
  }
  
  public Hashtable getHashtable(int index) {
    return WUtil.toHashtable(get(index), false);
  }
  
  public Hashtable getHashtable(int index, boolean notNull) {
    if(list.size() <= index && notNull) return new Hashtable();
    return WUtil.toHashtable(get(index), notNull);
  }
  
  public Hashtable getHashtable(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toHashtable(oDefault, null);
    return WUtil.toHashtable(get(index), oDefault);
  }
  
  public Map getMap(int index) {
    return WUtil.toMap(get(index), false);
  }
  
  public Map getMap(int index, boolean notNull) {
    if(list.size() <= index && notNull) return new HashMap();
    return WUtil.toMap(get(index), notNull);
  }
  
  public Map getMap(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toMap(oDefault, null);
    return WUtil.toMap(get(index), oDefault);
  }
  
  public Object getValue(int index, Object key, Object oDefault) {
    if(list.size() <= index) return oDefault;
    return WUtil.getValue(get(index), key, oDefault);
  }
  
  public <T> T getBean(int index, Class<T> beanClass) {
    Object item = get(index);
    if(item == null) return null;
    if(beanClass.isAssignableFrom(item.getClass())) {
      return (T) item;
    }
    Map mapValues = WUtil.toMap(item, false);
    if(mapValues == null) return null;
    return WUtil.populateBean(beanClass, mapValues);
  }
  
  public Map<String,Object> getMapObject(int index) {
    return WUtil.toMapObject(get(index));
  }
  
  public Set getSet(int index) {
    return WUtil.toSet(get(index), false);
  }
  
  public Set getSet(int index, boolean notNull) {
    if(list.size() <= index && notNull) return new HashSet();
    return WUtil.toSet(get(index), notNull);
  }
  
  public Set getSet(int index, Object oDefault) {
    if(list.size() <= index) return WUtil.toSet(oDefault, null);
    return WUtil.toSet(get(index), oDefault);
  }
  
  public String[] getArrayOfString(int index) {
    return WUtil.toArrayOfString(get(index), false);
  }
  
  public String[] getArrayOfString(int index, boolean notNull) {
    if(list.size() <= index && notNull) return new String[0];
    return WUtil.toArrayOfString(get(index), notNull);
  }
  
  public int[] getArrayOfInt(int index) {
    return WUtil.toArrayOfInt(get(index), false);
  }
  
  public int[] getArrayOfInt(int index, boolean notNull) {
    if(list.size() <= index && notNull) return new int[0];
    return WUtil.toArrayOfInt(get(index), notNull);
  }
  
  public double[] getArrayOfDouble(int index) {
    return WUtil.toArrayOfDouble(get(index), false);
  }
  
  public double[] getArrayOfDouble(int index, boolean notNull) {
    if(list.size() <= index && notNull) return new double[0];
    return WUtil.toArrayOfDouble(get(index), notNull);
  }
  
  public byte[] getArrayOfByte(int index) {
    return WUtil.toArrayOfByte(get(index), false);
  }
  
  public byte[] getArrayOfByte(int index, boolean notNull) {
    if(list.size() <= index && notNull) return new byte[0];
    return WUtil.toArrayOfByte(get(index), notNull);
  }
  
  // add methods
  
  public boolean add(int i) {
    return add(new Integer(i));
  }
  
  public boolean add(long l) {
    return add(new Long(l));
  }
  
  public boolean add(double d) {
    return add(new Double(d));
  }
  
  public boolean add(boolean b) {
    return add(new Boolean(b));
  }
  
  public boolean addBoolean(Object object) {
    return add(WUtil.toBooleanObj(object, null));
  }
  
  public boolean addString(Object object) {
    return add(WUtil.toString(object, null));
  }
  
  public boolean addUpper(Object object) {
    return add(WUtil.toUpperString(object, null));
  }
  
  public boolean addLower(Object object) {
    return add(WUtil.toLowerString(object, null));
  }
  
  public boolean addDate(Object object) {
    return add(WUtil.toDate(object, null));
  }
  
  public boolean addCalendar(Object object) {
    return add(WUtil.toCalendar(object, null));
  }
  
  public boolean addTime(Object object) {
    return add(WUtil.toTime(object, null));
  }
  
  public boolean addDateTime(Object date, Object time) {
    return add(WUtil.toTimeCalendar(date, time, null));
  }
  
  public boolean addDate(int iYYYYMMDD) {
    return add(WUtil.toDate(iYYYYMMDD, 0));
  }
  
  public boolean addCalendar(int iYYYYMMDD) {
    return add(WUtil.toCalendar(iYYYYMMDD, 0));
  }
  
  public boolean addTime(int iTime) {
    return add(WUtil.intToTime(iTime));
  }
  
  public boolean addNumber(Object object, Object oDefault) {
    return add(WUtil.toNumber(object, oDefault));
  }
  
  public boolean addNumber(Object object, Object oDefault, int decimal) {
    Number number = WUtil.toNumber(object, oDefault);
    if(number != null) {
      double dValue = number.doubleValue();
      return add(new Double(WUtil.round(dValue, decimal)));
    }
    return add(number);
  }
  
  public boolean addBigDecimal(Object object, BigDecimal oDefault) {
    return add(WUtil.toBigDecimal(object, oDefault));
  }
  
  public boolean addBigInteger(Object object, BigInteger oDefault) {
    return add(WUtil.toBigInteger(object, oDefault));
  }
  
  public boolean add(int iId, String sDesc) {
    if(iId == 0 &&(sDesc == null || sDesc.length() == 0)) {
      return false;
    }
    if(sDesc == null) sDesc = String.valueOf(iId);
    List list = new ArrayList(2);
    list.add(new Integer(iId));
    list.add(sDesc);
    return add(list);
  }
  
  public boolean add(int iId, String sCode, String sDesc) {
    if(iId == 0 &&(sCode == null || sCode.length() == 0)) {
      return false;
    }
    if(sCode == null) sCode = String.valueOf(iId);
    if(sDesc == null) sDesc = sCode;
    List list = new ArrayList(3);
    list.add(new Integer(iId));
    list.add(sCode);
    list.add(sDesc);
    return add(list);
  }
  
  public boolean add(Object oId, String sDesc) {
    if(oId   == null) return false;
    if(sDesc == null) sDesc = oId.toString();
    List list = new ArrayList(2);
    list.add(oId);
    list.add(sDesc);
    return add(list);
  }
  
  public boolean add(Object oId, String sCode, String sDesc) {
    if(oId   == null) return false;
    if(sCode == null) sCode = oId.toString();
    if(sDesc == null) sDesc = sCode;
    List list = new ArrayList(3);
    list.add(oId);
    list.add(sCode);
    list.add(sDesc);
    return add(list);
  }
  
  public boolean addNotNull(Object e) {
    if(e == null) return false;
    return add(e);
  }
  
  public boolean addNotZero(int i) {
    if(i == 0) return false;
    return add(new Integer(i));
  }
  
  public boolean addNotZero(int iId, String sCode) {
    if(iId == 0) return false;
    return add(iId, sCode);
  }
  
  public boolean addNotZero(int iId, String sCode, String sDesc) {
    if(iId == 0) return false;
    return add(iId, sCode, sDesc);
  }
  
  public boolean isBlank(int index) {
    return WUtil.isBlank(get(index));
  }
  
  public boolean isBlank(int index, int iBlankValue) {
    return WUtil.isBlank(get(index), iBlankValue);
  }
}
