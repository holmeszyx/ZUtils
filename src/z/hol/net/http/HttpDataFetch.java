package z.hol.net.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;

public class HttpDataFetch implements HttpHandleInf, HttpHeaderAddible{
	
	public static final String HTTP_HEAD_SESSION_KEY = "Cookie";
	public static final String HTTP_HEAD_SESSION_VALUE_HEAD = "sessionid=";
	public static final String HTTP_HEAD_USER_AGENT_KEY = "User-Agent";
	
	
	public static String token = "";
	public static long lastTimestamp = 0;
	public static long lastSMSThreadTimestamp = 0;
	public static String session = null;
	
	private HttpClient httpClient;
	private HashMap<String, String> mHeaders;
	protected Context mContext;
	
	public HttpDataFetch(){
		this(null);
		//httpClient = getSSLHttpClient();
	}
	
	public HttpDataFetch(Context context){
		this(context, false);
	}
	
	public HttpDataFetch(Context context, boolean https){
		mContext = context;
		if (https){
			httpClient = getNewHttpClient();
		}else{
			httpClient  =  new DefaultHttpClient();
		}
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000); 
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000); 
	}
	
	
	
	@Override
	public void addHeader(String name, String value) {
		
		if (name == null || value == null){
			return;
		}
		if (mHeaders == null){
			mHeaders = new HashMap<String, String>();
		}
		mHeaders.put(name, value);
	}

	@Override
	public void clearAddedHeaders() {
		if (mHeaders != null){
			mHeaders.clear();
		}
	}
	
	/**
	 * 应用自己添加的一些头部
	 * @param httpRequest
	 */
	private void insertAddedHeaders(HttpRequestBase httpRequest){
		if (mHeaders == null || mHeaders.isEmpty()){
			return;
		}
		Set<Entry<String, String>> headers = mHeaders.entrySet();
		Iterator<Entry<String, String>> iter = headers.iterator();
		while(iter.hasNext()){
			Entry<String, String> header = iter.next();
			httpRequest.addHeader(header.getKey(), header.getValue());
		}
	}

	@Override
	public Response httpGet(int type, String url) {
		Response data = null;
		HttpGet get = new HttpGet(url);
		try {
			insertAddedHeaders(get);
			HttpResponse response = httpClient.execute(get);
			data = new Response(type, response);
			
		}catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return data;
	}
	
	@Override
	public Response httpGet(int type, String url, List<NameValuePair> params) {
		String paramStr = toGetOperationParams(params);
		return httpGet(type, url + paramStr);
	}
	
	@Override
	public Response httpGet(String url) {
		return httpGet(NetConst.UNKNOWN, url);
	}

	@Override
	public Response httpPost(int type, String url, List<NameValuePair> params) {
		Response data = null;
		HttpPost post = new HttpPost(url);
		try {
			insertAddedHeaders(post);
			if (params != null){
				post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}
			HttpResponse response = httpClient.execute(post);
			data = new Response(type, response);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return data;
	}
	
	@Override
	public Response httpPost(String url, List<NameValuePair> params) {
		return httpPost(NetConst.UNKNOWN, url, params);
	}

	@Override
	public void httpPostNoResponse(int type, String url, List<NameValuePair> params) {
		HttpPost post = new HttpPost(url);
		try {
			if (params != null){
				post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}
			HttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				
			}else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND){
				
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void httpPostNoResponse(String url, List<NameValuePair> params) {
		httpPostNoResponse(NetConst.UNKNOWN, url, params);
	}
	
	/**
	 * 关闭连接
	 */
	public void shutdown(){
		httpClient.getConnectionManager().shutdown();
	}
	
	public static String getToken(Context context){
		String token = "";
		return token;
	}
	
	public static List<NameValuePair> getNameValuePairs(String data){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		BasicNameValuePair param= new BasicNameValuePair("data", data);
		params.add(param);
		return params;
	}
	
	public static List<NameValuePair> createNameValuePairs(String[] names, String[] values){
		List<NameValuePair> params = null;
		int nameSize = names.length;
		int valueSize = values.length;
		if (nameSize != valueSize){
			throw new IllegalArgumentException("Name size and values size are not equal");
		}
		params = new ArrayList<NameValuePair>();
		for (int i = 0; i < nameSize; i ++){
			BasicNameValuePair nameValuePair = new BasicNameValuePair(names[i], values[i]);
			params.add(nameValuePair);
		}
		
		return params;
	}
	
	/**
	 * 获取GET操作的URL参数表示<br>
	 * 类似 ?name=zyx&pass=123&note=%20%3Dfgs
	 * 
	 * @param params
	 * @return 返回URL参数，当params size为0时返回""
	 */
	public static String toGetOperationParams(List<NameValuePair> params){
		if (params.isEmpty()){
			return "";
		}
		String paramStr = URLEncodedUtils.format(params, HTTP.UTF_8);
		return "?" + paramStr;
	}
	
	@Deprecated
	public static HttpClient getSSLHttpClient(){
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
		 
		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		 
		ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(cm, params);
	}
	
    public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

}
