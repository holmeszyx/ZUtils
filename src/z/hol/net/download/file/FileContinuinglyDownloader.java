package z.hol.net.download.file;

import z.hol.model.SimpleFile;
import z.hol.net.download.AbsDownloadManager.Task;
import z.hol.net.download.ContinuinglyDownloader;

public class FileContinuinglyDownloader extends ContinuinglyDownloader{
	
	private DownloadListener mListener;
	private SimpleFile mFile;
	private FileStatusSaver mStatusSaver;
	
	public FileContinuinglyDownloader(SimpleFile app,String saveFile, long startPos, FileStatusSaver saver, DownloadListener listener){
		super(app.getUrl(), app.getSize(), startPos, 0, saveFile);
		mFile = app;
		mListener = listener;
		mStatusSaver = saver;
		if (mStatusSaver == null){
			//mFileService = new FileService();
			throw new IllegalArgumentException("file saver is null, I can not save download state.");
		}
	}
	
	public FileContinuinglyDownloader(String url, long blockSize,
			long startPos, int threadIndex, String filePath) {
		super(url, blockSize, startPos, threadIndex, filePath);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 获取下载ID<br>
	 * 一般是TaskID
	 * @return
	 */
	public long getDownloadId(){
		return mFile.getId();
	}
	
	@Override
	public void onRedirect(String originUrl, String newUrl) {
		// TODO Auto-generated method stub
		super.onRedirect(originUrl, newUrl);
		if (isSaveRedirectUrl()){
		    mStatusSaver.changUrl(getDownloadId(), newUrl);
		}
	}

	@Override
	protected boolean isAleadyComplete(long startPos, long remain,
			long blockSize) {
		// TODO Auto-generated method stub
		//return super.isAleadyComplete(startPos, remain, blockSize);
		if (startPos == blockSize){
			return true;
		}
		return false;
	}
	
	@Override
	protected void onPerpareFileSizeDone(long total) {
		// TODO Auto-generated method stub
		super.onPerpareFileSizeDone(total);
		mStatusSaver.updateTaskSize(getDownloadId(), total);
	}
	
	@Override
	protected void onPrepare() {
		// TODO Auto-generated method stub
		super.onPrepare();
		mStatusSaver.changeTaskState(getDownloadId(), Task.STATE_PERPARE);
		if (mListener != null){
			mListener.onPrepare(getDownloadId());
		}
	}

	@Override
	protected void onStart(long startPos, long remain, long blockSize) {
		// TODO Auto-generated method stub
		super.onStart(startPos, remain, blockSize);
		mStatusSaver.changeTaskState(getDownloadId(), Task.STATE_RUNNING);
		if (mListener != null){
			mListener.onStart(getDownloadId(), blockSize, startPos);
		}
	}

	@Override
	protected void saveBreakpoint(long startPos, long remain, long blockSize) {
		// TODO Auto-generated method stub
		super.saveBreakpoint(startPos, remain, blockSize);
		mStatusSaver.updateDownloadPos(getDownloadId(), startPos);
		if (mListener != null){
			mListener.onProgress(getDownloadId(), blockSize, startPos);
		}
	}

	@Override
	protected void onBlockComplete() {
		// TODO Auto-generated method stub
		super.onBlockComplete();
		mStatusSaver.beginTransaction();
		try {
			mStatusSaver.changeTaskState(getDownloadId(), Task.STATE_COMPLETE);
			mStatusSaver.changeTaskDoneTime(getDownloadId(), System.currentTimeMillis());
			mStatusSaver.setTransactionSuccessful();
		} finally {
			// This is Auto-generated catch block
			mStatusSaver.endTransaction();
		}
		if (mListener != null){
			mListener.onComplete(getDownloadId());
		}
	}

	@Override
	protected void onDownloadError(int errorCode) {
		// TODO Auto-generated method stub
		super.onDownloadError(errorCode);
		mStatusSaver.changeTaskState(getDownloadId(), Task.STATE_PAUSE);
		if (mListener != null){
			mListener.onError(getDownloadId(), errorCode);
		}
	}

	@Override
	protected void onCancel() {
		// TODO Auto-generated method stub
		super.onCancel();
		mStatusSaver.changeTaskState(getDownloadId(), Task.STATE_PAUSE);
		if (mListener != null){
			mListener.onCancel(getDownloadId());
		}
	}

}
