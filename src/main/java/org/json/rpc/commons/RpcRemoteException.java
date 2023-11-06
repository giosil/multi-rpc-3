/*
 * Copyright (C) 2011 ritwik.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json.rpc.commons;

import org.rpc.server.RpcExecutor;

public
class RpcRemoteException extends RuntimeException
{
  private static final long serialVersionUID = 4027118690858678028L;
  
  private final int     code;
  private final String  msg;
  private final String  data;
  
  public RpcRemoteException(String msg)
  {
    super(msg);
    this.code = RpcExecutor.SERVER_ERROR_START;
    this.msg  = msg;
    this.data = null;
  }
  
  public RpcRemoteException(int code, String msg, String data)
  {
    super(msg);
    this.code = code;
    this.msg  = msg;
    this.data = data;
  }
  
  public int getCode() {
    return code;
  }
  
  public String getMsg() {
    return msg;
  }
  
  public String getData() {
    return data;
  }
}
