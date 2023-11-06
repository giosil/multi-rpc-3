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

package org.rpc.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@SuppressWarnings({"rawtypes","unchecked"})
public
class HttpRpcClientTransport implements RpcClientTransport
{
  protected URL url;
  protected Map headers;
  protected int timeOut;
  
  public HttpRpcClientTransport(URL url)
  {
    this.url = url;
  }
  
  public
  String getURL()
  {
    return url.toString();
  }
  
  public
  void setHeader(String key, String value)
  {
    if(headers == null) headers = new HashMap();
    this.headers.put(key, value);
  }
  
  public
  void setHeaders(Map headers)
  {
    this.headers = headers;
  }
  
  public
  void setTimeOut(int iTimeOut)
  {
    this.timeOut = iTimeOut;
  }
  
  public
  String call(String requestData)
    throws Exception
  {
    return post(url, headers, requestData);
  }
  
  public
  String call(String requestData, Map callHeaders)
    throws Exception
  {
    if(callHeaders == null) {
      callHeaders = headers;
    }
    else {
      if(headers != null) {
        Iterator iterator = headers.entrySet().iterator();
        while(iterator.hasNext()) {
          Map.Entry entry = (Map.Entry) iterator.next();
          try { headers.put(entry.getKey(), entry.getValue()); } catch(Exception ignore) {}
        }
      }
    }
    
    return post(url, headers, requestData);
  }
  
  protected
  String post(URL url, Map headers, String data)
    throws IOException
  {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    
    if(headers != null && !headers.isEmpty()) {
      Iterator iterator = headers.entrySet().iterator();
      while(iterator.hasNext()) {
        Map.Entry entry = (Map.Entry) iterator.next();
        Object oKey   = entry.getKey();
        Object oValue = entry.getValue();
        String sValue = oValue != null ? oValue.toString() : null;
        connection.addRequestProperty(oKey.toString(), sValue);
      }
    }
    connection.addRequestProperty("Accept-Encoding", "gzip,deflate");
    if(data.startsWith("<")) {
      connection.addRequestProperty("Content-Type", "text/xml");
    }
    else {
      connection.addRequestProperty("Content-Type", "application/json");
    }
    connection.setRequestMethod("POST");
    connection.setDoOutput(true);
    // 1.5+
    if(timeOut > 0) {
      connection.setConnectTimeout(timeOut);
      connection.setReadTimeout(timeOut);
    }
    connection.connect();
    
    OutputStream out = null;
    try {
      out = connection.getOutputStream();
      
      out.write(data.getBytes());
      out.flush();
      out.close();
      
      int statusCode = connection.getResponseCode();
      if(statusCode != HttpURLConnection.HTTP_OK) {
        throw new IOException("unexpected status code returned : " + statusCode);
      }
    }
    finally {
      if(out != null) try{ out.close(); } catch(Exception ex) {}
    }
    
    String responseEncoding = connection.getHeaderField("Content-Encoding");
    responseEncoding = (responseEncoding == null ? "" : responseEncoding.trim());
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    InputStream in = null;
    try {
      in = connection.getInputStream();
      if("gzip".equalsIgnoreCase(responseEncoding)) {
        in = new GZIPInputStream(in);
      }
      in = new BufferedInputStream(in);
      
      byte[] buff = new byte[1024];
      int n;
      while((n = in.read(buff)) > 0) {
        bos.write(buff, 0, n);
      }
      bos.flush();
      bos.close();
    }
    finally {
      if(in != null) try{ in.close(); } catch(Exception ex) {}
    }
    return bos.toString();
  }
}
