package z.hol.general;

/**
 * 取消器
 * @author holmes
 *
 */
public class Canceler {
	private boolean mCancel;
	private byte[] lock = new byte[0];

	public Canceler(){
		mCancel = false;
	}
	
	/**
	 * 取消
	 */
	public void cancel(){
		synchronized (lock) {
			mCancel = true;
		}
	}
	
	/**
	 * 重制
	 */
	public void restore(){
		synchronized (lock) {
			mCancel = false;
		}
	}
	
	/**
	 * 是否已取消
	 * @return
	 */
	public boolean isCanceled(){
		boolean cancel;
		synchronized (lock) {
			cancel = mCancel;
		}
		return cancel;
	}
	
}
