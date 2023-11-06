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

package org.json.rpc.server;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;

import org.dew.util.RefUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.rpc.commons.RpcRemoteException;

import org.rpc.server.RpcAudit;
import org.rpc.server.RpcExecutor;
import org.rpc.server.RpcServerTransport;
import org.rpc.server.RpcTracer;
import org.rpc.server.RpcUtil;

import org.util.WUtil;

@SuppressWarnings({"rawtypes","unchecked"})
public
class JsonRpcExecutor implements RpcExecutor
{
  protected Map       handlers;
  protected RpcTracer tracer;
  protected RpcAudit  audit;
  
  public JsonRpcExecutor()
  {
    System.err.println(this.getClass().getName() + " ver. 2.9 instantiated at " + WUtil.formatDateTime(new Date(), "-", true));
    this.handlers = new HashMap();
  }
  
  public JsonRpcExecutor(Map mapHandlers)
  {
    System.err.println(this.getClass().getName() + " ver. 2.9 instantiated at " + WUtil.formatDateTime(new Date(), "-", true));
    this.handlers = mapHandlers;
  }
  
  public
  void addHandler(String name, Object handler)
  {
    synchronized(this.handlers) {
      if(this.handlers.containsKey(name)) {
        throw new IllegalArgumentException("handler " + name + " already exists");
      }
      this.handlers.put(name, handler);
    }
  }
  
  public
  void removeHandler(String name)
  {
    synchronized(this.handlers) {
      this.handlers.remove(name);
    }
  }
  
  public
  void setTracer(RpcTracer tracer)
  {
    this.tracer = tracer;
  }
  
  public
  void setAudit(RpcAudit audit)
  {
    this.audit = audit;
  }
  
  public
  void execute(RpcServerTransport transport)
  {
    String[] requestData = null;
    try {
      requestData = transport.readRequest("application/json");
    }
    catch(Throwable t) {
      t.printStackTrace();
      return;
    }
    jsonrpc_execute(requestData, transport);
  }
  
  private
  void jsonrpc_execute(String[] requestData, RpcServerTransport transport)
  {
    String methodName = null;
    JSONArray params = null;
    
    JSONObject resp = new JSONObject();
    resp.put("jsonrpc", "2.0");
    
    JSONObject req = null;
    try {
      req = new JSONObject(requestData[1]);
    }
    catch(Throwable t) {
      t.printStackTrace();
      if(tracer != null) tracer.trace(requestData[0], requestData[1], t);
      int errorCode        = PARSE_ERROR_CODE;
      String  errorMessage = "unable to parse json-rpc request";
      String  errorData    = RefUtil.getStackTrace(t);
      RpcUtil.jsonrpc_sendError(transport, resp, errorCode, errorMessage, errorData);
      return;
    }
    
    try {
      resp.put("id", req.get("id"));
      
      methodName = req.getString("method");
      params     = req.getJSONArray("params");
      if(params == null) params = new JSONArray();
      
      if(methodName.indexOf('.') < 0) {
        methodName = RpcUtil.completeMethodName(methodName, requestData);
      }
    }
    catch(Throwable t) {
      t.printStackTrace();
      if(tracer != null) tracer.trace(requestData[0], requestData[1], t);
      int     errorCode    = INVALID_REQUEST_ERROR_CODE;
      String  errorMessage = "unable to read request";
      String  errorData    = RefUtil.getStackTrace(t);
      RpcUtil.jsonrpc_sendError(transport, resp, errorCode, errorMessage, errorData);
      return;
    }
    
    if(!transport.checkAuthorization(methodName)) return;
    
    try {
      Object result = RpcUtil.executeMethod(handlers, audit, methodName, params.toVector());
      
      resp.put("result", result);
    }
    catch(Throwable t) {
      if(tracer != null) tracer.trace(requestData[0], requestData[1], t);
      if(t instanceof RpcRemoteException) {
        RpcRemoteException jrre = (RpcRemoteException) t;
        RpcUtil.jsonrpc_sendError(transport, resp, jrre.getCode(), jrre.getMessage(), jrre.getData());
        return;
      }
      else
      if(t instanceof XmlRpcException) {
        XmlRpcException xre = (XmlRpcException) t;
        RpcUtil.jsonrpc_sendError(transport, resp, xre.code, xre.getMessage(), null);
        return;
      }
      int errorCode       = SERVER_ERROR_START - 1;
      String errorMessage = t.getMessage();
      String errorData    = RefUtil.getStackTrace(t);
      RpcUtil.jsonrpc_sendError(transport, resp, errorCode, errorMessage, errorData);
      return;
    }
    
    try {
      if(tracer != null) tracer.trace(requestData[0], requestData[1], resp.toString(), methodName);
      transport.writeResponse("application/json", resp.toString(), false);
    }
    catch(Throwable t) {
      t.printStackTrace();
      if(tracer != null) tracer.trace(requestData[0], requestData[1], resp.toString(), methodName, t);
    }
  }
}
