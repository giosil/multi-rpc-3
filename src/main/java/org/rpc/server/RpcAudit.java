package org.rpc.server;

public
interface RpcAudit
{
  public Object beforeInvoke(String handlerName, String methodName, Object handler, Object[] parameters) throws Exception;
  
  public Object afterInvoke(String handlerName, String methodName, Object handler, Object[] parameters, long lBefore, Object oResult, Throwable thFault) throws Exception;
}
