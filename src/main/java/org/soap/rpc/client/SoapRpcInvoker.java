package org.soap.rpc.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.rpc.client.AsyncCallback;
import org.rpc.client.HttpRpcClientTransport;
import org.rpc.client.RpcClientTransport;
import org.rpc.client.RpcInvoker;
import org.soap.rpc.SoapRpcContentHandler;
import org.xml.rpc.XmlRpcSerializer;

@SuppressWarnings({"rawtypes","unchecked"})
public
class SoapRpcInvoker implements RpcInvoker
{
  private RpcClientTransport transport;
  private boolean boLegacy = true;
  
  public SoapRpcInvoker()
  {
  }
  
  public SoapRpcInvoker(RpcClientTransport transport)
  {
    this.transport = transport;
  }
  
  public SoapRpcInvoker(URL url)
  {
    this.transport = new HttpRpcClientTransport(url);
  }
  
  public SoapRpcInvoker(String sURL)
    throws MalformedURLException
  {
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
    StringBuffer sbArgs = new StringBuffer(colArgs != null ? colArgs.size() * 5 : 0 + 15);
    if(colArgs == null || colArgs.size() == 0) {
      sbArgs.append("<args>[]</args>");
    }
    else {
      String sJSONArray = new JSONArray(colArgs).toString();
      sbArgs.append("<args>" + XmlRpcSerializer.normalizeString(sJSONArray) + "</args>");
    }
    StringBuffer requestData = new StringBuffer(248 + sMethod.length() + sbArgs.length());
    requestData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    requestData.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">");
    requestData.append("<s:Body>");
    requestData.append("<execute xmlns=\"http://soap.rpc.org\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">");
    requestData.append("<method>" + sMethod + "</method>");
    requestData.append(sbArgs);
    requestData.append("</execute>");
    requestData.append("</s:Body>");
    requestData.append("</s:Envelope>");
    
    Map headers = new HashMap(5);
    headers.put("Content-Type", "text/xml");
    headers.put("SOAPAction",   "\"http://soap.rpc.org/execute\"");
    
    String responseData;
    try {
      responseData = transport.call(requestData.toString(), headers);
    }
    catch(Exception e) {
      throw new IOException("unable to get data from transport");
    }
    
    SoapRpcContentHandler contentHandler = new SoapRpcContentHandler(boLegacy);
    contentHandler.load(responseData);
    if(contentHandler.isFault()) {
      String sFaultCode = contentHandler.getFaultCode();
      if(sFaultCode != null && sFaultCode.length() > 0 && !sFaultCode.equals("0")) {
        int iFaultCode = 0;
        try { iFaultCode = Integer.parseInt(sFaultCode); } catch(Exception ex) {}
        if(iFaultCode != 0) {
          throw new XmlRpcException(iFaultCode, contentHandler.getFaultString());
        }
      }
      throw new Exception(contentHandler.getFaultString());
    }
    return contentHandler.getResult();
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

