/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package z.hol.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;


/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class ImageDownloader {
	public static final int NO_DEFAULT_PIC = -1;
	public static final String CACHE_PATH = "/sdcard/zicache/";
    private static final String LOG_TAG = "ImageDownloader";
    private static Boolean AUTO_CLEAR = true;	// is auto clear cache
    

    public enum Mode { NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT }
    private Mode mode = Mode.NO_ASYNC_TASK;
    
    private static ImageDownloader mImageDownloader = null;
    private static final byte[] INSTANCE_LOCK = new byte[0];
    
    /**
     * 获取单个实例
     * @return
     */
    public static ImageDownloader getInstance(){
    	if (mImageDownloader == null){
    		synchronized (INSTANCE_LOCK) {
    			if (mImageDownloader == null){
    				mImageDownloader = new ImageDownloader();
    				mImageDownloader.setMode(Mode.CORRECT);
    			}
			}
    	}
    	return mImageDownloader;
    }
    		
    public ImageDownloader(){
    	initCacheFolder();
    }
    
    private void initCacheFolder(){
    	File f = new File(CACHE_PATH);
    	if (!f.exists()){
    		if (f.mkdirs()){
    			// new cache folder created
    		}else{
    			// can't create cache folder
    			// so don't auto clear the memory cache
    			AUTO_CLEAR = false;
    		}
    		Log.i(LOG_TAG, "make cache dirs");
    	}else{
    		Log.i(LOG_TAG, "cache dir exists");
    	}
    	f = null;
    }
    
    public void download(String url, ImageView imageView){
    	download(url, imageView, NO_DEFAULT_PIC);
    }
    /**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void download(String url, ImageView imageView, int defaultPic) {
        resetPurgeTimer();
        Bitmap bitmap = getBitmapFromCache(url);

        if (bitmap == null) {
            forceDownload(url, imageView, defaultPic);
        } else {
            cancelPotentialDownload(url, imageView);
            imageView.setImageBitmap(bitmap);
            //设置图片过后,清空TAG
            imageView.setTag(null);
        }
    }
    
   /**
    * 只通获取已缓存的图片来设置图片显示。 <br>
    * 只当存在缓存的时候才会设置图片
    * @param url	图片URL
    * @param imageView 图片的的显示Imageview
    * @return 如果有缓存则返回true，否则false
    */
    public boolean setImageFromCache(String url, ImageView imageView){
    	if (TextUtils.isEmpty(url)){
    		return false;
    	}
        resetPurgeTimer();
        Bitmap bitmap = getBitmapFromCache(url);
        
        if (bitmap != null){
            cancelPotentialDownload(url, imageView);
            imageView.setImageBitmap(bitmap);
            //设置图片过后,清空TAG
            imageView.setTag(null);
            return true;
        }else{
        	//TODO 如果没有缓存
        	
        }
        
    	return false;
    }

    /*
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
       private void forceDownload(String url, ImageView view) {
          forceDownload(url, view, null);
       }
     */

    
    @SuppressWarnings("unused")
	private void forceDownload(String url, ImageView imageView){
    	forceDownload(url, imageView, NO_DEFAULT_PIC);
    }
    
    /**
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, ImageView imageView, int defaultPic) {
        // State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
        if (url == null) {
            imageView.setImageDrawable(null);
            return;
        }

        if (cancelPotentialDownload(url, imageView)) {
            switch (mode) {
                case NO_ASYNC_TASK:
                    Bitmap bitmap = downloadBitmap(url);
                    addBitmapToCache(url, bitmap);
                    imageView.setImageBitmap(bitmap);
                    break;

                case NO_DOWNLOADED_DRAWABLE:
                    imageView.setMinimumHeight(156);
                    BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, defaultPic);
                    task.execute(url);
                    break;

                case CORRECT:
                    task = new BitmapDownloaderTask(imageView, defaultPic);
                    DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
                    //为了有默认图片，改用tag去存当前的下载状态，即downloadDrawable
                    //imageView.setImageDrawable(downloadedDrawable);
                    if (defaultPic != NO_DEFAULT_PIC)
	                    imageView.setImageResource(defaultPic);	//设置默认图片
                    imageView.setTag(downloadedDrawable); 
                    imageView.setMinimumHeight(156);
                    task.execute(url);
                    break;
            }
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
        	//改用tag存储
            //Drawable drawable = imageView.getDrawable();
        	Object drawable = imageView.getTag();
        	if (drawable == null){
        		return null;
        	}
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
	Bitmap downloadBitmap(String url) {
    	
    	// 从SD卡拿图片
    	Bitmap image = getImageFromSD(url);
    	if (image != null){
    		return image;
    	}
    	
        final int IO_BUFFER_SIZE = 4 * 1024;

        // AndroidHttpClient is not allowed to be used from the main thread
        // for android 2.1
        /*
        final HttpClient client = (mode == Mode.NO_ASYNC_TASK) ? new DefaultHttpClient() :
            AndroidHttpClient.newInstance("Android");
        */
        final HttpClient client = new DefaultHttpClient();
        //final HttpClient client = HttpDataFetch.getNewHttpClient();
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    // return BitmapFactory.decodeStream(inputStream);
                    // Bug on slow connections, fixed in future release.
                    image = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                    cacheImageToFile(url, image);
                    return image;
                    //return BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                    // 加入圆角图后
                    /*Bitmap orgBitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                    Bitmap roundedBitmap = null;
                    if (orgBitmap != null){
                    	roundedBitmap = ImageUtil.getRoundedCornerBitmap(orgBitmap, 3);
                    	orgBitmap.recycle();
                    	orgBitmap = null;
                    }
                    return roundedBitmap;*/
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (IOException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
        } finally {
        	//not for android 2.1
        	/*
        	if ((client instanceof AndroidHttpClient)) {
                ((AndroidHttpClient) client).close();
            }
            */
        }
        return null;
    }

    /*
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
     */
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
    
    /**
     * MD5的reference
     */
    ConcurrentHashMap<String, WeakReference<String>> mWeakMD5Map = new ConcurrentHashMap<String, WeakReference<String>>(HARD_CACHE_CAPACITY);
    
    /**
     * 获取一个URL的MD5值
     * @param url
     * @return
     */
    private String getUrlMd5(String url){
    	WeakReference<String> md5Reference = mWeakMD5Map.get(url);
    	if (md5Reference != null){
    		String md5 = md5Reference.get();
    		if (md5 != null){
    			return md5;
    		}else{
    			mWeakMD5Map.remove(url);
    		}
    	}
    	
    	String md5 = MD5Util.getMD5String(url).toLowerCase();
    	md5Reference = new WeakReference<String>(md5);
    	mWeakMD5Map.put(url, md5Reference);
    	return md5;
    }
    
    /**
     * 获取一个本地缓存的文件
     * @param name
     * @return
     */
    private File getCacheFile(String name){
    	return new File(CACHE_PATH, name);
    }
    
    /**
     * 缓存一个图片到本地<br>
     * 根据情况开线程去缓存
     * @param url
     * @param bitmap
     */
    private void cacheImageToFile(String url, Bitmap bitmap){
    	cacheImageToFileSingle(url, bitmap);
    }
    
    /**
     * 缓存一个图片到本地<br>
     * 未开线程，所以最好使用 {@link #cacheImageToFile(String, Bitmap)}
     * @param url
     * @param bitmap
     */
    private void cacheImageToFileSingle(String url, Bitmap bitmap){
    	if (bitmap != null){
	    	String fileName = getUrlMd5(url);
	    	try {
				FileOutputStream outStream = new FileOutputStream(getCacheFile(fileName));
				bitmap.compress(Bitmap.CompressFormat.PNG, 80, outStream);
				outStream.flush();
				outStream.close();
				changeFileLastModifyTime(fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}    	
    }
    
    /**
     * 设置文件最好修改时间
     * @param fileName
     */
    private void changeFileLastModifyTime(String fileName){
    	File f = getCacheFile(fileName);
    	f.setLastModified(System.currentTimeMillis());
    }
    
    /**
     * 从SD卡得到图片<br>
     * 耗时操作，最好放在非UI线程里面
     * @param url
     * @return
     */
    public Bitmap getImageFromSD(String url){
    	String fileName = getUrlMd5(url);
    	File f = getCacheFile(fileName);
    	if (!f.exists()){
    		return null;
    	}
    	Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
    	return bitmap;
    }
    
    /**
     * 移除本地图片缓存<br>
     * 可以视情况，是否开线程处理
     * @param url
     */
    public void removeImageFromSD(String url){
    	removeImageFromSDSingle(url);
    }
    
    /**
     * 移除本地图片缓存<br>
     * 没有开新线程
     * @param url
     */
    private void removeImageFromSDSingle(String url){
    	String fileName = getUrlMd5(url);
    	File f = getCacheFile(fileName);
    	if (f.exists()){
    		f.delete();
    	}
    	f = null;
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;
        private int defaultPic;

        public BitmapDownloaderTask(ImageView imageView, int defaultPic) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.defaultPic = defaultPic;
        }
        
        public BitmapDownloaderTask(ImageView imageView){
        	this(imageView, NO_DEFAULT_PIC);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            return downloadBitmap(url);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            addBitmapToCache(url, bitmap);

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                // Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
                if ((this == bitmapDownloaderTask) || (mode != Mode.CORRECT)) {
                	if (bitmap == null){
                		//可能下载出错
                		if (defaultPic != NO_DEFAULT_PIC)
	                		imageView.setImageResource(defaultPic);
                	}else{
	                    imageView.setImageBitmap(bitmap);
                	}
                    //将存储改为tag后，下载并设置完图片后，清空tag
                    imageView.setTag(null);
                }
            }
        }
    }


    /**
     * A fake Drawable that will be attached to the imageView while the download is in progress.
     *
     * <p>Contains a reference to the actual download task, so that a download task can be stopped
     * if a new binding is required, and makes sure that only the last started download process can
     * bind its result, independently of the download finish order.</p>
     */
    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;
        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
            super(Color.BLACK);
            bitmapDownloaderTaskReference =
                new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }
        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        clearCache();
    }

    
    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */
    
    private static final int HARD_CACHE_CAPACITY = 10;
    private static final int DELAY_BEFORE_PURGE = 15 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    @SuppressWarnings("serial")
	private final HashMap<String, Bitmap> sHardBitmapCache =
        new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
        new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
            Log.i(LOG_TAG, "clear memory cache");
        }
    };

    /**
     * Adds this bitmap to the cache.
     * @param bitmap The newly downloaded bitmap.
     */
    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, bitmap);
            }
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url) {
        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }

        return null;
    }
    
    /**
     * remove url content from cache 
     * @param url
     */
    public void removeFromCache(String url){
    	sHardBitmapCache.remove(url);
    	sSoftBitmapCache.remove(url);
    	removeImageFromSD(url);
    }
 
    /**
     * Clears the image cache used internally to improve performance. Note that for memory
     * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
    	if (AUTO_CLEAR){
	        purgeHandler.removeCallbacks(purger);
	        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    	}
    }
}
