package org.rpc.server;

public
interface RpcServerTransport
{
  public String[] readRequest(String sContentType) throws Exception;
  
  public boolean checkAuthorization(String methodName);
  
  public void writeResponse(String sContentType, String responseData, boolean boTransEncChunked) throws Exception;
  
  public void setEncoding(String encoding);
  
  public String getEncoding();
}
