package org.rpc.client;

import java.util.Map;

@SuppressWarnings({"rawtypes"})
public
interface RpcClientTransport
{
  public void setHeaders(Map headers);
  
  public void setTimeOut(int iTimeOut);
  
  public String call(String requestData) throws Exception;
  
  public String call(String requestData, Map headers) throws Exception;
}
