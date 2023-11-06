package org.soap.rpc;

import java.io.ByteArrayInputStream;
import java.util.*;

import org.json.JSON;
import org.json.JSONArray;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

@SuppressWarnings({"rawtypes"})
public
class SoapRpcContentHandler implements ContentHandler
{
  private String sCurrentValue;
  
  private boolean boLegacy = false;
  
  private String sMethod;
  private String sArgs;
  private String sReturn;
  private String sFaultCode;
  private String sFaultString;
  
  public SoapRpcContentHandler()
  {
  }
  
  public SoapRpcContentHandler(boolean boLegacy)
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
  String getArgs()
  {
    return sArgs;
  }
  
  public
  List getListArgs()
  {
    if(sArgs == null || sArgs.length() == 0) {
      if(boLegacy) {
        return new Vector();
      }
      else {
        return new ArrayList();
      }
    }
    char cFirst = '\0';
    for(int i = 0; i < sArgs.length(); i++) {
      char c = sArgs.charAt(i);
      if(c > 32) {
        cFirst = c;
        break;
      }
    }
    if(cFirst != '[') sArgs = "[" + sArgs + "]";
    if(boLegacy) {
      return new JSONArray(sArgs).toVector();
    }
    return new JSONArray(sArgs).toArrayList();
  }
  
  public
  Object getResult()
  {
    if(boLegacy) {
      return JSON.parseLegacy(sReturn);
    }
    else {
      return JSON.parse(sReturn);
    }
  }
  
  public
  boolean isFault()
  {
    return sFaultCode != null && sFaultCode.length() > 0;
  }
  
  public
  String getFaultCode()
  {
    return sFaultCode;
  }
  
  public
  String getFaultString()
  {
    return sFaultString;
  }
  
  public
  void startDocument()
    throws SAXException
  {
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
  }
  
  public
  void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    if(localName.equalsIgnoreCase("method")) {
      sMethod = sCurrentValue;
      return;
    }
    else
    if(localName.equalsIgnoreCase("args")) {
      sArgs = sCurrentValue;
      return;
    }
    else
    if(localName.equalsIgnoreCase("executeReturn") || localName.equalsIgnoreCase("executeResult")) {
      sReturn = sCurrentValue;
      return;
    }
    else
    if(localName.equalsIgnoreCase("faultcode")) {
      sFaultCode = sCurrentValue;
      return;
    }
    else
    if(localName.equalsIgnoreCase("faultstring")) {
      sFaultString = sCurrentValue;
      return;
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

