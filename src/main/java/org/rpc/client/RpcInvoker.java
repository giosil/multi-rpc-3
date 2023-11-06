package org.rpc.client;

@SuppressWarnings({"rawtypes"})
public
interface RpcInvoker
{
  public void setTransport(RpcClientTransport transport);
  
  public RpcClientTransport getTransport();
  
  public String getURL();
  
  public boolean isLegacy();
  
  public void setLegacy(boolean boLegacy);
  
  public Object invoke(String sMethod, java.util.Collection colArgs) throws Throwable;
  
  public void invokeAsync(String sMethod, java.util.Collection colArgs, AsyncCallback callback);
}
