package z.hol.net.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import z.hol.net.http.HttpDataFetch;

public class HttpUtils {
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
	
	/**
	 * 生成一个Http参数列表
	 * @param names
	 * @param values
	 * @return
	 */
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
	 * 获取服务器文件最后更新时间
	 * @param url
	 * @return
	 */
	public static String getServerFileLastModifyDate(String url){
		String lastModifiedDate = null;
		
		HttpClient http = HttpDataFetch.getNewHttpClient();
		HttpContext context = new BasicHttpContext();
		HttpGet get = new HttpGet(url);
		HttpResponse response = null;
		try {
			response = http.execute(get, context);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (response != null && response.getStatusLine().getStatusCode() == 200){
			//System.out.println(response.getStatusLine().getStatusCode());
			
			// handler redirect
//			HttpUriRequest currentRequest = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
//			HttpHost currentHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
//			if (currentRequest instanceof RequestWrapper){
//				HttpUriRequest origin = (HttpUriRequest) ((RequestWrapper) currentRequest).getOriginal();
//				currentRequest = null;
//				currentRequest = origin;
//			}
//			if (currentRequest == get){
//				System.out.println("direct");
//			}else{
//				System.out.println("redirect");
//				System.out.println(mergeUrl(currentHost, currentRequest));
//			}
			
			Header lastModified = response.getFirstHeader("Last-Modified");
			if (lastModified != null){
				lastModifiedDate = lastModified.getValue();
				//System.out.println(lastModifiedDate);
			}
		}
		http.getConnectionManager().shutdown();
		return lastModifiedDate;
	}
	
	/**
	 * 合并URL
	 * @param host
	 * @param request
	 * @return
	 */
	public static String mergeUrl(HttpHost host,HttpUriRequest request){
		if (request.getURI().isAbsolute()){
			return request.getURI().toString();
		}else{
			return host.toString() + request.getURI().toString();
		}
	}
}
