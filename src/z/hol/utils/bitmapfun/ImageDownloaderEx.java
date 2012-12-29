package z.hol.utils.bitmapfun;

import java.util.HashMap;

import z.hol.utils.bitmapfun.ImageCache.ImageCacheParams;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.widget.ImageView;

/**
 * 更强大的图片下载器
 * @author holmes
 *
 */
public class ImageDownloaderEx {
	public static final int MEM_CACHE_SIZE = 1024 * 1024 * 2;
	public static final String NAME_DETAIL = "detail";
	public static final String NAME_ICON = "icon";
	
	private ImageFetcher mImageWorker;
	
	public ImageDownloaderEx(Context context, SizeInfo size){
		mImageWorker = new ImageFetcher(context, size.width, size.height);
		ImageCacheParams cacheParams = new ImageCacheParams(size.forDetail ? NAME_DETAIL : NAME_ICON);
		cacheParams.memCacheSize = MEM_CACHE_SIZE;
		cacheParams.compressFormat = CompressFormat.PNG;
		mImageWorker.setImageCache(ImageCache.findOrCreateCache(context, cacheParams));
		mImageWorker.setImageSize(size.width, size.height);
	}
	public void download(String url, ImageView img){
		mImageWorker.setLoadingImage(null);
		mImageWorker.loadImage(url, img);
	}
	
	public void download(String url, ImageView img, int defaultPic){
		mImageWorker.setLoadingImage(defaultPic);
		mImageWorker.loadImage(url, img);
	}
	
	public void download(String url, ImageView img, int w, int h){
		mImageWorker.setImageSize(w, h);
		mImageWorker.loadImage(url, img);
	}
	
	public void download(String url, ImageView img, SizeInfo size){
		download(url, img, size.width, size.height);
	}
	
	public Bitmap getImageFromCache(String url){
		return mImageWorker.getImageFromCache(url);
	}
	
	/**
	 * 图片尺寸
	 * @author holmes
	 *
	 */
	public static final class SizeInfo{
		public int width;
		public int height;
		/**
		 * 当detail为true时
		 * 会使用detail缓存文件夹
		 * 否则使用icon缓存文件夹
		 */
		public boolean forDetail = false;
		
		public SizeInfo(){
			this(0, 0);
		}
		
		public SizeInfo(int w, int h){
			width = w;
			height = h;
		}
		
		public SizeInfo(SizeInfo s){
			this(s.width, s.height);
		}
		
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			//return super.hashCode();
			//return width * height ^ 31 * width ^ 13 * height;
			return width * 35731 + height;
		}
		
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			//return super.equals(o);
			if (o instanceof SizeInfo){
				SizeInfo s = (SizeInfo) o;
				return this.width == s.width && this.height == s.height;
			}
			return false;
		}
	}
	
	public static HashMap<SizeInfo, ImageDownloaderEx> IMAGE_DOWNLOADER_MAP = new HashMap<ImageDownloaderEx.SizeInfo, ImageDownloaderEx>();
	
	public static void clear(){
		IMAGE_DOWNLOADER_MAP.clear();
	}
	
	public static ImageDownloaderEx getOrCreate(Context context, SizeInfo size){
		ImageDownloaderEx imageDownloaderEx = null;
		imageDownloaderEx = IMAGE_DOWNLOADER_MAP.get(size);
		if (imageDownloaderEx == null){
			imageDownloaderEx = new ImageDownloaderEx(context.getApplicationContext(), size);
			IMAGE_DOWNLOADER_MAP.put(size, imageDownloaderEx);
		}
		return imageDownloaderEx;
	}
}
