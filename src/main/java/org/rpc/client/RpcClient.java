package org.rpc.client;

@SuppressWarnings({"rawtypes"})
public
interface RpcClient
{
  public void setURL(String sURL) throws java.net.MalformedURLException;
  
  public String getURL();
  
  public boolean isLegacy();
  
  public void setLegacy(boolean boLegacy);
  
  public void setTimeOut(int iTimeOut);
  
  public int getTimeOut();
  
  public void setDefaultTimeOut();
  
  public void setHeaders(java.util.Map headers);
  
  public Object execute(String sMethod, java.util.Collection colParameters) throws Exception;
  
  public void execute(String sMethod, java.util.Collection colParameters, AsyncCallback callBack) throws Exception;
}
