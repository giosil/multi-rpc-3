package org.soap.rpc.server;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcException;

import org.json.JSON;
import org.json.rpc.commons.RpcRemoteException;
import org.rpc.server.RpcAudit;
import org.rpc.server.RpcExecutor;
import org.rpc.server.RpcServerTransport;
import org.rpc.server.RpcTracer;
import org.rpc.server.RpcUtil;
import org.soap.rpc.SoapRpcContentHandler;
import org.util.WUtil;
import org.xml.rpc.XmlRpcSerializer;

@SuppressWarnings({"rawtypes","unchecked"})
public
class SoapRpcExecutor implements RpcExecutor
{
  protected Map       handlers;
  protected RpcTracer tracer;
  protected RpcAudit  audit;
  
  public SoapRpcExecutor()
  {
    System.err.println(this.getClass().getName() + " ver. 2.9 instantiated at " + WUtil.formatDateTime(new Date(), "-", true));
    this.handlers = new HashMap();
  }
  
  public SoapRpcExecutor(Map mapHandlers)
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
      requestData = transport.readRequest("application/soap+xml");
    }
    catch(Throwable t) {
      t.printStackTrace();
      return;
    }
    soaprpc_execute(requestData, transport);
  }
  
  public static
  boolean isWSDLRequest(HttpServletRequest request)
  {
    String sWsdl = request.getParameter("wsdl");
    if(sWsdl != null) return true;
    sWsdl = request.getParameter("WSDL");
    if(sWsdl != null) return true;
    sWsdl = request.getParameter("Wsdl");
    if(sWsdl != null) return true;
    return false;
  }
  
  public static
  void sendWSDL(HttpServletRequest request, HttpServletResponse response)
  {
    try {
      String sWsdl = RpcUtil.getWSDL(request, null);
      response.setContentType("text/xml");
      response.setContentLength(sWsdl.length());
      PrintWriter out = response.getWriter();
      out.write(sWsdl);
      out.flush();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  
  public static
  void sendWSDL(HttpServletRequest request, HttpServletResponse response, String sLocation)
  {
    try {
      String sWsdl = RpcUtil.getWSDL(request, sLocation);
      response.setContentType("text/xml");
      response.setContentLength(sWsdl.length());
      PrintWriter out = response.getWriter();
      out.write(sWsdl);
      out.flush();
    }
    catch(Exception ex) {
      ex.printStackTrace();
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
      else
      if(t instanceof XmlRpcException) {
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
