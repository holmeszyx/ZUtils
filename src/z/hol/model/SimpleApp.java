package z.hol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import z.hol.utils.FileUtils;
import android.text.TextUtils;

public class SimpleApp implements Serializable{
	private static final long serialVersionUID = 2409399518037749844L;
	
	private String packageName;
	private String icon;
	private String name;
	private String versionName;
	private int versionCode;
	private long size;
	private String formatedSize = null;
	private String appUrl;
	private long appId;
	
	private String userData1;
	private String userData2;
	private String userData3;
	
	
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	public int getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getAppUrl() {
		return appUrl;
	}
	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}
	public long getAppId() {
		return appId;
	}
	public void setAppId(long appId) {
		this.appId = appId;
	}
	public void setFormatedSize(String size){
		formatedSize = size;
	}
	
	public String getFormatedSize(){
		if (TextUtils.isEmpty(formatedSize)){
			formatedSize = FileUtils.formatFileSize(size);
		}
		return formatedSize;
	}
	
	public String getUserData1() {
		return userData1;
	}
	public void setUserData1(String data) {
		this.userData1 = data;
	}
	public String getUserData2() {
		return userData2;
	}
	public void setUserData2(String data) {
		this.userData2 = data;
	}
	public String getUserData3() {
		return userData3;
	}
	public void setUserData3(String data) {
		this.userData3 = data;
	}
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if (o == null){
			return false;
		}
		if (o instanceof SimpleApp){
			if (((SimpleApp) o).getAppId() == this.getAppId())
				return true;
			else
				return false;
		}else{
			return false;
		}
		//return super.equals(o);
	}
	
	public static List<SimpleApp> fromJsonArray(JSONArray json) throws JSONException{
		int length = json.length();
		if (length < 16){
			length = 16;
		}
		List<SimpleApp> appList = new ArrayList<SimpleApp>(length);
		for (int i = 0; i < json.length(); i ++){
			SimpleApp app = fromJson(json.getJSONObject(i));
			if (app != null)
				appList.add(app);
		}
		return appList;
	}
	
	public static SimpleApp fromJson(JSONObject json) throws JSONException{
		if (json != null){
			SimpleApp app = new SimpleApp();
			app.setAppId(json.getLong("id"));
			app.setPackageName(json.getString("package"));
			app.setIcon(json.getString("icon"));
			app.setName(json.getString("name"));
			app.setVersionName(json.getString("version_name"));
			app.setVersionCode(json.getInt("version_code"));
			//app.setSize(json.getLong("size"));
			app.setFormatedSize(json.getString("size"));
			app.setAppUrl(json.getString("url"));
			return app;
		}
		return null;
	}
	
	public static class Detail{
		public String introduction;
		public int level;
		public String reason;
		public String[] screens;
		
		public static Detail fromJson(JSONObject json) throws JSONException{
			if (json != null){
				Detail detail = new Detail();
				detail.introduction = json.getString("introduction");
				detail.level = json.getInt("level");
				detail.reason = json.isNull("reason") ? null: json.getString("reason");
				detail.screens = getStringArray(json.getJSONArray("screens"));
				return detail;
			}
			return null;
		}
		
		private static String[] getStringArray(JSONArray jsonArray) throws JSONException{
			int length = jsonArray.length();
			String[] array = new String[length];
			for (int i = 0; i < length; i ++){
				array[i] = jsonArray.getString(i);
			}
			return array;
		}
	}
	
}
