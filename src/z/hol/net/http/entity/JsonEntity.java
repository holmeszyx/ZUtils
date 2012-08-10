package z.hol.net.http.entity;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import z.hol.net.http.utils.Utils;

public class JsonEntity extends StringEntity{
	
	public JsonEntity(final String json, final String charset) throws UnsupportedEncodingException{
		super(json, charset);
		setContentType(Utils.createContentType("application/json", charset));
	}
	
	public JsonEntity(final List<? extends NameValuePair> params, final String charset) throws UnsupportedEncodingException, JSONException{
		this(Utils.toJson(params, charset) , charset);
	}

	public JsonEntity(final JSONObject obj, final String charset) throws UnsupportedEncodingException{
		this(obj.toString(), charset);
	}
}
