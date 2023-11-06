package org.dew.rpc;

import org.dew.demo.Demo;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

@WebServlet(name = "MicroMultiRPC", loadOnStartup = 0, urlPatterns = { "/rpc/*" })
public 
class MicroMultiRPC extends org.rpc.server.RpcServlet 
{
  private static final long serialVersionUID = 1052971765978188070L;

  public 
  void init() 
    throws ServletException 
  {
    rpcExecutor = new org.rpc.server.MultiRpcExecutor();
    
    restAudit  = null;
    restTracer = null;
    
    createRpcContex  = true;
    checkSession     = false;
    checkSessionREST = false;
    restful          = true;
    about            = true;
    
    basicAuth        = false;
    
    addWebService(new Demo(), "DEMO", "Demo services");
  }
}
