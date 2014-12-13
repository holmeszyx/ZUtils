/*
 * Copyright (C) 2012 The Android Open Source Project
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

package z.hol.utils.bitmapfun;

import z.hol.utils.LruCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

/**
 * 只保留内存缓存
 */
public class ImageCache {
    //private static final String TAG = "ImageCache";

    // Default memory cache size
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 2; // 5MB


    // Compression settings when writing images to disk cache
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;

    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;

    private LruCache<String, Bitmap> mMemoryCache;

    /**
     * Creating a new ImageCache object using the specified parameters.
     *
     * @param context The context to use
     * @param cacheParams The cache parameters to use to initialize the cache
     */
    public ImageCache(Context context, ImageCacheParams cacheParams) {
        init(context, cacheParams);
    }

    /**
     * Creating a new ImageCache object using the default parameters.
     *
     * @param context The context to use
     * @param uniqueName A unique name that will be appended to the cache directory
     */
    public ImageCache(Context context, String uniqueName) {
        init(context, new ImageCacheParams(uniqueName));
    }


    /**
     * Initialize the cache, providing all parameters.
     *
     * @param context The context to use
     * @param cacheParams The cache parameters to initialize the cache
     */
    private void init(Context context, ImageCacheParams cacheParams) {
       // if (BuildConfig.DEBUG){
        //	Log.d(TAG, "disk cache is " + diskCacheDir.getAbsolutePath());
        //}

        // Set up memory cache
        if (cacheParams.memoryCacheEnabled) {
            mMemoryCache = new LruCache<String, Bitmap>(cacheParams.memCacheSize) {
                /**
                 * Measure item size in bytes rather than units which is more practical for a bitmap
                 * cache
                 */
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return Utils.getBitmapSize(bitmap);
                }
            };
        }
    }

    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null && mMemoryCache.get(data) == null) {
            mMemoryCache.put(data, bitmap);
        }

    }

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        if (mMemoryCache != null) {
            final Bitmap memBitmap = mMemoryCache.get(data);
            if (memBitmap != null) {
                //if (BuildConfig.DEBUG) {
                //    Log.d(TAG, "Memory cache hit");
                //}
                return memBitmap;
            }
        }
        return null;
    }

    public void clearCaches() {
        mMemoryCache.evictAll();
    }

    /**
     * A holder class that contains cache parameters.
     */
    public static class ImageCacheParams {
        public String uniqueName;
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;

        public ImageCacheParams(String uniqueName) {
            this.uniqueName = uniqueName;
        }
    }
}
