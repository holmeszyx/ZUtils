package z.hol.utils.bitmapfun.ex;

import java.io.File;

import z.hol.utils.ImageUtil;
import z.hol.utils.bitmapfun.ImageCache;
import z.hol.utils.bitmapfun.ImageCache.ImageCacheParams;
import z.hol.utils.bitmapfun.ImageDownloaderEx;
import z.hol.utils.bitmapfun.ImageWorker;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class AppImageWorker extends ImageWorker{

	public AppImageWorker(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		ImageCacheParams cacheParams = new ImageCacheParams("APK");
		cacheParams.diskCacheEnabled = false;
		cacheParams.memCacheSize = ImageDownloaderEx.MEM_CACHE_SIZE;
		ImageCache cache = new ImageCache(mContext, cacheParams);
		setImageCache(cache);
	}

	@Override
	protected Bitmap processBitmap(Object data) {
		// TODO Auto-generated method stub
		if (data instanceof PackageInfo){
			return ImageUtil.drawableToBitmap(getApkIcon(mContext, ((PackageInfo) data).applicationInfo));
		}else if (data instanceof ResolveInfo){
			return ImageUtil.drawableToBitmap(getApkIcon(mContext, (ResolveInfo) data));
		}else if (data instanceof ApplicationInfo){
			return ImageUtil.drawableToBitmap(getApkIcon(mContext, (ApplicationInfo) data));
		}else if (data instanceof String){
			File f = new File((String)data);
			if (f.exists()){
				return ImageUtil.drawableToBitmap(getApkIcon(mContext, f.getAbsolutePath()));
			}
		}
		
		return null;
	}
	
	public static Drawable getApkIcon(Context context, ResolveInfo pkgInfo){
		PackageManager pm = context.getPackageManager();
		if (pkgInfo != null){
			return pkgInfo.loadIcon(pm);
		}
		return null;
	}
	
	public static Drawable getApkIcon(Context context, ApplicationInfo appInfo){
		PackageManager pm = context.getPackageManager();
		if (appInfo != null){
			return appInfo.loadIcon(pm);
		}
		return null;
	}

    /*
     * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
     * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
     * http://code.google.com/p/android/issues/detail?id=9151
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e("ApkIconLoader", e.toString());
            }
        }
        return null;
    }
}
