/*
 * Copyright (C) 2011 ritwik.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json.rpc.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.rpc.commons.RpcRemoteException;
import org.rpc.client.AsyncCallback;
import org.rpc.client.HttpRpcClientTransport;
import org.rpc.client.RpcClientTransport;
import org.rpc.client.RpcInvoker;

@SuppressWarnings({"rawtypes"})
public
class JsonRpcInvoker implements RpcInvoker
{
  private Random rand = new Random();
  private RpcClientTransport transport;
  private boolean boLegacy = true;
  
  public JsonRpcInvoker()
  {
  }
  
  public JsonRpcInvoker(RpcClientTransport transport)
  {
    this.transport = transport;
  }
  
  public JsonRpcInvoker(URL url)
  {
    this.transport = new HttpRpcClientTransport(url);
  }
  
  public JsonRpcInvoker(String sURL)
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
    int id = rand.nextInt(Integer.MAX_VALUE);
    
    JSONObject req = new JSONObject();
    req.put("id",     id);
    req.put("method", sMethod);
    req.put("params", new JSONArray(colArgs));
    
    String requestData = req.toString();
    String responseData;
    try {
      responseData = transport.call(requestData);
    }
    catch(Exception e) {
      throw new IOException("unable to get data from transport: " + e);
    }
    
    JSONObject resp = new JSONObject(responseData);
    Object result = resp.opt("result");
    Object error  = resp.opt("error");
    if(error != null && !error.equals(null)) {
      if(error instanceof String) {
        throw new RpcRemoteException((String) error);
      }
      else
      if(error instanceof JSONObject) {
        int code       = ((JSONObject) error).optInt("code");
        String message = ((JSONObject) error).optString("message");
        Object data    = ((JSONObject) error).opt("data");
        String sData   = data != null ? data.toString() : null;
        throw new RpcRemoteException(code, message, sData);
      } else {
        throw new RpcRemoteException("unknown error: " + error.toString());
      }
    }
    if(boLegacy) {
      if(result instanceof JSONObject) {
        return ((JSONObject) result).toHashtable();
      }
      if(result instanceof JSONArray) {
        return ((JSONArray) result).toVector();
      }
    }
    else {
      if(result instanceof JSONObject) {
        return ((JSONObject) result).toHashMap();
      }
      if(result instanceof JSONArray) {
        return ((JSONArray) result).toArrayList();
      }
    }
    if(result instanceof JSONObject.Null) {
      return null;
    }
    return result;
  }
  
  public
  String toString()
  {
    return this.getClass().getName();
  }
  
  class Worker implements Runnable
  {
    private RpcClientTransport w_trans;
    private String        w_method;
    private Collection    w_args;
    private AsyncCallback w_callback;
    
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
