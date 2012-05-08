package z.hol.net.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

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
}
