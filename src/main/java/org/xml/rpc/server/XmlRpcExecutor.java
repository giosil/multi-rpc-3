package org.xml.rpc.server;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;

import org.json.rpc.commons.RpcRemoteException;
import org.rpc.server.RpcAudit;
import org.rpc.server.RpcExecutor;
import org.rpc.server.RpcServerTransport;
import org.rpc.server.RpcTracer;
import org.rpc.server.RpcUtil;
import org.util.WUtil;
import org.xml.rpc.XmlRpcContentHandler;
import org.xml.rpc.XmlRpcSerializer;

@SuppressWarnings({"rawtypes","unchecked"})
public
class XmlRpcExecutor implements RpcExecutor
{
  protected Map handlers;
  protected RpcTracer tracer;
  protected RpcAudit audit;
  
  public XmlRpcExecutor()
  {
    System.err.println(this.getClass().getName() + " ver. 3.0.0 instantiated at " + WUtil.formatDateTime(new Date(), "-", true));
    this.handlers = new HashMap();
  }
  
  public XmlRpcExecutor(Map mapHandlers)
  {
    System.err.println(this.getClass().getName() + " ver. 3.0.0 instantiated at " + WUtil.formatDateTime(new Date(), "-", true));
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
      requestData = transport.readRequest("text/xml");
    }
    catch(Throwable t) {
      t.printStackTrace();
      return;
    }
    xmlrpc_execute(requestData, transport);
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
      else
      if(t instanceof XmlRpcException) {
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
}

