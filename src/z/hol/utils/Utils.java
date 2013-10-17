package z.hol.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 工具
 * @author holmes
 *
 */
public class Utils{
     
     
     /**
      * 获取百分比
      * @param total
      * @param current
      * @return 百分比，如果total小于等0,则直接返回100
      */
     public static int computePercent(int total, int current){
    	int percent = 100;
    	if (total > 0)
    		percent = current * 100 / total;
    	return percent;
     }
     
     /**
      * 过滤字符串中中文字符
      * @param str
      * @return
      */
     public static String filterChars(String str, String spaceChar){
    	 String f = str.replaceAll("[\u4e00-\u9fa5\u3002\uff1b\uff0c\uff1a\u201c\u201d\uff08\uff09\u3001\uff1f\u300a\u300b\u3010\u3011\\\\/]", "");
    	 if (spaceChar != null){
    		 f = f.replaceAll(" ", spaceChar);
    	 }
    	return f;
     }

     
     private static int VERSION_CODE = -1;
     private static String VERSION_NAME = null;
     
     /**
      * 获取当前应用的版本状态
      * @param context
      */
     private static void getVersionState(Context context){
    	if (VERSION_CODE == -1 && VERSION_NAME == null){
     		PackageManager pm = context.getPackageManager();
     		try {
     			PackageInfo pkg = pm.getPackageInfo(context.getPackageName(), 0);
     			VERSION_CODE = pkg.versionCode;
     			VERSION_NAME = pkg.versionName;
     		} catch (NameNotFoundException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
    	}
     }
     
     /**
      * 获取当前应用的版本号
      * @param context
      * @return
      */
     public static int getVersionCode(Context context){
    	 getVersionState(context);
    	 return VERSION_CODE;
     }
     
     /**
      * 获取当前应用的版本名称
      * @param context
      * @return
      */
     public static String getVersionName(Context context){
    	 getVersionState(context);
    	 return VERSION_NAME;
     }

}
