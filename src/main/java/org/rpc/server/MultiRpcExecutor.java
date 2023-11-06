package org.rpc.server;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcException;

import org.dew.util.RefUtil;

import org.json.JSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.rpc.commons.RpcRemoteException;

import org.soap.rpc.SoapRpcContentHandler;
import org.soap.rpc.server.SoapRpcExecutor;

import org.util.WUtil;

import org.xml.rpc.XmlRpcContentHandler;
import org.xml.rpc.XmlRpcSerializer;

@SuppressWarnings({"rawtypes","unchecked"})
public
class MultiRpcExecutor implements RpcExecutor
{
  protected Map       handlers;
  protected RpcTracer tracer;
  protected RpcAudit  audit;
  
  public MultiRpcExecutor()
  {
    System.err.println(this.getClass().getName() + " ver. 2.9 instantiated at " + WUtil.formatDateTime(new Date(), "-", true));
    this.handlers = new HashMap();
  }
  
  public MultiRpcExecutor(Map mapHandlers)
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
    String[] requestData;
    try {
      requestData = transport.readRequest(null);
    }
    catch(Throwable t) {
      t.printStackTrace();
      return;
    }
    
    String sContentType = requestData[0];
    if(sContentType != null && sContentType.equals("text/xml")) {
      xmlrpc_execute(requestData, transport);
    }
    else if(sContentType != null && sContentType.equals("application/soap+xml")) {
      soaprpc_execute(requestData, transport);
    }
    else {
      jsonrpc_execute(requestData, transport);
    }
  }
  
  public static
  boolean isWSDLRequest(HttpServletRequest request)
  {
    return SoapRpcExecutor.isWSDLRequest(request);
  }
  
  public static
  void sendWSDL(HttpServletRequest request, HttpServletResponse response)
  {
    SoapRpcExecutor.sendWSDL(request, response);
  }
  
  public static
  void sendWSDL(HttpServletRequest request, HttpServletResponse response, String sLocation)
  {
    SoapRpcExecutor.sendWSDL(request, response, sLocation);
  }
  
  private
  void xmlrpc_execute(String[] requestData, RpcServerTransport transport)
  {
    String methodName = null;
    List params = null;
    
    XmlRpcContentHandler req = null;
    try {
      req = new XmlRpcContentHandler(true);
      req.load(requestData[1]);
    }
    catch(Throwable t) {
      t.printStackTrace();
      if(tracer != null) tracer.trace(requestData[0], requestData[1], t);
      int    errorCode    = PARSE_ERROR_CODE;
      String errorMessage = "unable to parse xml-rpc request";
      RpcUtil.xmlrpc_sendError(transport, errorCode, errorMessage);
      return;
    }
    
    try {
      methodName = req.getMethod();
      params     = req.getParams();
      
      if(methodName.indexOf('.') < 0) {
        methodName = RpcUtil.completeMethodName(methodName, requestData);
      }
    }
    catch(Throwable t) {
      t.printStackTrace();
      if(tracer != null) tracer.trace(requestData[0], requestData[1], t);
      int    errorCode    = INVALID_REQUEST_ERROR_CODE;
      String errorMessage = "unable to read request";
      RpcUtil.xmlrpc_sendError(transport, errorCode, errorMessage);
      return;
    }
    
    if(!transport.checkAuthorization(methodName)) return;
    
    String sResult = null;
    try {
      Object result = RpcUtil.executeMethod(handlers, audit, methodName, params);
      
      sResult = XmlRpcSerializer.serialize(result);
    }
    catch(Throwable t) {
      if(tracer != null) tracer.trace(requestData[0], requestData[1], t);
      if(t instanceof RpcRemoteException) {
        RpcRemoteException rre = (RpcRemoteException) t;
        RpcUtil.xmlrpc_sendError(transport, rre.getCode(), rre.getMessage());
        return;
      }
      else if(t instanceof XmlRpcException) {
        XmlRpcException xre = (XmlRpcException) t;
        RpcUtil.xmlrpc_sendError(transport, xre.code, xre.getMessage());
        return;
      }
      String errorMessage = t.getMessage();
      if(errorMessage == null || errorMessage.length() == 0) {
        errorMessage = t.toString();
      }
      RpcUtil.xmlrpc_sendError(transport, 0, errorMessage);
      return;
    }
    
    StringBuffer responseData = null;
    try {
      responseData = new StringBuffer(sResult.length() + 108);
      String sEncoding = transport.getEncoding();
      if(sEncoding != null && sEncoding.length() > 0) {
        responseData.append("<?xml version=\"1.0\" encoding=\"" + sEncoding + "\"?><methodResponse><params><param>");
      }
      else {
        responseData.append("<?xml version=\"1.0\"?><methodResponse><params><param>");
      }
      responseData.append(sResult);
      responseData.append("</param></params></methodResponse>");
      
      if(tracer != null) tracer.trace(requestData[0], requestData[1], responseData.toString(), methodName);
      transport.writeResponse("text/xml", responseData.toString(), false);
    }
    catch(Throwable t) {
      t.printStackTrace();
      if(tracer != null) tracer.trace(requestData[0], requestData[1], responseData.toString(), methodName, t);
    }
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
      else if(t instanceof XmlRpcException) {
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
  
  private
  void soaprpc_execute(String[] requestData, RpcServerTransport transport)
  {
    String methodName = null;
    List params = null;
    
    SoapRpcContentHandler req = null;
    try {
      req = new SoapRpcContentHandler(true);
      req.load(requestData[1]);
    }
    catch(Throwable t) {
      t.printStackTrace();
      if(tracer != null) tracer.trace(requestData[0], requestData[1], t);
      int    errorCode    = PARSE_ERROR_CODE;
      String errorMessage = "unable to parse soap-rpc request";
      RpcUtil.soaprpc_sendError(transport, errorCode, errorMessage, null);
      return;
    }
    
    try {
      methodName = req.getMethod();
      params     = req.getListArgs();
      
      if(methodName.indexOf('.') < 0) {
        methodName = RpcUtil.completeMethodName(methodName, requestData);
      }
    }
    catch(Throwable t) {
      t.printStackTrace();
      if(tracer != null) tracer.trace(requestData[0], requestData[1], t);
      int    errorCode    = INVALID_REQUEST_ERROR_CODE;
      String errorMessage = "unable to read request";
      RpcUtil.soaprpc_sendError(transport, errorCode, errorMessage, null);
      return;
    }
    
    if(!transport.checkAuthorization(methodName)) return;
    
    String sResult = null;
    try {
      Object result = RpcUtil.executeMethod(handlers, audit, methodName, params);
      
      sResult = XmlRpcSerializer.normalizeString(JSON.stringify(result));
    }
    catch(Throwable t) {
      if(tracer != null) tracer.trace(requestData[0], requestData[1], t);
      if(t instanceof RpcRemoteException) {
        RpcRemoteException rre = (RpcRemoteException) t;
        RpcUtil.soaprpc_sendError(transport, rre.getCode(), rre.getMessage(), rre.getData());
        return;
      }
      else if(t instanceof XmlRpcException) {
        XmlRpcException xre = (XmlRpcException) t;
        RpcUtil.soaprpc_sendError(transport, xre.code, xre.getMessage(), null);
        return;
      }
      String errorMessage = t.getMessage();
      if(errorMessage == null || errorMessage.length() == 0) {
        errorMessage = t.toString();
      }
      RpcUtil.soaprpc_sendError(transport, 0, errorMessage, null);
      return;
    }
    
    StringBuffer responseData = null;
    try {
      responseData = new StringBuffer(sResult.length() + 325);
      responseData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      responseData.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><s:Body><executeResponse xmlns=\"http://soap.rpc.org\"><executeReturn>");
      responseData.append(sResult);
      responseData.append("</executeReturn></executeResponse></s:Body></s:Envelope>");
      
      if(tracer != null) tracer.trace(requestData[0], requestData[1], responseData.toString(), methodName);
      transport.writeResponse("text/xml", responseData.toString(), true);
    }
    catch(Throwable t) {
      t.printStackTrace();
      if(tracer != null) tracer.trace(requestData[0], requestData[1], responseData.toString(), methodName, t);
    }
  }
}
