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

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
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
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;

import z.hol.net.http.entity.GzipDecompressingEntity;
import z.hol.net.http.entity.JsonEntity;

public class HttpDataFetch implements IHttpHandle, HttpHeaderAddible{
	
	public static final String HTTP_HEAD_SESSION_KEY = "Cookie";
	public static final String HTTP_HEAD_SESSION_VALUE_HEAD = "sessionid=";
	public static final String HTTP_HEAD_USER_AGENT_KEY = "User-Agent";
	
	
	public static String token = "";
	public static long lastTimestamp = 0;
	public static String session = null;
	
	/**
	 * Gzip请求插值器
	 */
	private static final HttpRequestInterceptor HTTP_REQUEST_INTERCEPTOR = new HttpRequestInterceptor() {
		
		@Override
		public void process(HttpRequest request, HttpContext context)
				throws HttpException, IOException {
			// TODO Auto-generated method stub
            if (!request.containsHeader("Accept-Encoding")) {
                request.addHeader("Accept-Encoding", "gzip");
            }
		}
	};
	
	/**
	 * gzip响应插值器
	 */
	private static final HttpResponseInterceptor HTTP_RESPONSE_INTERCEPTOR = new HttpResponseInterceptor() {
		
		@Override
		public void process(HttpResponse response, HttpContext context)
				throws HttpException, IOException {
			// TODO Auto-generated method stub
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                Header ceheader = entity.getContentEncoding();
                if (ceheader != null) {
                    HeaderElement[] codecs = ceheader.getElements();
                    for (int i = 0; i < codecs.length; i++) {
                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(
                                    new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
		}
	};
	
	private static HashMap<String, String> sCommonHeader = null;

	//protected Context mContext;
	private HttpClient httpClient;
	private HashMap<String, String> mHeaders;
	private boolean gzipEnable = true;
	private boolean mAutoShutdown;
	private boolean mIsIgnoreCommenHeaders = false;
	
	/**
	 * 生成一个http请求器,
	 * 默认关闭https, 开启gzip, 开启自动关闭连接
	 */
	public HttpDataFetch(){
		this(true);
		//httpClient = getSSLHttpClient();
	}

	/**
	 * 生成一个Http请求器,默认关闭https，开启gzip
	 * @param autoshutdown 是否在完成一个请求后自动关闭连接
	 */
	public HttpDataFetch(boolean autoshutdown){
		this(false, true, autoshutdown);
	}
	
	//public HttpDataFetch(Context context){
	//	this(context, false);
	//}

	//public HttpDataFetch(boolean https){
	//	this(https, true);
	//}
	
	public HttpDataFetch(boolean https, boolean autoshutdown){
		this(https, true, autoshutdown);
	}
	
	/**
	 * 生成一个Http请求器。
	 * @param https 是否使用https
	 * @param gzip	是否开启gzip
	 * @param autoshutdown	是否完成一个请求后自动关闭连接
	 */
	public HttpDataFetch(boolean https, boolean gzip, boolean autoshutdown){
		//mContext = context;
		if (https){
			httpClient = getNewHttpClient();
		}else{
			httpClient  =  new DefaultHttpClient();
		}
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000); 
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000); 
		gzipEnable = gzip;
		
		if (gzipEnable){
			DefaultHttpClient defaultHttpClient = (DefaultHttpClient) httpClient;
			defaultHttpClient.addRequestInterceptor(HTTP_REQUEST_INTERCEPTOR);
			defaultHttpClient.addResponseInterceptor(HTTP_RESPONSE_INTERCEPTOR);
		}
		
		mAutoShutdown = autoshutdown;
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
	 * 设置是否忽略通用头
	 * @param enable
	 */
	public void setIgnoreCommenHeadersEnable(boolean enable){
		mIsIgnoreCommenHeaders = enable;
	}
	
	/**
	 * 是否已经忽略通用头
	 * @return
	 */
	public boolean isIgnoreCommenHeaders(){
		return mIsIgnoreCommenHeaders;
	}
	
	/**
	 * 添加通用头
	 * @param name
	 * @param value
	 */
	public static void addCommonHeader(String name, String value){
		if (name == null || value == null){
			return;
		}
		if (sCommonHeader == null){
			sCommonHeader = new HashMap<String, String>();
		}
		sCommonHeader.put(name, value);
	}
	
	/**
	 * 移除一个通用头
	 * @param name
	 */
	public static void removeCommonHeader(String name){
		if (name != null && sCommonHeader != null){
			sCommonHeader.remove(name);
		}
	}
	
	/**
	 * 清空通用头
	 */
	public static void clearCommonHeader(){
		if (sCommonHeader != null){
			sCommonHeader.clear();
		}
	}
	
	/**
	 * 应用自己添加的一些头部
	 * @param httpRequest
	 */
	private void insertAddedHeaders(HttpRequestBase httpRequest){
		if (!mIsIgnoreCommenHeaders){
			insertCommonHeaders(httpRequest);
		}
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
	
	private static void insertCommonHeaders(HttpRequestBase httpRequest){
		if (sCommonHeader == null || sCommonHeader.isEmpty()){
			return;
		}
		Set<Entry<String, String>> headers = sCommonHeader.entrySet();
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
		
		autoShutdown();
		
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
	public Response httpPost(int type, String url, List<NameValuePair> params,
			boolean json) {
		// TODO Auto-generated method stub
		Response data = null;
		HttpPost post = new HttpPost(url);
		try {
			insertAddedHeaders(post);
			if (params != null){
				HttpEntity entity = null;
				if (json){
					entity = new JsonEntity(params, HTTP.UTF_8);
				}else{
					entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
				}
				post.setEntity(entity);
			}
			HttpResponse response = httpClient.execute(post);
			data = new Response(type, response);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		autoShutdown();
		
		return data;
	}

	@Override
	public Response httpPost(int type, String url, List<NameValuePair> params) {
		return httpPost(type, url, params, false);
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
		autoShutdown();
	}

	@Override
	public void httpPostNoResponse(String url, List<NameValuePair> params) {
		httpPostNoResponse(NetConst.UNKNOWN, url, params);
	}
	
	private void autoShutdown(){
		if (mAutoShutdown){
			shutdown();
		}
	}
	
	/**
	 * 关闭连接
	 */
	public void shutdown(){
		httpClient.getConnectionManager().shutdown();
	}
	
	public static String getToken(){
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
