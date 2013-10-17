package z.hol.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 限制大小的list缓存。
 * @author holmes
 *
 * @param <T> list里面的元素类型
 */
public class LimitedList <T>{
	public static final int DEFAULT_LIMIT = 10;
	private int mLimit = DEFAULT_LIMIT;
	private List<T> mList = null;
	private int mCurrentSize = 0;
	
	public LimitedList(){
		this(DEFAULT_LIMIT);
	}
	
	/**
	 * 初始化一个限制List，
	 * @param limit list的大小限制，必须大于0
	 */
	public LimitedList(int limit){
		if (limit <= 0){
			throw new IllegalArgumentException("limit must larger than 0 , but now limit is " + limit);
		}
		mLimit = limit;
		mList = new ArrayList<T>(mLimit);
	}
	
	/**
	 * 是否可以再添加
	 * @return
	 */
	public boolean canAdd(){
		return mLimit > mCurrentSize ? true : false;
	}
	
	/**
	 * 获取当前已加入的List，并且limitedList缓存会清空
	 * @return
	 */
	public List<T> getCurrentList(){
		List<T> list = mList;
		mList = new ArrayList<T>(mLimit);
		mCurrentSize = 0;
		return list;
	}

	/**
	 * 添加一个新的元素
	 * @param item
	 * @return 如果不能添加，如已到上限会返回false,添加成功会返回true
	 */
	public boolean add(T item){
		if (!canAdd())
			return false;
		boolean success = mList.add(item);
		if (success) mCurrentSize ++;
		return success;
	}
	
	/**
	 * 获取当前已缓存的List大小
	 * @return
	 */
	public int getCurrentSize(){
		return mCurrentSize;
	}
	
	/**
	 * 是否有未拿出的数据
	 * @return
	 */
	public boolean hasData(){
		return mCurrentSize > 0 ? true : false;
	}
	
	/**
	 * 清空缓存
	 */
	public void clear(){
		mList.clear();
		mCurrentSize = 0;
	}
	
}
