package z.hol.utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * WeakReference 的缓存
 * @author holmes
 *
 * @param <Key>
 * @param <Value>
 */
public class WeakCache <Key, Value>{

	private HashMap<Key, WeakReference<Value>> mCache;
	
	public WeakCache(){
		mCache = new HashMap<Key, WeakReference<Value>>();
	}
	
	/**
	 * 加入缓存
	 * @param k
	 * @param v
	 */
	public void put(Key k, Value v){
		if (v != null){
			mCache.put(k, new WeakReference<Value>(v));
		}
	}
	
	/**
	 * 获取缓存
	 * @param k
	 * @return
	 */
	public Value get(Key k){
		WeakReference<Value> wr = mCache.get(k);
		if (wr != null){
			Value v = wr.get();
			if (v == null){
				mCache.remove(k);
			}
			return v;
		}else{
			return null;
		}
	}
	
	/**
	 * 清理
	 */
	public void clear(){
		mCache.clear();
	}
}
