package org.rpc.util;

import java.security.Principal;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@SuppressWarnings({"rawtypes","unchecked"})
public
class RPCContext
{
  private static ThreadLocal threadLocal = new ThreadLocal();
  
  public static
  void setContext(HttpServletRequest request, HttpServletResponse response, ServletConfig config, ServletContext context)
  {
    WebContext webContext = new WebContext(request, response, config, context);
    threadLocal.set(webContext);
  }
  
  public static
  void removeContext()
  {
    threadLocal.set(null);
  }
  
  public static
  WebContext getContext()
  {
    return (WebContext) threadLocal.get();
  }
  
  public static
  HttpServletRequest getRequest()
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getRequest();
  }
  
  public static
  HttpServletResponse getResponse()
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getResponse();
  }
  
  public static
  ServletConfig getServletConfig()
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getServletConfig();
  }
  
  public static
  ServletContext getServletContext()
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getServletContext();
  }
  
  public static
  Principal getUserPrincipal()
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getUserPrincipal();
  }
  
  public static
  String getUserPrincipalName()
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    Principal principal = webContext.getUserPrincipal();
    if(principal == null) return null;
    return principal.getName();
  }
  
  public static
  boolean isUserInRole(String role)
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return false;
    return webContext.isUserInRole(role);
  }
  
  public static
  Map getUserPrincipalAttributes()
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return new HashMap(0);
    Principal principal = webContext.getUserPrincipal();
    if(principal == null) return new HashMap(0);
    if(principal instanceof SimplePrincipal) {
      return ((SimplePrincipal) principal).getAttributes();
    }
    return new HashMap(0);
  }
  
  public static
  HttpSession getSession()
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getSession();
  }
  
  public static
  Object getSessionAttribute(String sAttributeName)
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getSessionAttribute(sAttributeName);
  }
  
  public static
  Object getRequestAttribute(String sAttributeName)
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getRequestAttribute(sAttributeName);
  }
  
  public static
  Object getContextAttribute(String sAttributeName)
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getContextAttribute(sAttributeName);
  }
  
  public static
  String getInitParameter(String sParName)
  {
    WebContext webContext = (WebContext) threadLocal.get();
    if(webContext == null) return null;
    return webContext.getInitParameter(sParName);
  }
}
