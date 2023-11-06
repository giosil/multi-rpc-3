package org.rpc.server;

public
interface RpcTracer
{
  public void trace(String sContentType, String sRequest, Throwable throwable);
  
  public void trace(String sContentType, String sRequest, String sResponse, String sMethodName);
  
  public void trace(String sContentType, String sRequest, String sResponse, String sMethodName, Throwable throwable);
}
