package z.hol.net.download;

import z.hol.model.SimpleApp;
import z.hol.net.download.AbsDownloadManager.Task;

public class FileContinuinglyDownloader extends ContinuinglyDownloader{
	
	private DownloadListener mListener;
	private SimpleApp mApp;
	private AppStatusSaver mStatusSaver;
	
	public FileContinuinglyDownloader(SimpleApp app,String saveFile, long startPos, AppStatusSaver saver, DownloadListener listener){
		super(app.getAppUrl(), app.getSize(), startPos, 0, saveFile);
		mApp = app;
		mListener = listener;
		mStatusSaver = saver;
		if (mStatusSaver == null){
			//mFileService = new FileService();
			throw new IllegalArgumentException("file service is null, I can not save download state.");
		}
	}
	
	public FileContinuinglyDownloader(String url, long blockSize,
			long startPos, int threadIndex, String filePath) {
		super(url, blockSize, startPos, threadIndex, filePath);
		// TODO Auto-generated constructor stub
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
		mStatusSaver.updateAppSize(mApp.getAppId(), total);
	}
	
	@Override
	protected void onPrepare() {
		// TODO Auto-generated method stub
		super.onPrepare();
		mStatusSaver.changeAppTaskState(mApp.getAppId(), Task.STATE_PERPARE);
		if (mListener != null){
			mListener.onPrepare(mApp.getAppId());
		}
	}

	@Override
	protected void onStart(long startPos, long remain, long blockSize) {
		// TODO Auto-generated method stub
		super.onStart(startPos, remain, blockSize);
		mStatusSaver.changeAppTaskState(mApp.getAppId(), Task.STATE_RUNNING);
		if (mListener != null){
			mListener.onStart(mApp.getAppId(), blockSize, startPos);
		}
	}

	@Override
	protected void saveBreakpoint(long startPos, long remain, long blockSize) {
		// TODO Auto-generated method stub
		super.saveBreakpoint(startPos, remain, blockSize);
		mStatusSaver.updateAppDownloadPos(mApp.getAppId(), startPos);
		if (mListener != null){
			mListener.onProgress(mApp.getAppId(), blockSize, startPos);
		}
	}

	@Override
	protected void onBlockComplete() {
		// TODO Auto-generated method stub
		super.onBlockComplete();
		mStatusSaver.changeAppTaskState(mApp.getAppId(), Task.STATE_COMPLETE);
		if (mListener != null){
			mListener.onComplete(mApp.getAppId());
		}
	}

	@Override
	protected void onDownloadError(int errorCode) {
		// TODO Auto-generated method stub
		super.onDownloadError(errorCode);
		mStatusSaver.changeAppTaskState(mApp.getAppId(), Task.STATE_PAUSE);
		if (mListener != null){
			mListener.onError(mApp.getAppId(), errorCode);
		}
	}

	@Override
	protected void onCancel() {
		// TODO Auto-generated method stub
		super.onCancel();
		mStatusSaver.changeAppTaskState(mApp.getAppId(), Task.STATE_PAUSE);
		if (mListener != null){
			mListener.onCancel(mApp.getAppId());
		}
	}

	

}
