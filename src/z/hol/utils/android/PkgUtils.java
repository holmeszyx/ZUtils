package z.hol.utils.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 包相关工具
 * @author holmes
 *
 */
public class PkgUtils{
     
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
