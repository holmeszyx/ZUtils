package z.hol.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

public class PhoneState {
	TelephonyManager mTelephonyManager;
	
	private static PhoneState mPhoneState;
	
	private PhoneState(Context context) {
		super();
		// TODO Auto-generated constructor stub
		this.mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	public static PhoneState get(Context context){
		if (mPhoneState == null){
			mPhoneState = new PhoneState(context);
		}
		return mPhoneState;
	}

	public String getPhoneId(){
		return mTelephonyManager.getDeviceId();
	}
	
	public String getPhoneIMSI(){
		return mTelephonyManager.getSubscriberId();
	}
	
	public int getPhoneType(){
		return mTelephonyManager.getPhoneType();
	}
	
	public static String phoneType2String(int type){
		String typeStr = null;
		switch(type){
		case TelephonyManager.PHONE_TYPE_CDMA:
			typeStr = "CDMA";
			break;
		case TelephonyManager.PHONE_TYPE_GSM:
			typeStr = "GSM";
			break;
		case TelephonyManager.PHONE_TYPE_NONE:
			typeStr = "NONE";
			break;
		default:
			typeStr = "UnKnow";
			break;
		}
		return typeStr; 
	}
	
	public String getNetworkOpertor(){
		return mTelephonyManager.getNetworkOperatorName();
	}
	
	public static String getDevice(){
		return android.os.Build.DEVICE;
	}
	
	public static String getPhoneModel(){
		return android.os.Build.MODEL;
	}
	
	public static String getPhoneSDK(){
		return android.os.Build.VERSION.SDK;
	}
	
	public static String getPhoneRelease(){
		return android.os.Build.VERSION.RELEASE;
	}
}
