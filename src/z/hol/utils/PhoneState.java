package z.hol.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

public class PhoneState {
	public static final int PHONE_TYPE_SIP = 3;
	
	public static final int NETWORK_TYPE_IDEN = 11;
	public static final int NETWORK_TYPE_LTE = 13;
	public static final int NETWORK_TYPE_EVDO_B = 12;
	public static final int NETWORK_TYPE_EHRPD = 14;
	public static final int NETWORK_TYPE_HSPAP = 15;
	
	private TelephonyManager mTelephonyManager;
	
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
			typeStr = "cdma";
			break;
		case TelephonyManager.PHONE_TYPE_GSM:
			typeStr = "gsm";
			break;
		case TelephonyManager.PHONE_TYPE_NONE:
			typeStr = "none";
			break;
		case PHONE_TYPE_SIP:
			typeStr = "sip";
			break;
		default:
			typeStr = "unknown";
			break;
		}
		return typeStr; 
	}
	
	public int getMobileNetType(){
		return mTelephonyManager.getNetworkType();
	}
	
	public static String mobileNetType2String(int type){
		String str = null;
		switch (type){
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			str = "1xrtt";
			break;
		case TelephonyManager.NETWORK_TYPE_CDMA:
			str = "cdma";
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			str = "edge";
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			str = "evdo_0";
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			str = "evd0_a";
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			str = "gprs";
			break;
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			str = "hsdpa";
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			str = "hspa";
			break;
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			str = "hsupa";
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			str = "umts";
			break;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			str = "unknown";
			break;
		case NETWORK_TYPE_EHRPD:
			str = "ehrpd";
			break;
		case NETWORK_TYPE_EVDO_B:
			str = "evdo_b";
			break;
		case NETWORK_TYPE_HSPAP:
			str = "hspap";
			break;
		case NETWORK_TYPE_IDEN:
			str = "iden";
			break;
		case NETWORK_TYPE_LTE:
			str = "lte";
			break;
		default:
			str = "unknown";
			break;
		}
		return str;
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
