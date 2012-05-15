package z.hol.general;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 原子同步取消器
 * @author holmes
 *
 */
public class ConcurrentCanceler {
	
	private AtomicBoolean mCanceler;
	
	public ConcurrentCanceler(){
		mCanceler = new AtomicBoolean(false);
	}
	
	/**
	 * 取消
	 */
	public void cancel(){
		mCanceler.set(true);
	}
	
	/**
	 * 重置取消器
	 */
	public void restore(){
		mCanceler.set(false);
	}
	
	/**
	 * 是否已取消
	 * @return
	 */
	public boolean isCanceled(){
		return mCanceler.get();
	}
}
