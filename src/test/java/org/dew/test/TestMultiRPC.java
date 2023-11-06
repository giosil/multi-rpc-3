package org.dew.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.rpc.client.JsonRpcInvoker;

import org.rpc.client.RpcClientTransport;
import org.rpc.client.RpcInvoker;

import org.rpc.server.MultiRpcExecutor;
import org.rpc.server.RpcExecutor;
import org.rpc.server.RpcServerTransport;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public 
class TestMultiRPC extends TestCase implements RpcServerTransport, RpcClientTransport 
{
  // Executor
  private RpcExecutor rpcExecutor;
  // Buffer
  private String sRequestData;
  private String sResponseData;

  public TestMultiRPC(String testName) {
    super(testName);

    rpcExecutor = new MultiRpcExecutor();
    rpcExecutor.addHandler("TEST", this);
  }

  public static Test suite() {
    return new TestSuite(TestMultiRPC.class);
  }

  public void testApp() throws Throwable {
    List<Object> parameters = new ArrayList<Object>();
    parameters.add("World");
    
    RpcInvoker rpcInvoker = new JsonRpcInvoker();
    rpcInvoker.setTransport(this);
    Object result = rpcInvoker.invoke("TEST.hello", parameters);
    
    assert(result.equals("Hello World!"));
  }
  
  // Handler methods ------------------------------------------------
  
  public String hello(String name) {
    return "Hello " + name + "!";
  }

  // RpcServerTransport ---------------------------------------------
  @Override
  public String[] readRequest(String sContentType) throws Exception {
    String[] asResult = new String[2];
    asResult[1] = sRequestData;
    if (sContentType == null || sContentType.length() == 0) {
      // Is XML?
      int iLength = asResult[1].length();
      boolean boXml = false;
      for (int i = 0; i < iLength; i++) {
        char c = asResult[1].charAt(i);
        if (c > 33) {
          boXml = c == '<';
          break;
        }
      }
      if (boXml) {
        int j = 2;
        char[] acLast3 = { 32, 32, 32 };
        for (int i = iLength - 1; i >= 0 && j >= 0; i--) {
          char c = asResult[1].charAt(i);
          if (j == 2 && c < 33)
            continue;
          acLast3[j--] = c;
        }
        String sLast3 = new String(acLast3);
        if (sLast3.equals("pe>")) { // Envelope>
          asResult[0] = "application/soap+xml";
        } else {
          asResult[0] = "text/xml";
        }
      } else {
        asResult[0] = "application/json";
      }
    } else {
      asResult[0] = sContentType;
    }
    return asResult;
  }

  @Override
  public boolean checkAuthorization(String methodName) {
    return true;
  }

  @Override
  public void writeResponse(String sContentType, String responseData, boolean boTransEncChunked) throws Exception {
    this.sResponseData = responseData;
  }
  
  @Override
  public void setEncoding(String encoding) {
    System.setProperty("file.encoding", encoding);
  }

  @Override
  public String getEncoding() {
    return System.getProperty("file.encoding");
  }

  // RpcClientTransport ---------------------------------------------
  @SuppressWarnings("rawtypes")
  @Override
  public void setHeaders(Map headers) {
  }

  @Override
  public void setTimeOut(int iTimeOut) {
  }

  @Override
  public String call(String requestData) throws Exception {
    this.sRequestData = requestData;
    System.out.println(sRequestData);
    rpcExecutor.execute(this);
    System.out.println(sResponseData);
    return sResponseData;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public String call(String requestData, Map headers) throws Exception {
    this.sRequestData = requestData;
    System.out.println(sRequestData);
    rpcExecutor.execute(this);
    System.out.println(sResponseData);
    return sResponseData;
  }
}
