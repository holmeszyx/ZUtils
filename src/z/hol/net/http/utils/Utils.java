package z.hol.net.http.utils;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

	public static String toJson(final List<? extends NameValuePair> params, String charset) throws JSONException{
		JSONObject obj = new JSONObject();
		for (NameValuePair n : params){
			String encodedName = encode(n.getName(), charset);
			String v = encode(n.getValue(), charset);
			obj.put(encodedName, v == null ? JSONObject.NULL : v);
		}
		return obj.toString();
	}
	
	public static String createContentType(String mimeType, String charset){
        StringBuilder buf = new StringBuilder();
        buf.append(mimeType);
        if (charset != null) {
            buf.append("; charset=");
            buf.append(charset);
        }
        return buf.toString();
	}
	
	public static String encode(String content, String charset){
		if (content == null){
			return null;
		}
		try {
			return new String(content.getBytes(), charset);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException(e);
		}
	}
}
