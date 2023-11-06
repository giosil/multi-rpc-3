package org.xml.rpc;

public
class XmlRpc
{
  public static
  Object parse(String sText)
  {
    if(sText == null) return null;
    sText = sText.trim();
    if(sText.length() == 0) return "";
    try {
      XmlRpcContentHandler contentHanlder = new XmlRpcContentHandler();
      contentHanlder.load(sText);
      return contentHanlder.getData();
    }
    catch(Throwable th) {
      return null;
    }
  }
  
  public static
  Object parseLegacy(String sText)
  {
    if(sText == null) return null;
    sText = sText.trim();
    if(sText.length() == 0) return "";
    try {
      XmlRpcContentHandler contentHanlder = new XmlRpcContentHandler(true);
      contentHanlder.load(sText);
      return contentHanlder.getData();
    }
    catch(Throwable th) {
      return null;
    }
  }
  
  public static
  String stringify(Object object)
  {
    return XmlRpcSerializer.serialize(object);
  }
}
