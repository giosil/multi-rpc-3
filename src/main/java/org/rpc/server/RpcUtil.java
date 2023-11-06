package org.rpc.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.xmlrpc.XmlRpcException;

import org.dew.util.RefUtil;

import org.json.JSONObject;
import org.json.rpc.commons.RpcRemoteException;

import org.xml.rpc.XmlRpcSerializer;

@SuppressWarnings({"rawtypes"})
public
class RpcUtil
{
  public static
  void xmlrpc_sendError(RpcServerTransport transport, int code, String message)
  {
    if(message == null) message = "Service exception";
    message = XmlRpcSerializer.normalizeString(message);
    StringBuilder sb = new StringBuilder(246 + message.length());
    String sEncoding = transport.getEncoding();
    if(sEncoding != null && sEncoding.length() > 0) {
      sb.append("<?xml version=\"1.0\" encoding=\"" + sEncoding + "\"?><methodResponse><fault><value><struct><member><name>faultString</name><value>");
    }
    else {
      sb.append("<?xml version=\"1.0\"?><methodResponse><fault><value><struct><member><name>faultString</name><value>");
    }
    sb.append(message);
    sb.append("</value></member><member><name>faultCode</name><value><int>");
    sb.append(code);
    sb.append("</int></value></member></struct></value></fault></methodResponse>");
    try {
      transport.writeResponse("text/xml", sb.toString(), false);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static
  void soaprpc_sendError(RpcServerTransport transport, int code, String message, String detail)
  {
    if(message == null) message = "Service exception";
    message = XmlRpcSerializer.normalizeString(message);
    if(detail == null) detail = "";
    detail  = XmlRpcSerializer.normalizeString(detail);
    StringBuilder sb = new StringBuilder(320 + message.length() + detail.length());
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><s:Body><s:Fault><faultcode>");
    sb.append(code);
    sb.append("</faultcode><faultstring>");
    sb.append(message);
    sb.append("</faultstring>");
    if(detail != null && detail.length() > 0) {
      sb.append("<detail>");
      sb.append(detail);
      sb.append("</detail>");
    }
    else {
      sb.append("<detail></detail>");
    }
    sb.append("</s:Fault></s:Body></s:Envelope>");
    try {
      transport.writeResponse("text/xml", sb.toString(), true);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static
  void jsonrpc_sendError(RpcServerTransport transport, JSONObject resp, int code, String message, String data)
  {
    if(message == null) message = "Service exception";
    JSONObject error = new JSONObject();
    error.put("code",    new Integer(code));
    error.put("message", message);
    if(data != null) error.put("data", data);
    resp.put("error", error);
    resp.remove("result");
    String responseData = resp.toString();
    try {
      transport.writeResponse("application/json", responseData, false);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static
  String getWSDL(HttpServletRequest request, String sLocation)
  {
    if(sLocation == null || sLocation.length() == 0) {
      sLocation = System.getProperty("org.rpc.server.location");
    }
    if(sLocation == null || sLocation.length() == 0) {
      String sServerName  = request.getServerName();
      int iServerPort     = request.getServerPort();
      String sCtxPath     = request.getContextPath();
      String sServletPath = request.getServletPath();
      String sProtocol    = iServerPort == 443 ? "https" : "http";
      sLocation = sProtocol + "://";
      if(sServerName == null || sServerName.length() == 0) {
        sServerName = "localhost";
      }
      sLocation += sServerName;
      if(iServerPort != 80 && iServerPort != 443) {
        sLocation += ":" + iServerPort;
      }
      if(sCtxPath == null || sCtxPath.length() == 0) sCtxPath = "/RPC";
      if(sCtxPath.startsWith("/")) {
        sLocation += sCtxPath;
      }
      else {
        sLocation += "/" + sCtxPath;
      }
      if(sServletPath == null || sServletPath.length() == 0) sServletPath = "/ws";
      if(sServletPath.startsWith("/")) {
        sLocation += sServletPath;
      }
      else {
        sLocation += "/" + sServletPath;
      }
    }
    else
    if(sLocation.endsWith("*")) {
      sLocation = sLocation.substring(0, sLocation.length()-1);
      if(sLocation.endsWith("/")) sLocation = sLocation.substring(0, sLocation.length()-1);
      String sCtxPath     = request.getContextPath();
      String sServletPath = request.getServletPath();
      if(sCtxPath == null || sCtxPath.length() == 0) sCtxPath = "/RPC";
      if(sCtxPath.startsWith("/")) {
        sLocation += sCtxPath;
      }
      else {
        sLocation += "/" + sCtxPath;
      }
      if(sServletPath == null || sServletPath.length() == 0) sServletPath = "/ws";
      if(sServletPath.startsWith("/")) {
        sLocation += sServletPath;
      }
      else {
        sLocation += "/" + sServletPath;
      }
    }
    return getWSDL(sLocation);
  }
  
  public static
  String getWSDL(String sLocation)
  {
    if(sLocation == null || sLocation.length() == 0) {
      sLocation = "http://localhost:8080/RPC/ws";
    }
    StringBuilder sb = new StringBuilder(2000);
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("<wsdl:definitions targetNamespace=\"http://soap.rpc.org\" xmlns:impl=\"http://soap.rpc.org\" xmlns:intf=\"http://soap.rpc.org\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:wsdlsoap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
    sb.append("<wsdl:types>");
    sb.append("<schema elementFormDefault=\"qualified\" targetNamespace=\"http://soap.rpc.org\" xmlns=\"http://www.w3.org/2001/XMLSchema\">");
    sb.append("<element name=\"execute\">");
    sb.append("<complexType>");
    sb.append("<sequence>");
    sb.append("<element name=\"method\" type=\"xsd:string\"/>");
    sb.append("<element name=\"args\" type=\"xsd:string\"/>");
    sb.append("</sequence>");
    sb.append("</complexType>");
    sb.append("</element>");
    sb.append("<element name=\"executeResponse\">");
    sb.append("<complexType>");
    sb.append("<sequence>");
    sb.append("<element name=\"executeReturn\" type=\"xsd:string\"/>");
    sb.append("</sequence>");
    sb.append("</complexType>");
    sb.append("</element>");
    sb.append("</schema>");
    sb.append("</wsdl:types>");
    sb.append("<wsdl:message name=\"executeResponse\">");
    sb.append("<wsdl:part element=\"impl:executeResponse\" name=\"parameters\">");
    sb.append("</wsdl:part>");
    sb.append("</wsdl:message>");
    sb.append("<wsdl:message name=\"executeRequest\">");
    sb.append("<wsdl:part element=\"impl:execute\" name=\"parameters\">");
    sb.append("</wsdl:part>");
    sb.append("</wsdl:message>");
    sb.append("<wsdl:portType name=\"RPCExecutor\">");
    sb.append("<wsdl:operation name=\"execute\">");
    sb.append("<wsdl:input message=\"impl:executeRequest\" name=\"executeRequest\">");
    sb.append("</wsdl:input>");
    sb.append("<wsdl:output message=\"impl:executeResponse\" name=\"executeResponse\">");
    sb.append("</wsdl:output>");
    sb.append("</wsdl:operation>");
    sb.append("</wsdl:portType>");
    sb.append("<wsdl:binding name=\"RPCExecutorSoapBinding\" type=\"impl:RPCExecutor\">");
    sb.append("<wsdlsoap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>");
    sb.append("<wsdl:operation name=\"execute\">");
    sb.append("<wsdlsoap:operation soapAction=\"\"/>");
    sb.append("<wsdl:input name=\"executeRequest\">");
    sb.append("<wsdlsoap:body use=\"literal\"/>");
    sb.append("</wsdl:input>");
    sb.append("<wsdl:output name=\"executeResponse\">");
    sb.append("<wsdlsoap:body use=\"literal\"/>");
    sb.append("</wsdl:output>");
    sb.append("</wsdl:operation>");
    sb.append("</wsdl:binding>");
    sb.append("<wsdl:service name=\"RPCExecutorService\">");
    sb.append("<wsdl:port binding=\"impl:RPCExecutorSoapBinding\" name=\"RPCExecutor\">");
    sb.append("<wsdlsoap:address location=\"" + sLocation + "\"/>");
    sb.append("</wsdl:port>");
    sb.append("</wsdl:service>");
    sb.append("</wsdl:definitions>");
    return sb.toString();
  }
  
  public static
  String completeMethodName(String methodName, String[] requestData)
  {
    if(requestData.length > 2) {
      String sPathInfo = requestData[2];
      if(sPathInfo != null) {
        if(sPathInfo.startsWith("/")) sPathInfo = sPathInfo.substring(1);
        if(sPathInfo.length() > 0) return sPathInfo + "." + methodName;
      }
    }
    return methodName;
  }
  
  public static
  Object executeMethod(Map handlers, RpcAudit audit, String methodName, List params)
    throws Throwable
  {
    String handlerName  = null;
    Object handler      = null;
    Object[] parameters = null;
    long lBefore        = 0;
    try {
      if(methodName == null || methodName.length() == 0) {
        throw new RpcRemoteException(RpcExecutor.INVALID_REQUEST_ERROR_CODE, "invalid method name", null);
      }
      int iSep = methodName.indexOf('.');
      if(iSep <= 0) {
        throw new RpcRemoteException(RpcExecutor.INVALID_REQUEST_ERROR_CODE, "invalid method name", null);
      }
      handlerName = methodName.substring(0,  iSep);
      methodName  = methodName.substring(iSep + 1);
      
      // Find handler...
      handler = handlers.get(handlerName);
      if(handler == null) {
        // [Security]
        throw new RpcRemoteException(RpcExecutor.METHOD_NOT_FOUND_ERROR_CODE, "handler " + RefUtil.msgText(handlerName) + " not found", null);
      }
      if(handler instanceof String) {
        try {
          Context context = new InitialContext();
          handler = context.lookup((String) handler);
        }
        catch(Throwable th) {
          th.printStackTrace();
          // [Security]
          throw new RpcRemoteException(RpcExecutor.METHOD_NOT_FOUND_ERROR_CODE, "handler " + RefUtil.msgText(handlerName) + " not available", null);
        }
      }
      
      // Find method...
      Method method = null;
      Method lastMethodSameName = null;
      Method[] methods = handler.getClass().getMethods();
      for(int i = 0; i < methods.length; i++) {
        Method m = methods[i];
        if(!m.getName().equals(methodName)) continue;
        
        lastMethodSameName = m;
        
        parameters = RefUtil.getParameters(m, params);
        if(parameters == null) continue;
        
        method = m;
        break;
      }
      if(method == null) {
        if(lastMethodSameName != null) {
          parameters = RefUtil.getParametersExt(lastMethodSameName, params);
        }
        if(parameters == null) {
          // [Security]
          throw new RpcRemoteException(RpcExecutor.METHOD_NOT_FOUND_ERROR_CODE, "method " + RefUtil.msgText(methodName) + "(" + RefUtil.getStringParams(params) + ") not found", null);
        }
        method = lastMethodSameName;
      }
      
      // Invoke method...
      if(audit != null) {
        lBefore = System.currentTimeMillis();
        Object oBefore = audit.beforeInvoke(handlerName, methodName, handler, parameters);
        if(oBefore != null) return oBefore;
      }
      Object oResult = method.invoke(handler, parameters);
      if(audit != null) {
        Object oAfter = audit.afterInvoke(handlerName, methodName, handler, parameters, lBefore, oResult, null);
        if(oAfter != null) return oAfter;
      }
      return oResult;
    }
    catch(Throwable t) {
      if(audit != null) {
        try {
          Object oAfter = audit.afterInvoke(handlerName, methodName, handler, parameters, lBefore, null, t);
          if(oAfter != null) return oAfter;
        }
        catch(Throwable th) {
          if(th instanceof InvocationTargetException) {
            th = ((InvocationTargetException) th).getTargetException();
          }
          throw th;
        }
      }
      if(t instanceof InvocationTargetException) {
        t = ((InvocationTargetException) t).getTargetException();
      }
      if(t instanceof RpcRemoteException) throw t;
      if(t instanceof XmlRpcException) throw t;
      String sMessage = t.getMessage();
      if(sMessage == null || sMessage.length() == 0) sMessage = t.toString();
      throw new RpcRemoteException(0, sMessage, RefUtil.getStackTrace(t));
    }
  }
  
  // Backwards compatibility
  
  public static
  String getBeanGenericType(Method method, int iIndex)
  {
    return RefUtil.getBeanGenericType(method, iIndex);
  }
  
  public static
  Object[] getParameters(Method method, List<?> params)
  {
    return RefUtil.getParameters(method, params);
  }
  
  public static
  Object[] getParametersExt(Method method, List<?> params)
  {
    return RefUtil.getParametersExt(method, params);
  }
  
  public static
  String getStackTrace(Throwable t)
  {
    return RefUtil.getStackTrace(t);
  }
  
  public static
  String getMethodAndRow(byte[] aBytes, int iDepth)
  {
    return RefUtil.getMethodAndRow(aBytes, iDepth);
  }
  
  public static
  String getStringParams(List<?> params)
  {
    return RefUtil.getStringParams(params);
  }
}
