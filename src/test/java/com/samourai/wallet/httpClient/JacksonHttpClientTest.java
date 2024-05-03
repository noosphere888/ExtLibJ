package com.samourai.wallet.httpClient;

import com.samourai.wallet.api.backend.beans.HttpException;
import com.samourai.wallet.api.paynym.beans.PaynymErrorResponse;
import com.samourai.wallet.httpClient.JacksonHttpClient;
import com.samourai.wallet.test.AbstractTest;
import com.samourai.wallet.util.AsyncUtil;
import com.samourai.wallet.util.JSONUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class JacksonHttpClientTest extends AbstractTest {

  private JacksonHttpClient httpClient;
  private String mockResponse;
  private HttpException mockException;

  @BeforeEach
  public void setUp() throws Exception{
    super.setUp();
    this.mockException = null;
  }

  public JacksonHttpClientTest() throws Exception {
    PaynymErrorResponse mock = new PaynymErrorResponse();
    mock.message = "test";
    mockResponse = JSONUtils.getInstance().getObjectMapper().writeValueAsString(mock);

    httpClient = new JacksonHttpClient(null) {
      @Override
      protected String requestJsonGet(String urlStr, Map<String, String> headers, boolean async) throws HttpException {
        if (mockException != null) {
          throw mockException;
        }
        return mockResponse;
      }

      @Override
      protected String requestJsonPost(String urlStr, Map<String, String> headers, String jsonBody) throws HttpException {
        if (mockException != null) {
          throw mockException;
        }
        return mockResponse;
      }

      @Override
      protected String requestJsonPostUrlEncoded(String urlStr, Map<String, String> headers, Map<String, String> body) throws HttpException {
        if (mockException != null) {
          throw mockException;
        }
        return mockResponse;
      }

      @Override
      protected String requestStringPost(String urlStr, Map<String, String> headers, String contentType, String content) throws HttpException {
        if (mockException != null) {
          throw mockException;
        }
        return mockResponse;
      }

      @Override
      public void connect() throws Exception {}
    };
  }

  @Test
  public void getJsonSuccess() throws Exception {
    // success
    PaynymErrorResponse response = httpClient.getJson("http://test", PaynymErrorResponse.class, null);
    Assertions.assertEquals("test", response.message);

    // success: String - parseJson()
    String stringResponse = httpClient.getJson("http://test", String.class, null);
    Assertions.assertEquals("{\"message\":\"test\"}", stringResponse);
  }

  @Test
  public void getJsonException() throws Exception {
    // exception
    mockException = new HttpNetworkException("test");
    try {
      httpClient.getJson("http://test", PaynymErrorResponse.class, null);
    } catch (HttpException e) {
      Assertions.assertEquals("test", e.getMessage());
    }

    // exception: String - parseJson()
    try {
      httpClient.getJson("http://test", String.class, null);
    } catch (HttpException e) {
      Assertions.assertEquals("test", e.getMessage());
    }
  }

  @Test
  public void postJsonSuccess() throws Exception {
    // success
    PaynymErrorResponse response = AsyncUtil.getInstance().blockingGet(httpClient.postJson("http://test", PaynymErrorResponse.class, null, null)).get();
    Assertions.assertEquals("test", response.message);

    // success: String - parseJson()
    String stringResponse = AsyncUtil.getInstance().blockingGet(httpClient.postJson("http://test", String.class, null, null)).get();
    Assertions.assertEquals("{\"message\":\"test\"}", stringResponse);
  }

  @Test
  public void postJsonException() throws Exception {
    // exception
    mockException = new HttpNetworkException("test");
    try {
      AsyncUtil.getInstance().blockingGet(httpClient.postJson("http://test", PaynymErrorResponse.class, null, null)).get();
    } catch (HttpException e) {
      Assertions.assertEquals("test", e.getMessage());
    }

    // exception: String - parseJson()
    try {
      AsyncUtil.getInstance().blockingGet(httpClient.postJson("http://test", String.class, null, null)).get();
    } catch (HttpException e) {
      Assertions.assertEquals("test", e.getMessage());
    }
  }

  @Test
  public void postUrlEncodedSuccess() throws Exception {
    // success
    PaynymErrorResponse response = httpClient.postUrlEncoded("http://test", PaynymErrorResponse.class, null, null);
    Assertions.assertEquals("test", response.message);

    // success: String - parseJson()
    String stringResponse = httpClient.postUrlEncoded("http://test", String.class, null, null);
    Assertions.assertEquals("{\"message\":\"test\"}", stringResponse);
  }

  @Test
  public void postUrlEncodedException() throws Exception {
    // exception
    mockException = new HttpNetworkException("test");
    try {
      httpClient.postUrlEncoded("http://test", PaynymErrorResponse.class, null, null);
    } catch (HttpException e) {
      Assertions.assertEquals("test", e.getMessage());
    }

    // exception: String - parseJson()
    try {
      httpClient.postUrlEncoded("http://test", String.class, null, null);
    } catch (HttpException e) {
      Assertions.assertEquals("test", e.getMessage());
    }
  }
}
