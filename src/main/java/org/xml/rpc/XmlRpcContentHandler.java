package org.xml.rpc;

import java.io.ByteArrayInputStream;
import java.util.*;

import org.rpc.util.Base64Coder;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

@SuppressWarnings({"rawtypes","unchecked"})
public
class XmlRpcContentHandler implements ContentHandler
{
  private String sCurrentValue;
  
  private boolean boLegacy = false;
  
  private String sMethod;
  private boolean boValue = false;
  private boolean boFault = false;
  private List   listParams;
  private Object oData;
  private Object oCollection;
  private Stack  stackCollections;
  private Object oValue;
  private String sMember;
  
  public XmlRpcContentHandler()
  {
  }
  
  public XmlRpcContentHandler(boolean boLegacy)
  {
    this.boLegacy = boLegacy;
  }
  
  public
  void load(String sText)
    throws Exception
  {
    InputSource inputSource = new InputSource(new ByteArrayInputStream(sText.getBytes()));
    XMLReader xmlReader = XMLReaderFactory.createXMLReader();
    xmlReader.setContentHandler(this);
    xmlReader.parse(inputSource);
  }
  
  public
  String getMethod()
  {
    return sMethod;
  }
  
  public
  List getParams()
  {
    if(listParams == null) {
      if(boLegacy) {
        listParams = new Vector();
      }
      else {
        listParams = new ArrayList();
      }
    }
    if(oData != null) listParams.add(oData);
    return listParams;
  }
  
  public
  Object getData()
  {
    if(oData == null) {
      if(listParams != null && listParams.size() > 0) {
        return listParams.get(0);
      }
    }
    else
    if(oData instanceof String) {
      if(oData.equals("null")) return null;
    }
    return oData;
  }
  
  public
  boolean isFault()
  {
    return boFault;
  }
  
  public
  void startDocument()
    throws SAXException
  {
    stackCollections = new Stack();
  }
  
  public
  void endDocument()
    throws SAXException
  {
  }
  
  public
  void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    sCurrentValue = "";
    if(localName.equals("array")) {
      List array = null;
      if(boLegacy) {
        stackCollections.add(array = new Vector());
      }
      else {
        stackCollections.add(array = new ArrayList());
      }
      if(oCollection instanceof List) {
        ((List) oCollection).add(array);
      }
      else
      if(oCollection instanceof Map) {
        if(sMember != null) {
          ((Map) oCollection).put(sMember, array);
        }
      }
      
      oCollection = array;
      oValue      = null;
      sMember     = null;
      
      if(oData == null) oData = array;
    }
    else
    if(localName.equals("struct")) {
      Map map = null;
      if(boLegacy) {
        stackCollections.add(map = new Hashtable());
      }
      else {
        stackCollections.add(map = new HashMap());
      }
      if(oCollection instanceof List) {
        ((List) oCollection).add(map);
      }
      else
      if(oCollection instanceof Map) {
        if(sMember != null) {
          ((Map) oCollection).put(sMember, map);
        }
      }
      
      oCollection = map;
      oValue      = null;
      sMember     = null;
      
      if(oData == null) oData = map;
    }
    else
    if(localName.equals("fault")) {
      boFault = true;
    }
    
    boValue = localName.equals("value");
  }
  
  public
  void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    if(localName.equals("name")) {
      sMember = sCurrentValue;
      return;
    }
    else
    if(localName.equals("data")) {
      oValue  = null;
      sMember = null;
      return;
    }
    else
    if(localName.equals("array")) {
      if(!stackCollections.isEmpty()) {
        stackCollections.pop();
        if(!stackCollections.isEmpty()) {
          oCollection = stackCollections.peek();
        }
        else {
          oCollection = null;
        }
      }
      else {
        oCollection = null;
      }
      oValue  = null;
      sMember = null;
      return;
    }
    else
    if(localName.equals("struct")) {
      if(!stackCollections.isEmpty()) {
        stackCollections.pop();
        if(!stackCollections.isEmpty()) {
          oCollection = stackCollections.peek();
        }
        else {
          oCollection = null;
        }
      }
      else {
        oCollection = null;
      }
      oValue  = null;
      sMember = null;
      return;
    }
    else
    if(localName.equals("value")) {
      if(boValue) {
        oValue  = sCurrentValue;
        boValue = false;
      }
      else {
        return;
      }
    }
    else
    if(localName.equals("string")) {
      oValue = sCurrentValue;
    }
    else
    if(localName.equals("nil")) {
      oValue = null;
    }
    else
    if(localName.equals("i4") || localName.equals("int")) {
      if(sCurrentValue == null || sCurrentValue.length() == 0) {
        oValue = new Integer(0);
      }
      else {
        try{ oValue = new Integer(sCurrentValue); } catch(Throwable th) {}
      }
    }
    else
    if(localName.equals("i8") || localName.equals("long")) {
      if(sCurrentValue == null || sCurrentValue.length() == 0) {
        oValue = new Long(0l);
      }
      else {
        try{ oValue = new Long(sCurrentValue); } catch(Throwable th) {}
      }
    }
    else
    if(localName.equals("double")) {
      if(sCurrentValue == null || sCurrentValue.length() == 0) {
        oValue = new Double(0.0d);
      }
      else {
        try{ oValue = new Double(sCurrentValue.replace(',', '.')); } catch(Throwable th) {}
      }
    }
    else
    if(localName.equals("boolean")) {
      if(sCurrentValue == null || sCurrentValue.length() == 0) {
        oValue = Boolean.FALSE;
      }
      else
      if(sCurrentValue.equals("0")) {
        oValue = Boolean.FALSE;
      }
      else {
        oValue = Boolean.TRUE;
      }
    }
    else
    if(localName.startsWith("dateTime")) {
      if(sCurrentValue == null || sCurrentValue.length() < 17) {
        oValue = null;
      }
      else {
        try {
          int iYear  = Integer.parseInt(sCurrentValue.substring( 0,  4));
          int iMonth = Integer.parseInt(sCurrentValue.substring( 4,  6));
          int iDay   = Integer.parseInt(sCurrentValue.substring( 6,  8));
          int iHour  = Integer.parseInt(sCurrentValue.substring( 9, 11));
          int iMin   = Integer.parseInt(sCurrentValue.substring(12, 14));
          int iSec   = Integer.parseInt(sCurrentValue.substring(15, 17));
          Calendar cal = new GregorianCalendar(iYear, iMonth-1, iDay, iHour, iMin, iSec);
          oValue = cal.getTime();
        }
        catch(Exception ex) {
          oValue = null;
        }
      }
    }
    else
    if(localName.equals("base64")) {
      if(sCurrentValue == null || sCurrentValue.length() < 0) {
        oValue = new byte[0];
      }
      else {
        oValue = Base64Coder.decode(sCurrentValue);
      }
    }
    else
    if(localName.equals("methodName")) {
      sMethod = sCurrentValue;
      return;
    }
    else
    if(localName.equals("param")) {
      if(listParams == null) listParams = new ArrayList();
      listParams.add(oData);
      oData       = null;
      oCollection = null;
      oValue      = null;
      sMember     = null;
      return;
    }
    else
    if(localName.equals("params")) {
      oData       = null;
      oCollection = null;
      oValue      = null;
      sMember     = null;
      return;
    }
    
    if(oCollection instanceof List) {
      if(oValue instanceof String) {
        String sValue = (String) oValue;
        if(sValue.equalsIgnoreCase("null")) {
          ((List) oCollection).add(null);
        }
        else {
          ((List) oCollection).add(sValue);
        }
      }
      else {
        ((List) oCollection).add(oValue);
      }
    }
    else
    if(oCollection instanceof Map) {
      if(sMember != null && oValue != null) {
        if(oValue instanceof String) {
          String sValue = (String) oValue;
          if(sValue.equalsIgnoreCase("null")) {
            if(!(oCollection instanceof Hashtable)) {
              ((Map) oCollection).put(sMember, null);
            }
          }
          else {
            ((Map) oCollection).put(sMember, sValue);
          }
        }
        else {
          ((Map) oCollection).put(sMember, oValue);
        }
      }
    }
    
    if(oData == null) {
      if(oValue instanceof String) {
        String sValue = (String) oValue;
        if(sValue.equalsIgnoreCase("null")) {
          oData = null;
        }
        else {
          oData = oValue;
        }
      }
      else {
        oData = oValue;
      }
    }
  }
  
  public
  void characters(char[] ch, int start, int length)
    throws SAXException
  {
    sCurrentValue += new String(ch, start, length);
  }
  
  public void setDocumentLocator(Locator locator) {}
  public void startPrefixMapping(String prefix, String uri) throws SAXException {}
  public void endPrefixMapping(String prefix) throws SAXException {}
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
  public void processingInstruction(String target, String data) throws SAXException {}
  public void skippedEntity(String name) throws SAXException {}
}
