package org.rpc.server;

public
interface RpcExecutor
{
  public static final int PARSE_ERROR_CODE            = -32700;
  public static final int INVALID_REQUEST_ERROR_CODE  = -32600;
  public static final int METHOD_NOT_FOUND_ERROR_CODE = -32601;
  public static final int INVALID_PARAMS_ERROR_CODE   = -32602;
  public static final int INTERNAL_ERROR_CODE         = -32603;
  public static final int SERVER_ERROR_START          = -32000;
  
  public void addHandler(String name, Object handler);
  
  public void removeHandler(String name);
  
  public void setTracer(RpcTracer tracer);
  
  public void setAudit(RpcAudit audit);
  
  public void execute(RpcServerTransport transport);
}
