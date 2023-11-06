package org.rpc.server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public
interface RpcAuthorizationChecker
{
  public boolean checkAuthorization(String methodName, HttpServletRequest request, HttpServletResponse response);
}
