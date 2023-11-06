package org.json.rpc.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.rpc.client.AsyncCallback;
import org.rpc.client.RpcClient;
import org.rpc.client.RpcInvoker;

@SuppressWarnings({"rawtypes"})
public
class JsonRpcClient implements RpcClient, AsyncCallback
{
  public final static int iDEFAULT_TIMEOUT = 30000;
  
  protected RpcInvoker rpcInvoker;
  protected int iTimeOut = iDEFAULT_TIMEOUT;
  
  protected String    sMethodCall = null;
  protected Object    oResult     = null;
  protected Throwable oException  = null;
  
  public
  JsonRpcClient()
  {
    String sURL = System.getProperty("org.rpc.client.url");
    if(sURL == null || sURL.length() == 0) sURL = "http://localhost/RPC/ws";
    try {
      this.rpcInvoker = new JsonRpcInvoker(sURL);
      this.rpcInvoker.getTransport().setTimeOut(iDEFAULT_TIMEOUT);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  
  public
  JsonRpcClient(URL url)
  {
    this.rpcInvoker = new JsonRpcInvoker(url);
    this.rpcInvoker.getTransport().setTimeOut(iDEFAULT_TIMEOUT);
  }
  
  public
  JsonRpcClient(String sURL)
    throws MalformedURLException
  {
    this.rpcInvoker = new JsonRpcInvoker(sURL);
    this.rpcInvoker.getTransport().setTimeOut(iDEFAULT_TIMEOUT);
  }
  
  public
  void setURL(String sURL)
    throws MalformedURLException
  {
    this.rpcInvoker = new JsonRpcInvoker(sURL);
    this.rpcInvoker.getTransport().setTimeOut(iTimeOut);
  }
  
  public
  String getURL()
  {
    if(rpcInvoker == null) return null;
    return rpcInvoker.getURL();
  }
  
  public
  boolean isLegacy()
  {
    if(rpcInvoker == null) return false;
    return rpcInvoker.isLegacy();
  }
  
  public
  void setLegacy(boolean boLegacy)
  {
    rpcInvoker.setLegacy(boLegacy);
  }
  
  public
  void setTimeOut(int iTimeOut)
  {
    this.iTimeOut = iTimeOut;
    this.rpcInvoker.getTransport().setTimeOut(iTimeOut);
  }
  
  public
  int getTimeOut()
  {
    return iTimeOut;
  }
  
  public
  void setDefaultTimeOut()
  {
    setTimeOut(iDEFAULT_TIMEOUT);
  }
  
  public
  void setHeaders(Map headers)
  {
    rpcInvoker.getTransport().setHeaders(headers);
  }
  
  public
  Object execute(String sMethod, Collection colParameters)
    throws Exception
  {
    int iElapsed = 0;
    sMethodCall  = null;
    oResult      = null;
    oException   = null;
    
    if(iTimeOut > 0) {
      rpcInvoker.invokeAsync(sMethod, colParameters, this);
      while(!sMethod.equals(sMethodCall)) {
        Thread.sleep(100);
        iElapsed += 100;
        if(iElapsed > iTimeOut) {
          throw new Exception(sMethod + " call timed out at " + rpcInvoker.getURL());
        }
      }
    }
    else {
      rpcInvoker.invokeAsync(sMethod, colParameters, null);
      return null;
    }
    
    if(oException != null) {
      String sMessage = oException.getMessage();
      if(oException instanceof IOException) {
        throw new IOException(sMessage);
      }
      else {
        throw new Exception(sMessage);
      }
    }
    return oResult;
  }
  
  public
  void execute(String sMethod, java.util.Collection colParameters, AsyncCallback callBack)
  {
    rpcInvoker.invokeAsync(sMethod, colParameters, callBack);
  }
  
  public
  void handleResult(String sMethod, Collection colArgs, Object result)
  {
    this.sMethodCall = sMethod;
    this.oResult     = result;
  }
  
  public
  void handleError(String sMethod, Collection colArgs, Throwable error)
  {
    this.sMethodCall = sMethod;
    this.oException  = error;
  }
  
  public
  String toString()
  {
    return rpcInvoker.toString();
  }
}
