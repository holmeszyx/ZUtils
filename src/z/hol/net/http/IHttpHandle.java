package z.hol.net.http;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

/**
 * Http 操作接口，可以进行Http post与get
 * @author holmes
 *
 */
public interface IHttpHandle {
	
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
	
	public Response httpPost(int type, String url, List<NameValuePair> params, boolean json);
	
	
	public Response httpPost(String url, List<NameValuePair> params);
	
	/**
	 * Post a entity
	 * @param url
	 * @param entity
	 * @return
	 */
	public Response httpPost(String url, HttpEntity entity);

	/**
	 * Post json string with application/json content type
	 * @param url
	 * @param json
	 * @return
	 */
	public Response httpPostJson(String url, String json);
	/**
	 * only post data to server with http by http post method
	 * @param url http post url
	 */
	public void httpPostNoResponse(int type, String url, List<NameValuePair> params);
	
	public void httpPostNoResponse(String url, List<NameValuePair> params);

}
