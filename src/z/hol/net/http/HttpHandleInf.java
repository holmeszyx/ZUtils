package z.hol.net.http;

import java.util.List;

import org.apache.http.NameValuePair;

/**
 * Http 操作接口，可以进行Http post与get
 * @author holmes
 *
 */
public interface HttpHandleInf {
	
	/**
	 * get data with http by http get method
	 * @param url http get url
	 * @return
	 */
	public Response httpGet(int type, String url);
	
	public Response httpGet(int type, String url, List<NameValuePair> params);
	
	public Response httpGet(String url);
	
	/**
	 * get data with http by http post method
	 * @param url http post url
	 * @return
	 */
	public Response httpPost(int type, String url, List<NameValuePair> params);
	
	public Response httpPost(String url, List<NameValuePair> params);
	/**
	 * only post data to server with http by http post method
	 * @param url http post url
	 */
	public void httpPostNoResponse(int type, String url, List<NameValuePair> params);
	
	public void httpPostNoResponse(String url, List<NameValuePair> params);

}
