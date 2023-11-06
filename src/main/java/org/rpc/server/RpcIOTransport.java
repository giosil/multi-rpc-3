package org.rpc.server;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public
class RpcIOTransport implements RpcServerTransport
{
  private static final int BUFF_LENGTH = 1024;
  
  private InputStream  is;
  private OutputStream os;
  
  public
  RpcIOTransport(InputStream is, OutputStream os)
  {
    this.is = is;
    this.os = os;
  }
  
  public
  String[] readRequest(String sContentType)
      throws Exception
  {
    String[] asResult = new String[2];
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buff = new byte[BUFF_LENGTH];
      int n;
      while((n = is.read(buff)) > 0) {
        bos.write(buff, 0, n);
      }
      asResult[1] = bos.toString();
      if(sContentType == null || sContentType.length() == 0) {
        // Is XML?
        int iLength = asResult[1].length();
        boolean boXml  = false;
        for(int i = 0; i < iLength; i++) {
          char c = asResult[1].charAt(i);
          if(c > 33) {
            boXml = (c == '<');
            break;
          }
        }
        if(boXml) {
          int j = 2;
          char[] acLast3 = {32, 32, 32};
          for(int i = iLength-1; i >= 0 && j >= 0; i--) {
            char c = asResult[1].charAt(i);
            if(j == 2 && c < 33) continue;
            acLast3[j--] = c;
          }
          String sLast3 = new String(acLast3);
          if(sLast3.equals("pe>")) { // Envelope>
            asResult[0] = "application/soap+xml";
          }
          else {
            asResult[0] = "text/xml";
          }
        }
        else {
          asResult[0] = "application/json";
        }
      }
      else {
        asResult[0] = sContentType;
      }
    }
    finally {
      if(is != null) try{ is.close(); } catch(Exception ex) {}
    }
    return asResult;
  }
  
  public
  boolean checkAuthorization(String methodName)
  {
    return true;
  }
  
  public
  void writeResponse(String sContentType, String responseData, boolean boTransEncChunked)
      throws Exception
  {
    PrintWriter out = null;
    try {
      out = new PrintWriter(os);
      out.write(responseData, 0, responseData.length());
      out.flush();
    }
    finally {
      if(out != null) try{ out.close(); } catch(Exception ex) {}
    }
  }
  
  public
  void setEncoding(String encoding)
  {
    System.setProperty("file.encoding", encoding);
  }
  
  public
  String getEncoding()
  {
    String sEncoding = null;
    String sFileEncoding = System.getProperty("file.encoding");
    if(sFileEncoding != null && sFileEncoding.startsWith("Cp")) {
      sEncoding = "ISO-8859-1";
    }
    else if(sFileEncoding != null && sFileEncoding.startsWith("ISO-")) {
      sEncoding = sFileEncoding;
    }
    else if(sFileEncoding != null && sFileEncoding.startsWith("UTF-")) {
      sEncoding = sFileEncoding;
    }
    else {
      sEncoding = "ISO-8859-1";
    }
    return sEncoding;
  }
}
