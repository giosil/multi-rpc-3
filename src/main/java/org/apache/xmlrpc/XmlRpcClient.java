package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright(c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation(http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "XML-RPC" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES(INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.net.*;
import java.io.*;
import java.util.*;

import org.rpc.util.Base64Coder;
import org.xml.rpc.client.XmlRpcInvoker;

@SuppressWarnings({"rawtypes","unchecked"})
public
class XmlRpcClient
{
  private XmlRpcInvoker xmlRpcInvoker;
  
  public
  XmlRpcClient(URL url)
  {
    xmlRpcInvoker = new XmlRpcInvoker(url);
    xmlRpcInvoker.setLegacy(true);
  }
  
  public
  XmlRpcClient(String sURL)
    throws MalformedURLException
  {
    xmlRpcInvoker = new XmlRpcInvoker(sURL);
    xmlRpcInvoker.setLegacy(true);
  }
  
  public
  void setBasicAuthentication(String username, String password)
  {
    if(username == null || username.length() == 0) {
      xmlRpcInvoker.getTransport().setHeaders(null);
    }
    else {
      Map mapHeaders = new HashMap(1);
      mapHeaders.put("Authorization", "Basic " + Base64Coder.encodeString(username + ":" + password));
      xmlRpcInvoker.getTransport().setHeaders(mapHeaders);
    }
  }
  
  public
  URL getURL()
  {
    String sURL = xmlRpcInvoker.getURL();
    if(sURL != null && sURL.length() > 0) {
      try {
        return new URL(sURL);
      }
      catch(MalformedURLException ex) {
        return null;
      }
    }
    return null;
  }
  
  public
  Object execute(String sMethod, Vector oParams)
    throws XmlRpcException, IOException
  {
    Object oResult = null;
    try {
      oResult = xmlRpcInvoker.invoke(sMethod, oParams);
    }
    catch(IOException ioex) {
      throw ioex;
    }
    catch(XmlRpcException xmlrpcex) {
      throw xmlrpcex;
    }
    catch(Throwable th) {
      String sMessage = th.getMessage();
      if(sMessage == null || sMessage.length() == 0) {
        sMessage = th.toString();
      }
      throw new XmlRpcException(0, sMessage);
    }
    return oResult;
  }
  
  public
  void executeAsync(String sMethod, Vector oParams, final AsyncCallback oCallback)
  {
    xmlRpcInvoker.invokeAsync(sMethod, oParams, new org.rpc.client.AsyncCallback() {
      
      public
      void handleResult(String sMethod, Collection colArgs, Object result)
      {
        oCallback.handleResult(result, getURL(), sMethod);
      }
      
      public
      void handleError(String sMethod, Collection colArgs, Throwable error)
      {
        if(error instanceof Exception) {
          oCallback.handleError((Exception) error, getURL(), sMethod);
        }
        else {
          oCallback.handleError(new Exception(error), getURL(), sMethod);
        }
      }
    });
  }
  
  /**
   * Senza modificare l'interfaccia originaria di XmlRpcClient si utilizza
   * il metodo equals per passare la mappa degli headers (java.util.Map) o il time out java.lang.Number)
   */
  public
  boolean equals(Object obj)
  {
    if(obj instanceof Map) {
      xmlRpcInvoker.getTransport().setHeaders((Map) obj);
      return false;
    }
    else
    if(obj instanceof Number) {
      xmlRpcInvoker.getTransport().setTimeOut(((Number) obj).intValue());
      return false;
    }
    return super.equals(obj);
  }
  
  public
  String toString()
  {
    return xmlRpcInvoker.toString();
  }
}
