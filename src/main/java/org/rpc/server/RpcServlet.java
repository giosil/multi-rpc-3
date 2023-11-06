package org.rpc.server;

import java.io.IOException;
import java.io.PrintWriter;

import java.security.Principal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.json.JSON;
import org.json.JavascriptDate;

import org.rpc.util.Base64Coder;
import org.rpc.util.RPCContext;
import org.rpc.util.SimplePrincipal;
import org.rpc.util.WebContext;

import org.soap.rpc.server.SoapRpcExecutor;

import org.util.WUtil;

import org.xml.rpc.XmlRpcSerializer;

/**
 *  Endpoint WebServices RPC e RESTful.
 *  <pre>
 *  public
 *  class WebServices extends org.rpc.server.RpcServlet
 *  {
 *     public
 *     void init()
 *         throws javax.servlet.ServletException
 *     {
 *         rpcExecutor      = new org.rpc.server.MultiRpcExecutor();
 *         restAudit        = null;
 *         restTracer       = null;
 *         
 *         legacy           = false;
 *         createRpcContex  = false;
 *         checkSession     = false;
 *         checkSessionREST = false;
 *         restful          = false;
 *         basicAuth        = true;
 *         encoding         = null; // e.g. "UTF-8", "ISO-8859-1"
 *         
 *         sWSDL_LOCATION   = "http://rpc.service.org*";
 *         
 *         addWebService(new WSTest(), "TEST", "Test service");
 *     }
 *     
 *     protected
 *     Principal authenticate(String username, String password)
 *     {
 *         if(!username.equals(password)) return null;
 *         return new SimplePrincipal(username);
 *     }
 *  }
 *  </pre>
 */
@SuppressWarnings({"rawtypes","unchecked"})
public
class RpcServlet extends HttpServlet implements RpcAuthorizationChecker
{
  private static final long serialVersionUID = 5441759975542129824L;
  
  protected Map handlers = createMapHandlers();
  protected String sHandlersTable = "";
  
  protected String sRES_CONTENT_TYPE     = "application/json";
  protected String sPAR_HTTP_METHOD_1    = "_method";
  protected String sPAR_HTTP_METHOD_2    = "http-method";
  protected String sDEF_HTTP_GET_METHOD  = "read";
  protected String sDEF_HTTP_FIND_METHOD = "find";
  protected String sDEF_HTTP_POST_METHOD = "insert";
  protected String sDEF_HTTP_PUT_METHOD  = "update";
  protected String sDEF_HTTP_DEL_METHOD  = "delete";
  protected String sDEF_HTTP_REM_METHOD  = "remove";
  protected String sPAR_SESSION_MGR      = "cmd";
  protected String sPAR_SESSION_CHECK    = null;
  protected String sVAL_SESSION_OPEN     = "open";
  protected String sVAL_SESSION_CLOSE    = "close";
  
  protected String sWSDL_LOCATION        = null;
  protected String sJSONP_CALLBACK_PARAM = "callback";
  
  protected RpcExecutor rpcExecutor;
  protected RpcAudit    restAudit;
  protected RpcTracer   restTracer;
  
  protected boolean     legacy           = false;
  protected boolean     createRpcContex  = false;
  protected boolean     checkSession     = false;
  protected boolean     checkSessionREST = false;
  protected boolean     restful          = false;
  protected boolean     restParseDatePar = false;
  protected boolean     basicAuth        = false;
  protected boolean     about            = false;
  protected String      encoding         = null;
  protected String      basicRealm       = "RPC";
  protected String      sWhiteList       = null;
  protected String      sNoCacheList     = null;
  protected Map         basicCache       = new LinkedHashMap();
  protected int         basicExpiryIn    = 4*60*60*1000;
  
  protected
  void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    String sPathInfo = request.getPathInfo();
    if(sPathInfo == null || sPathInfo.length() == 0 || sPathInfo.equals("/")) {
      String sQueryString = request.getQueryString();
      if(sQueryString == null || sQueryString.length() == 0) {
        if(basicAuth && !checkBasicAuth(request, response, true)) return;
        showHandlers(request, response, false); // [Security]
        return;
      }
    }
    if(about && sPathInfo != null && sPathInfo.equalsIgnoreCase("/about")) {
      String sQueryString = request.getQueryString();
      if(sQueryString == null || sQueryString.length() == 0) {
        if(basicAuth && !checkBasicAuth(request, response, true)) return;
        showHandlers(request, response);
        return;
      }
    }
    if(rpcExecutor != null) {
      boolean boSOAP   = rpcExecutor instanceof org.soap.rpc.server.SoapRpcExecutor;
      boSOAP = boSOAP || rpcExecutor instanceof org.rpc.server.MultiRpcExecutor;
      if(boSOAP) {
        if(SoapRpcExecutor.isWSDLRequest(request)) {
          SoapRpcExecutor.sendWSDL(request, response, sWSDL_LOCATION);
          return;
        }
      }
    }
    if(manageSession(request, response)) return;
    try {
      if(createRpcContex) RPCContext.setContext(request, response, getServletConfig(), getServletContext());
      if(restful) restExecute(request, response);
    }
    finally {
      if(createRpcContex) RPCContext.removeContext();
    }
  }
  
  protected
  void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    try {
      if(createRpcContex) RPCContext.setContext(request, response, getServletConfig(), getServletContext());
      if(rpcExecutor != null) {
        String sQueryString = request.getQueryString();
        boolean restCall    = false;
        if(sQueryString != null && sQueryString.length() > 0) {
          restCall = true;
        }
        else {
          String sPathInfo = request.getPathInfo();
          if(sPathInfo != null && sPathInfo.indexOf('/') >= 0) {
            if(sPathInfo.indexOf('.') < 0 || restful) {
              restCall = true;
            }
          }
        }
        if(!restCall) {
          if(checkSession || basicAuth) {
            rpcExecutor.execute(new RpcServletTransport(request, response, encoding, this));
          }
          else {
            rpcExecutor.execute(new RpcServletTransport(request, response, encoding));
          }
          return;
        }
      }
      if(restful) restExecute(request, response);
    }
    finally {
      if(createRpcContex) RPCContext.removeContext();
    }
  }
  
  protected
  void doPut(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    try {
      if(createRpcContex) RPCContext.setContext(request, response, getServletConfig(), getServletContext());
      if(restful) restExecute(request, response);
    }
    finally {
      if(createRpcContex) RPCContext.removeContext();
    }
  }
  
  protected
  void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    try {
      if(createRpcContex) RPCContext.setContext(request, response, getServletConfig(), getServletContext());
      if(restful) restExecute(request, response);
    }
    finally {
      if(createRpcContex) RPCContext.removeContext();
    }
  }
  
  // Session ---------------------------------------------------------------------------
  
  protected
  boolean manageSession(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    // Nei metodi diversi dal GET la getParameter potrebbe consumare l'input stream utilizzato dai parser RPC.
    String sSessionMgr = request.getParameter(sPAR_SESSION_MGR);
    if(sSessionMgr != null && sSessionMgr.equalsIgnoreCase(sVAL_SESSION_OPEN)) {
      boolean boOpenSession = openSession(request, response);
      if(!boOpenSession) {
        response.sendError(401); // Unauthorized
        return true;
      }
      HttpSession httpSession = request.getSession(true);
      restResponse(request, response, "openSession", httpSession.getId());
      return true;
    }
    if(sSessionMgr != null && sSessionMgr.equalsIgnoreCase(sVAL_SESSION_CLOSE)) {
      closeSession(request, response);
      HttpSession httpSession = request.getSession(false);
      if(httpSession != null) httpSession.invalidate();
      restResponse(request, response, "closeSession", Boolean.TRUE);
      return true;
    }
    return false;
  }
  
  public
  boolean checkAuthorization(String methodName, HttpServletRequest request, HttpServletResponse response)
  {
    if(sWhiteList != null) {
      if(sWhiteList.indexOf("," + methodName + ",") >= 0) return true;
      int iSep = methodName.indexOf('.');
      if(iSep > 0) {
        if(sWhiteList.indexOf("," + methodName.substring(0,iSep+1) + "*,") >= 0) return true;
      }
    }
    boolean boNoCache = false;
    if(sNoCacheList != null) {
      if(sNoCacheList.indexOf("," + methodName + ",") >= 0) boNoCache = true;
      int iSep = methodName.indexOf('.');
      if(iSep > 0) {
        if(sNoCacheList.indexOf("," + methodName.substring(0,iSep+1) + "*,") >= 0) boNoCache = true;
      }
    }
    if(basicAuth) {
      boolean check = checkBasicAuth(request, response, boNoCache);
      if(check) return true;
      if(!checkSession && !checkSessionREST) return false;
    }
    if(!checkSession && !checkSessionREST) return true;
    HttpSession httpSession = request.getSession(false);
    if(httpSession == null) {
      if(basicAuth) {
        // Is it a lost session?
        String jsessionId = getCookieValue(request, "JSESSIONID");
        if(jsessionId != null && jsessionId.length() > 0) {
          try{ response.sendError(403); } catch(Throwable ignore) {} // Forbidden
          return false;
        }
        else {
          response.addHeader("WWW-Authenticate", "Basic realm=\"" + basicRealm + "\"");
          try{ response.sendError(401); } catch(Throwable ignore) {} // Unauthorized
          return false;
        }
      }
      try{ response.sendError(403); } catch(Throwable ignore) {} // Forbidden
      return false;
    }
    if(sPAR_SESSION_CHECK != null && sPAR_SESSION_CHECK.length() > 0) {
      Object oCheck = httpSession.getAttribute(sPAR_SESSION_CHECK);
      if(oCheck == null) {
        if(basicAuth) {
          response.addHeader("WWW-Authenticate", "Basic realm=\"" + basicRealm + "\"");
          try{ response.sendError(401); } catch(Throwable ignore) {} // Unauthorized
          return false;
        }
        try{ response.sendError(403); } catch(Throwable ignore) {} // Forbidden
        return false;
      }
    }
    return true;
  }
  
  protected
  boolean checkBasicAuth(HttpServletRequest request, HttpServletResponse response)
  {
    return checkBasicAuth(request, response, false);
  }
  
  protected
  boolean checkBasicAuth(HttpServletRequest request, HttpServletResponse response, boolean boNoCache)
  {
    final String sAuthorization = request.getHeader("Authorization");
    // Basic Base64_User:Password (Length of Base64 encoded string is a multiple of 4)
    if(sAuthorization == null || sAuthorization.length() < 10) {
      if(checkSession || checkSessionREST) return false;
      response.addHeader("WWW-Authenticate", "Basic realm=\"" + basicRealm + "\"");
      try{ response.sendError(401); } catch(Throwable ignore) {} // Unauthorized
      return false;
    }
    else {
      String sCredentials = null;
      int iSep = -1;
      if(sAuthorization.startsWith("Bearer")) {
        sCredentials = sAuthorization.substring(7);
      }
      else {
        try{
          sCredentials = Base64Coder.decodeString(sAuthorization.substring(6));
        }
        catch(Throwable th) {
          try{ response.sendError(403); } catch(Throwable ignore) {} // Forbidden
          return false;
        }
        iSep = sCredentials.indexOf(':');
        if(iSep <= 0) {
          try{ response.sendError(403); } catch(Throwable ignore) {} // Forbidden
          return false;
        }
      }
      // Check cache
      final long currentTimeMillis = System.currentTimeMillis();
      boolean alreadyCached = false;
      final PrincipalExpiryIn principalExpiryIn = boNoCache ? null : (PrincipalExpiryIn) basicCache.get(sCredentials);
      if(principalExpiryIn != null) {
        alreadyCached = true;
        if(principalExpiryIn.expiryIn > currentTimeMillis) {
          WebContext webContext = RPCContext.getContext();
          if(webContext != null) webContext.setUserPrincipal(principalExpiryIn.principal);
          return true;
        }
      }
      Principal principal = null;
      if(iSep < 0) {
        principal = checkToken(sCredentials);
        if(principal == null) {
          try{ response.sendError(403); } catch(Throwable ignore) {} // Forbidden
          return false;
        }
      }
      else {
        principal = authenticate(sCredentials.substring(0,iSep), sCredentials.substring(iSep+1));
      }
      if(principal == null) {
        response.addHeader("WWW-Authenticate", "Basic realm=\"" + basicRealm.replace('"', '\'') + "\"");
        try{ response.sendError(401); } catch(Throwable ignore) {} // Unauthorized
        return false;
      }
      basicCache.put(sCredentials, new PrincipalExpiryIn(principal, currentTimeMillis + basicExpiryIn));
      if(!alreadyCached) {
        final Iterator iterator = basicCache.entrySet().iterator();
        final Map.Entry entry = (Map.Entry) iterator.next();
        final PrincipalExpiryIn firstPrincipalExpiryIn = (PrincipalExpiryIn) entry.getValue();
        if(firstPrincipalExpiryIn.expiryIn < currentTimeMillis) {
          try{ iterator.remove(); } catch(Throwable ignore) {}
        }
      }
      WebContext webContext = RPCContext.getContext();
      if(webContext != null) webContext.setUserPrincipal(principal);
    }
    return true;
  }
  
  protected
  Principal checkToken(String token)
  {
    return null;
  }
  
  protected
  Principal authenticate(String username, String password)
  {
    return new SimplePrincipal(username);
  }
  
  protected
  boolean openSession(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    return true;
  }
  
  protected
  void closeSession(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    return;
  }
  
  // Handler ---------------------------------------------------------------------------
  
  protected
  void addHandler(String sId, Object oHandler, String sDescription)
    throws ServletException
  {
    addWebService(oHandler, sId, sDescription);
  }
  
  protected
  void addWebServiceREST(String sId, Object oHandler, String sDescription)
    throws ServletException
  {
    synchronized(this.handlers) {
      if(this.handlers.containsKey(sId)) {
        throw new IllegalArgumentException("handler " + sId + " already exists");
      }
      this.handlers.put(sId, oHandler);
    }
    if(sDescription == null || sDescription.length() == 0) sDescription = "&nbsp;";
    sHandlersTable += "<tr><td>" + sId + "</td>";
    sHandlersTable += "<td>(REST) " + sDescription + "</td>";
    sHandlersTable += "<td>" + oHandler.getClass().getName() + "</td></tr>";
  }
  
  protected
  void addWebServiceRPC(String sId, Object oHandler, String sDescription)
    throws ServletException
  {
    if(rpcExecutor == null) throw new IllegalArgumentException("rpcExecutor is null");
    rpcExecutor.addHandler(sId, oHandler);
    if(sDescription == null || sDescription.length() == 0) sDescription = "&nbsp;";
    sHandlersTable += "<tr><td>" + sId + "</td>";
    sHandlersTable += "<td>(RPC) " + sDescription + "</td>";
    sHandlersTable += "<td>" + oHandler.getClass().getName() + "</td></tr>";
  }
  
  protected
  void addWebService(Object oHandler, String sId, String sDescription)
    throws ServletException
  {
    synchronized(this.handlers) {
      if(this.handlers.containsKey(sId)) {
        throw new IllegalArgumentException("handler " + sId + " already exists");
      }
      this.handlers.put(sId, oHandler);
    }
    if(rpcExecutor != null) {
      rpcExecutor.addHandler(sId, oHandler);
    }
    if(sDescription == null || sDescription.length() == 0) sDescription = "&nbsp;";
    sHandlersTable += "<tr><td>" + sId + "</td>";
    sHandlersTable += "<td>" + sDescription + "</td>";
    sHandlersTable += "<td>" + oHandler.getClass().getName() + "</td></tr>";
  }
  
  protected
  void addToWhiteList(String sItem)
  {
    if(sItem == null || sItem.length() == 0) return;
    if(sWhiteList == null) sWhiteList = ",";
    int iSep = sItem.indexOf('.');
    if(iSep < 0) {
      sWhiteList += sItem + ".*,";
    }
    else {
      sWhiteList += sItem + ",";
    }
  }
  
  protected
  void addToNoCacheList(String sItem)
  {
    if(sItem == null || sItem.length() == 0) return;
    if(sNoCacheList == null) sNoCacheList = ",";
    int iSep = sItem.indexOf('.');
    if(iSep < 0) {
      sNoCacheList += sItem + ".*,";
    }
    else {
      sNoCacheList += sItem + ",";
    }
  }
  
  protected
  void showHandlers(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    showHandlers(request, response, true);
  }
  
  protected
  void showHandlers(HttpServletRequest request, HttpServletResponse response, boolean detail)
    throws IOException
  {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<head>");
    out.println("<title>RPCServlet 2.9</title>");
    out.println("</head>");
    out.println("<body>");
    if(detail) {
      out.println("<b>" + getClass().getName() + " is running.</b><br><br>");
      out.println("<table align=\"center\" border=\"1\" cellspacing=\"0\">");
      out.println("<tr bgcolor=\"#eeeeee\"><th>Object</th><th>Description</th><th>Class</th></tr>");
      out.println(sHandlersTable);
      out.println("</table>");
      if(basicAuth) {
        out.println("<hr>");
        out.println("<b>basicAuth:</b> "  + basicAuth  + "<br>");
        out.println("<b>basicRealm:</b> " + basicRealm + "<br>");
        if(basicCache != null) {
          out.println("<b>basicCache.size:</b> " + basicCache.size() + "<br>");
        }
        out.println("<hr>");
      }
      out.println("<hr>");
      Enumeration enumHeaderNames = request.getHeaderNames();
      while(enumHeaderNames.hasMoreElements()) {
        String sName = (String) enumHeaderNames.nextElement();
        String sValue = request.getHeader(sName);
        out.println("<b>" + sName + "</b>: " + sValue + "<br>");
      }
      out.println("<hr>");
      Iterator iterator = System.getProperties().entrySet().iterator();
      while(iterator.hasNext()) {
        Map.Entry entry = (Map.Entry) iterator.next();
        out.println("<b>" + entry.getKey() + "</b>: " + entry.getValue() + "<br>");
      }
    }
    else {
      out.println("<b>RPCServlet is running.</b><br><br>");
    }
    out.println("</body>");
    out.println("</html>");
  }
  
  protected
  void restExecute(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    // PathInfo -> listPathParams
    List listPathParams = null;
    if(legacy) listPathParams = new Vector(); else listPathParams = new ArrayList();
    String sPathInfo = request.getPathInfo();
    if(sPathInfo != null && sPathInfo.length() > 0) {
      if(sPathInfo.charAt(0) == '/') sPathInfo = sPathInfo.substring(1);
      int iIndexOf = 0;
      int iBegin   = 0;
      iIndexOf     = sPathInfo.indexOf('/');
      while(iIndexOf >= 0) {
        if(listPathParams.size() == 0) {
          // 0 = handler.method
          listPathParams.add(sPathInfo.substring(iBegin, iIndexOf));
        }
        else {
          listPathParams.add(restParamToObject(sPathInfo.substring(iBegin, iIndexOf)));
        }
        iBegin   = iIndexOf + 1;
        iIndexOf = sPathInfo.indexOf('/', iBegin);
      }
      if(listPathParams.size() == 0) {
        // 0 = handler.method
        listPathParams.add(sPathInfo.substring(iBegin));
      }
      else {
        listPathParams.add(restParamToObject(sPathInfo.substring(iBegin)));
      }
    }
    // Parameters -> mapParameters
    boolean boJSONP = false;
    Map mapParameters = null;
    if(legacy) mapParameters = new Hashtable(); else mapParameters = new HashMap();
    Enumeration enumeration = request.getParameterNames();
    while(enumeration.hasMoreElements()) {
      String sParameterName = (String) enumeration.nextElement();
      if(sParameterName.equals(sJSONP_CALLBACK_PARAM)) {
        boJSONP = true;
        continue;
      }
      if(sParameterName.equals("_") && boJSONP) {
        continue;
      }
      String sParameterVal  = request.getParameter(sParameterName);
      Object oValue = restParamToObject(sParameterVal);
      if(legacy && oValue == null) continue;
      mapParameters.put(sParameterName, oValue);
    }
    
    if(sPAR_HTTP_METHOD_1 != null && sPAR_HTTP_METHOD_1.length() > 0) mapParameters.remove(sPAR_HTTP_METHOD_1);
    if(sPAR_HTTP_METHOD_2 != null && sPAR_HTTP_METHOD_2.length() > 0) mapParameters.remove(sPAR_HTTP_METHOD_2);
    if(listPathParams.size() == 0 && mapParameters.size() == 0) {
      showHandlers(request, response, false);
      return;
    }
    
    String methodName = null;
    if(listPathParams.size() > 0) {
      Object oMethodName = listPathParams.remove(0);
      methodName = oMethodName != null ? oMethodName.toString() : null;
    }
    if(methodName == null || methodName.length() == 0) {
      restError(request, response, null, 400, "Invalid method"); // Bad Request
      return;
    }
    
    if(listPathParams.size() == 0 && mapParameters.size() == 0) {
      String httpMethod = request.getMethod();
      if(httpMethod != null && !httpMethod.equalsIgnoreCase("get")) {
        try {
          RpcServletTransport rpcServletTransport = new RpcServletTransport(request, response, encoding);
          String[] asRequest = rpcServletTransport.readRequest(null);
          if(asRequest != null && asRequest.length > 1) {
            String sContentType = asRequest[0];
            String sContentBody = asRequest[1];
            if(sContentType != null && sContentType.length() > 0 && sContentBody != null && sContentBody.length() > 0) {
              if(sContentType.endsWith("json")) {
                Object oContent = JSON.parse(sContentBody);
                if(oContent instanceof Map) {
                  mapParameters.putAll((Map) oContent);
                }
                else {
                  listPathParams.add(oContent);
                }
              }
              else {
                listPathParams.add(sContentBody);
              }
            }
          }
        }
        catch(Exception ex) {
          System.err.println("[RpcSerlvet] Exception in readRequest: " + ex);
        }
      }
    }
    
    String[] asDefMethods = restDefMethods(request);
    boolean boDefMethodPath   = false;
    boolean boDefMethodParams = false;
    int iSep = methodName.indexOf('.');
    if(iSep < 0) {
      methodName += ".";
      if(listPathParams.size() >= 0 && mapParameters.size() == 0) {
        boDefMethodPath = true;
      }
      else if(listPathParams.size() == 0 && mapParameters.size() == 1 && !asDefMethods[0].equals(asDefMethods[1])) {
        boDefMethodPath = true;
      }
      else {
        boDefMethodParams = true;
      }
      methodName += boDefMethodPath ? asDefMethods[0] : asDefMethods[1];
    }
    
    methodName = restMethod(methodName, listPathParams, mapParameters, boDefMethodPath, boDefMethodParams);
    if(methodName == null || methodName.length() == 0) {
      restError(request, response, null, 400, "Invalid method"); // Bad Request
      return;
    }
    
    if(checkSessionREST || basicAuth) {
      if(!checkAuthorization(methodName, request, response)) return;
    }
    
    Object oResult = null;
    try {
      List params = restParams(methodName, listPathParams, mapParameters, boDefMethodPath, boDefMethodParams);
      if(params == null) {
        restError(request, response, null, 204, "No Response"); // No Response
        return;
      }
      oResult = RpcUtil.executeMethod(handlers, restAudit, methodName, params);
    }
    catch(Throwable th) {
      restError(request, response, methodName, 500, th.toString());
      return;
    }
    restResponse(request, response, methodName, oResult);
  }
  
  /**
   * Determinazione del metodo (dell'handler REST) di default.
   * 
   * @param request HttpServletRequest instance
   * @return Array of String: <br>
   *          [0]=Metodo di default che accetta uno o piu' parametri scalari,<br>
   *          [1]=Metodo di default che accetta una struttura dati (Map).<br>
   */
  protected
  String[] restDefMethods(HttpServletRequest request)
  {
    String[] asResult = new String[2];
    String sParHttpMethod = null;
    if(sPAR_HTTP_METHOD_1 != null && sPAR_HTTP_METHOD_1.length() > 0) {
      sParHttpMethod = request.getParameter(sPAR_HTTP_METHOD_1);
    }
    if(sParHttpMethod == null || sParHttpMethod.length() == 0) {
      if(sPAR_HTTP_METHOD_2 != null && sPAR_HTTP_METHOD_2.length() > 0) {
        sParHttpMethod = request.getParameter(sPAR_HTTP_METHOD_2);
      }
    }
    if(sParHttpMethod == null) sParHttpMethod = request.getMethod();
    if(sParHttpMethod == null || sParHttpMethod.length() < 3) {
      sParHttpMethod = "get";
    }
    if(sParHttpMethod.equalsIgnoreCase("get")) {
      asResult[0] = sDEF_HTTP_GET_METHOD;
      asResult[1] = sDEF_HTTP_FIND_METHOD;
    }
    else if(sParHttpMethod.equalsIgnoreCase("post")) {
      asResult[0] = sDEF_HTTP_POST_METHOD;
      asResult[1] = sDEF_HTTP_POST_METHOD;
    }
    else if(sParHttpMethod.equalsIgnoreCase("put")) {
      asResult[0] = sDEF_HTTP_PUT_METHOD;
      asResult[1] = sDEF_HTTP_PUT_METHOD;
    }
    else if(sParHttpMethod.equalsIgnoreCase("delete")) {
      asResult[0] = sDEF_HTTP_DEL_METHOD;
      asResult[1] = sDEF_HTTP_REM_METHOD;
    }
    return asResult;
  }
  
  protected
  String restMethod(String sMethodName, List listPathParams, Map mapParameters, boolean boDefMethodPath, boolean boDefMethodParams)
  {
    return sMethodName;
  }
  
  protected
  List restParams(String sMethodName, List listPathParams, Map mapParameters, boolean boDefMethodPath, boolean boDefMethodParams)
  {
    List params = new ArrayList();
    params.addAll(listPathParams);
    if(boDefMethodPath && mapParameters.size() <= 1) {
      if(mapParameters.size() == 1) {
        Object oValue = null;
        Iterator iterator = mapParameters.values().iterator();
        if(iterator.hasNext()) oValue = iterator.next();
        params.add(oValue);
      }
    }
    else if(mapParameters.size() > 0) {
      params.add(mapParameters);
    }
    return params;
  }
  
  protected
  void restError(HttpServletRequest request, HttpServletResponse response, String sMethodName, int iErrCode, String sErrMessage)
    throws IOException
  {
    if(restTracer != null) {
      restTracer.trace(sRES_CONTENT_TYPE, restRequest(request), iErrCode + ": " + sErrMessage, sMethodName);
    }
    response.sendError(iErrCode, sErrMessage);
  }
  
  protected
  void restResponse(HttpServletRequest request, HttpServletResponse response, String sMethodName, Object oResult)
    throws ServletException, IOException
  {
    String contentType  = null;
    String responseData = null;
    try {
      if(sRES_CONTENT_TYPE == null || sRES_CONTENT_TYPE.length() == 0) {
        if(oResult instanceof String) {
          contentType  = "text/plain";
          responseData = (String) oResult;
        }
        else {
          contentType  = "application/json";
          responseData = JSON.stringify(oResult);
        }
      }
      else if(sRES_CONTENT_TYPE.indexOf("js") >= 0) {
        contentType  = sRES_CONTENT_TYPE;
        responseData = JSON.stringify(oResult);
      }
      else {
        contentType  = "text/xml";
        responseData = XmlRpcSerializer.serialize(oResult, legacy);
      }
      
      String sJSONPCallback = request.getParameter(sJSONP_CALLBACK_PARAM);
      if(sJSONPCallback != null && sJSONPCallback.length() > 0 && sJSONPCallback.indexOf(';') < 0) {
        contentType  = "application/javascript";
        responseData = sJSONPCallback + "(" + JavascriptDate.replaceDateTime(responseData) + ");";
      }
      
      if(encoding != null && encoding.length() > 0) {
        response.setCharacterEncoding(encoding);
      }
      
      byte[] data = responseData.getBytes(response.getCharacterEncoding());
      if(encoding != null && encoding.length() > 0) {
        response.addHeader("Content-Type", contentType + "; charset=" + encoding);
      }
      else {
        response.addHeader("Content-Type", contentType);
      }
      response.setHeader("Content-Length", String.valueOf(data.length));
      
      ServletOutputStream os = response.getOutputStream();
      os.write(data, 0, data.length);
      os.flush();
      
      if(restTracer != null) {
        restTracer.trace(sRES_CONTENT_TYPE, restRequest(request), responseData, sMethodName);
      }
    }
    catch(IOException ex) {
      if(restTracer != null) {
        restTracer.trace(sRES_CONTENT_TYPE, restRequest(request), responseData, sMethodName, ex);
      }
    }
  }
  
  protected
  String restRequest(HttpServletRequest request)
  {
    String sServletPath = request.getServletPath();
    String sPathInfo    = request.getPathInfo();
    String sQueryString = request.getQueryString();
    if(sServletPath == null) sServletPath = "";
    if(sPathInfo    == null) sPathInfo    = "";
    if(sQueryString == null) sQueryString = "";
    if(sPathInfo.startsWith("/") && sServletPath.endsWith("/")) {
      sPathInfo = sPathInfo.substring(1);
    }
    if(!sQueryString.startsWith("?")) sQueryString = "?" + sQueryString;
    return sServletPath + sPathInfo + sQueryString;
  }
  
  protected
  Object restParamToObject(String sValue)
  {
    if(sValue == null || sValue.length() == 0) return sValue;
    char c0 = sValue.charAt(0);
    if((c0 >= '0' && c0 <= '9') || c0 == '-') {
      try {
        if(sValue.indexOf('.') > -1 || sValue.indexOf('e') > -1 || sValue.indexOf('E') > -1) {
          return Double.valueOf(sValue);
        }
        else {
          Long longValue = new Long(sValue);
          if(sValue.equals(longValue.toString())) {
            if(longValue.longValue() == longValue.intValue()) {
              return new Integer(longValue.intValue());
            }
            else {
              return longValue;
            }
          }
        }
      }
      catch(Exception ignore) {
      }
    }
    if(sValue.equalsIgnoreCase("true"))  return Boolean.TRUE;
    if(sValue.equalsIgnoreCase("false")) return Boolean.FALSE;
    if(sValue.equalsIgnoreCase("null"))  return null;
    if(sValue.startsWith("[") && sValue.endsWith("]")) {
      String sListValues = sValue.substring(1, sValue.length()-1);
      List listValues = null;
      if(legacy) listValues = new Vector(); else listValues = new ArrayList();
      int iIndexOf = 0;
      int iBegin   = 0;
      iIndexOf     = sListValues.indexOf(',');
      while(iIndexOf >= 0) {
        listValues.add(restParamToObject(sListValues.substring(iBegin, iIndexOf)));
        iBegin   = iIndexOf + 1;
        iIndexOf = sListValues.indexOf(',', iBegin);
      }
      listValues.add(restParamToObject(sListValues.substring(iBegin)));
      return listValues;
    }
    if(sValue.startsWith("{") && sValue.endsWith("}")) {
      if(sValue.length() > 2 && sValue.indexOf('=') < 2) return sValue;
      if(legacy) return JSON.parseLegacy(sValue); else return JSON.parse(sValue);
    }
    if(restParseDatePar) {
      // check date (no time)
      String sCheck = WUtil.normalizeStringDate(sValue);
      if(sCheck != null && sCheck.length() > 0) {
        Calendar cal = WUtil.stringToCalendar(sValue);
        if(cal != null) return new java.util.Date(cal.getTimeInMillis());
      }
    }
    return sValue;
  }
  
  protected
  String getCookieValue(HttpServletRequest request, String coockieName)
  {
    Cookie[] cookies = request.getCookies();
    if(cookies == null || cookies.length == 0) {
      return null;
    }
    for(int i = 0; i < cookies.length; i++) {
      String cookieName = cookies[i].getName();
      if(cookieName != null && cookieName.equalsIgnoreCase(coockieName)) {
        return cookies[i].getValue();
      }
    }
    return null;
  }
  
  protected
  Map createMapHandlers()
  {
    return new HashMap();
  }
  
  static class PrincipalExpiryIn
  {
    private Principal principal;
    private long      expiryIn;
    
    public PrincipalExpiryIn(Principal principal, long expiryIn) {
      this.principal = principal;
      this.expiryIn  = expiryIn;
    }
    
    public Principal getPrincipal() {
      return principal;
    }
    
    public long getExpiryIn() {
      return expiryIn;
    }
  }
}
