package org.xml.rpc.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.rpc.client.AsyncCallback;
import org.rpc.client.RpcClientTransport;
import org.rpc.client.HttpRpcClientTransport;
import org.rpc.client.RpcInvoker;
import org.xml.rpc.XmlRpcContentHandler;
import org.xml.rpc.XmlRpcSerializer;

@SuppressWarnings({"rawtypes"})
public
class XmlRpcInvoker implements RpcInvoker
{
  private RpcClientTransport transport;
  private boolean boLegacy = true;
  private String sEncoding = "ISO-8859-1";
  
  public XmlRpcInvoker()
  {
    detectsEncoding();
  }
  
  public XmlRpcInvoker(RpcClientTransport transport)
  {
    detectsEncoding();
    this.transport = transport;
  }
  
  public XmlRpcInvoker(URL url)
  {
    detectsEncoding();
    this.transport = new HttpRpcClientTransport(url);
  }
  
  public XmlRpcInvoker(String sURL)
    throws MalformedURLException
  {
    detectsEncoding();
    this.transport = new HttpRpcClientTransport(new URL(sURL));
  }
  
  public
  void setTransport(RpcClientTransport transport)
  {
    this.transport = transport;
  }
  
  public
  RpcClientTransport getTransport()
  {
    return transport;
  }
  
  public
  String getURL()
  {
    if(transport instanceof HttpRpcClientTransport) {
      return ((HttpRpcClientTransport) transport).getURL();
    }
    return null;
  }
  
  public
  boolean isLegacy()
  {
    return boLegacy;
  }
  
  public
  void setLegacy(boolean boLegacy)
  {
    this.boLegacy = boLegacy;
  }
  
  public
  Object invoke(String sMethod, Collection colArgs)
    throws Throwable
  {
    return invoke(transport, sMethod, colArgs);
  }
  
  public
  void invokeAsync(String sMethod, Collection colArgs, AsyncCallback callback)
  {
    Thread thread = new Thread(new Worker(transport, sMethod, colArgs, callback));
    thread.start();
  }
  
  protected
  Object invoke(RpcClientTransport transport, String sMethod, Collection colArgs)
    throws Throwable
  {
     StringBuffer sbParams = new StringBuffer(colArgs != null ? colArgs.size() * 40 : 0 + 17);
    sbParams.append("<params>");
    if(colArgs != null) {
      Iterator iterator = colArgs.iterator();
      while(iterator.hasNext()) {
        Object oParam = iterator.next();
        if(oParam != null) {
          sbParams.append("<param>");
          sbParams.append(XmlRpcSerializer.serialize(oParam, boLegacy));
          sbParams.append("</param>");
        }
        else
        if(!boLegacy) {
          sbParams.append("<param><value><nil/></value></param>");
        }
        else {
          sbParams.append("<param><value></value></param>");
        }
      }
    }
    sbParams.append("</params>");
    
    StringBuffer requestData = new StringBuffer(95 + sMethod.length() + sbParams.length());
    requestData.append("<?xml version=\"1.0\" encoding=\"" + sEncoding + "\"?>");
    requestData.append("<methodCall>");
    requestData.append("<methodName>");
    requestData.append(sMethod);
    requestData.append("</methodName>");
    requestData.append(sbParams);
    requestData.append("</methodCall>");
    
    String responseData;
    try {
      responseData = transport.call(requestData.toString());
    }
    catch(Exception e) {
      throw new IOException("unable to get data from transport");
    }
    
    XmlRpcContentHandler contentHandler = new XmlRpcContentHandler(boLegacy);
    contentHandler.load(responseData);
    if(contentHandler.isFault()) {
      Object oFault = contentHandler.getData();
      if(oFault instanceof Map) {
        String sFaultString = (String) ((Map) oFault).get("faultString");
        Integer oFaultCode  = (Integer)((Map) oFault).get("faultCode");
        if(oFaultCode != null && oFaultCode.intValue() != 0) {
          throw new XmlRpcException(oFaultCode.intValue(), sFaultString);
        }
        throw new Exception(sFaultString);
      }
      else
      if(oFault instanceof String) {
        throw new Exception((String) oFault);
      }
      else {
        throw new Exception("exception call");
      }
    }
    return contentHandler.getData();
  }
  
  protected
  void detectsEncoding()
  {
    String sFileEncoding = System.getProperty("file.encoding");
    if(sFileEncoding != null && sFileEncoding.startsWith("Cp")) {
      sEncoding = "ISO-8859-1";
    }
    else
    if(sFileEncoding != null && sFileEncoding.startsWith("ISO-")) {
      sEncoding = sFileEncoding;
    }
    else
    if(sFileEncoding != null && sFileEncoding.startsWith("UTF-")) {
      sEncoding = sFileEncoding;
    }
    else {
      sEncoding = "ISO-8859-1";
    }
  }
  
  public
  String toString()
  {
    return this.getClass().getName();
  }
  
  class Worker implements Runnable
  {
    private RpcClientTransport w_trans;
    private String             w_method;
    private Collection         w_args;
    private AsyncCallback      w_callback;
    
    public Worker(RpcClientTransport transport, String sMethod, Collection colArgs, AsyncCallback callback)
    {
      this.w_trans    = transport;
      this.w_method   = sMethod;
      this.w_args     = colArgs;
      this.w_callback = callback;
    }
    
    public
    void run()
    {
      try {
        Object oResult = invoke(w_trans, w_method, w_args);
        if(w_callback != null) {
          w_callback.handleResult(w_method, w_args, oResult);
        }
      }
      catch(Throwable th) {
        if(w_callback != null) {
          w_callback.handleError(w_method, w_args, th);
        }
      }
    }
  }
}
