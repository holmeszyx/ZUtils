package z.hol.net.download;

/**
 * 带回调的断点下载器
 * @author holmes
 *
 */
public abstract class CallbackContinuinglyDownloader extends ContinuinglyDownloader{
	
	private DownloadListener mDownloadListener;

	public CallbackContinuinglyDownloader(String url, long blockSize,
			long startPos, int threadIndex, String filePath, DownloadListener listener) {
		super(url, blockSize, startPos, threadIndex, filePath);
		mDownloadListener = listener;
	}
	
	/**
	 * 获取下载ID<br>
	 * 一般是Task id，主要用于回调参数
	 * @return
	 */
	public abstract long getDownloadId();

	@Override
	protected boolean isAleadyComplete(long startPos, long remain,
			long blockSize) {
		// TODO Auto-generated method stub
		if (startPos == blockSize){
			return true;
		}else{
			return false;
		}
	}

	@Override
	protected void saveBreakpoint(long startPos, long remain, long blockSize) {
		// TODO Auto-generated method stub
		//super.saveBreakpoint(startPos, remain, blockSize);
		if (mDownloadListener != null){
			mDownloadListener.onProgress(getDownloadId(), blockSize, startPos);
		}
	}

	@Override
	protected void onBlockComplete() {
		// TODO Auto-generated method stub
		super.onBlockComplete();
		if (mDownloadListener != null){
			mDownloadListener.onComplete(getDownloadId());
		}
	}

	@Override
	protected void onDownloadError(int errorCode) {
		// TODO Auto-generated method stub
		super.onDownloadError(errorCode);
		if (mDownloadListener != null){
			mDownloadListener.onError(getDownloadId(), errorCode);
		}
	}

	@Override
	protected void onCancel() {
		// TODO Auto-generated method stub
		super.onCancel();
		if (mDownloadListener != null){
			mDownloadListener.onCancel(getDownloadId());
		}
	}

	@Override
	protected void onStart(long startPos, long remain, long blockSize) {
		// TODO Auto-generated method stub
		super.onStart(startPos, remain, blockSize);
		if (mDownloadListener != null){
			mDownloadListener.onStart(getDownloadId(), blockSize, startPos);
		}
	}

	@Override
	protected void onPrepare() {
		// TODO Auto-generated method stub
		super.onPrepare();
		if (mDownloadListener != null){
			mDownloadListener.onPrepare(getDownloadId());
		}
	}

}
