package org.rpc.util;

import java.security.Principal;

import java.util.List;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public
class WebContext
{
  private HttpServletRequest  request;
  private HttpServletResponse response;
  private ServletConfig       config;
  private ServletContext      context;
  private Principal           userPrincipal;
  
  public WebContext(HttpServletRequest request, HttpServletResponse response, ServletConfig config, ServletContext context)
  {
    this.request  = request;
    this.response = response;
    this.config   = config;
    this.context  = context;
  }
  
  public
  HttpServletRequest getRequest()
  {
    return request;
  }
  
  public
  HttpServletResponse getResponse()
  {
    return response;
  }
  
  public
  ServletConfig getServletConfig()
  {
    return config;
  }
  
  public
  ServletContext getServletContext()
  {
    return context;
  }
  
  public
  Principal getUserPrincipal()
  {
    if(userPrincipal == null) {
      if(request != null) {
        return request.getUserPrincipal();
      }
    }
    return userPrincipal;
  }
  
  public
  boolean isUserInRole(String role)
  {
    if(userPrincipal instanceof SimplePrincipal) {
      List<String> roles = ((SimplePrincipal) userPrincipal).getRoles();
      if(roles != null) {
        return roles.contains(role);
      }
    }
    if(request != null) {
      return request.isUserInRole(role);
    }
    return false;
  }
  
  public
  void setUserPrincipal(Principal userPrincipal)
  {
    this.userPrincipal = userPrincipal;
  }
  
  public
  HttpSession getSession()
  {
    if(request == null) return null;
    return request.getSession();
  }
  
  public
  Object getSessionAttribute(String sAttributeName)
  {
    HttpSession httpSession = request.getSession();
    if(httpSession == null) return null;
    return httpSession.getAttribute(sAttributeName);
  }
  
  public
  Object getContextAttribute(String sAttributeName)
  {
    if(context == null) return null;
    return context.getAttribute(sAttributeName);
  }
  
  public
  Object getRequestAttribute(String sAttributeName)
  {
    if(request == null) return null;
    return request.getAttribute(sAttributeName);
  }
  
  public
  String getInitParameter(String sParName)
  {
    if(config == null) return null;
    return config.getInitParameter(sParName);
  }
}
