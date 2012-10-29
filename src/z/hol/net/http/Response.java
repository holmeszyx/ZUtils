package z.hol.net.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;


public class Response {
	
	public static final int STATUS_OK = 200;
	public static final int STATUS_FAILURE = 403;
	public static final int STATUS_TOKEN_ERROR = 401;
	
	private int type;
	private int statusCode;
	private Header[] headers;
	private byte[] rawContent;
	private String strContent = null;
	
	public Response(int type, HttpResponse httpResp){
		this.type = type;
		statusCode = httpResp.getStatusLine().getStatusCode();
		headers = httpResp.getAllHeaders();
		try {
			rawContent = EntityUtils.toByteArray(httpResp.getEntity());
			httpResp.getEntity().consumeContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Response(HttpResponse httpResp){
		this(NetConst.UNKNOWN, httpResp);
	}
	
	public int getType(){
		return type;
	}
	
	public int getStatusCode(){
		return statusCode;
	}
	
	/*
	public void setStatusCode(int code){
		statusCode = code;
	}
	*/
	public Header[] getAllHeaders(){
		return headers;
	}
	
	public String getHeader(String headName){
		headName = headName.trim();
		String value = null;
		for (Header header : headers){
			if (header.getName().equals(headName)){
				value = header.getValue();
				break;
			}
		}
		
		return value;
	}
	
	public byte[] getRawContent(){
		return rawContent;
	}
	
	public String getUtf8ContentString(){
		if (strContent == null && rawContent != null){
			try {
				strContent = new String(rawContent, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return strContent;
	}
	
}
