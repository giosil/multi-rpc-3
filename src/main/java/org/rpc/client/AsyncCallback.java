package org.rpc.client;

import java.util.Collection;

@SuppressWarnings({"rawtypes"})
public
interface AsyncCallback
{
  public void handleResult(String sMethod, Collection colArgs, Object result);
  
  public void handleError(String sMethod, Collection colArgs, Throwable error);
}
