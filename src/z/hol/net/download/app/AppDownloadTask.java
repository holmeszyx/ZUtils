package z.hol.net.download.app;


import z.hol.model.SimpleApp;
import z.hol.net.download.AbsDownloadManager;
import z.hol.net.download.ContinuinglyDownloader.DownloadListener;
import z.hol.net.download.task.AppTask;

public class AppDownloadTask implements AppTask, DownloadListener{
	
	private AppContinuinglyDownloader downloader;
	private SimpleApp mApp;
	private Thread mThread;
	private int mState = STATE_INVALID;
	private DownloadListener mListener;
	private AppStatusSaver mStatusSaver;
	private long startPos = 0l;
	private long total = 0l;
	private String mSavePath = null;
	private boolean isWait = false;
	
	public AppDownloadTask(SimpleApp app, String savePath, long startPos, AppStatusSaver saver, DownloadListener listener){
		mApp = app;
		mStatusSaver = saver;
		mListener = listener;
		mSavePath = savePath;
		this.startPos = startPos;
	}
	
	public SimpleApp getApp(){
		return mApp;
	}
	
	@Override
	public void setStartPos(long startPos){
		this.startPos = startPos;
	}
	
	public void setTotal(long total){
		this.total = total;
	}
	
	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		if (isWait){
			return STATE_WAIT;
		}
		return mState;
	}
	
	/**
	 * 设置下载任务状态
	 */
	void setStatus(int state){
		mState = state;
	}
	
	@Override
	public String setFileSavePath(String filePath) {
		// TODO Auto-generated method stub
		if (mSavePath == null){
			mSavePath = filePath;
		}
		return mSavePath;
	}

	@Override
	public String getFileSavePath() {
		// TODO Auto-generated method stub
		return mSavePath;
	}
	
	@Override
	public long getCurrentPos() {
		// TODO Auto-generated method stub
		return startPos;
	}

	@Override
	public long getTotal() {
		// TODO Auto-generated method stub
		return total;
	}

	@Override
	public long getTaskId() {
		// TODO Auto-generated method stub
		return mApp.getAppId();
	}
	
	@Override
	public void waitForStart() {
		// TODO Auto-generated method stub
		isWait = true;
	}

	@Override
	public void notifyStart() {
		// TODO Auto-generated method stub
		isWait = false;
	}

	@Override
	public void goon() {
		// TODO Auto-generated method stub
		if (mState == STATE_PAUSE){
			mStatusSaver.getAppTask(getTaskId(), this);
			doStart();
		}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		doStart();
	}
	
	@Override
	public int getPercent() {
		// TODO Auto-generated method stub
		if (downloader == null){
			if (total > 0){
				return AbsDownloadManager.computePercent(total, startPos);
			}
			return 0;
		}
		return downloader.getBlockPercent();
	}
	
	private void doStart(){
		if (mState == STATE_RUNNING || mState == STATE_COMPLETE){
			// 有任务正在运行，或者已完成，不用再执行
			return;
		}
		mState = STATE_RUNNING;
		prepareDownloader();
		mThread = new Thread(downloader);
		mThread.start();
	}
	
	private void prepareDownloader(){
		downloader = new AppContinuinglyDownloader(mApp, getFileSavePath(), startPos, mStatusSaver, this);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (downloader != null){
			downloader.cancel();
		}
	}

	@Override
	public void onComplete(long id) {
		// TODO Auto-generated method stub
		mState = STATE_COMPLETE;
		if (mListener != null){
			mListener.onComplete(id);
		}
	}

	@Override
	public void onStart(long id, long total, long current) {
		// TODO Auto-generated method stub
		mState = STATE_RUNNING;
		if (mListener != null){
			mListener.onStart(id, total, current);
		}
	}

	@Override
	public void onError(long id, int errorCode) {
		// TODO Auto-generated method stub
		mState = STATE_PAUSE;
		if (mListener != null){
			mListener.onError(id, errorCode);
		}
	}

	@Override
	public void onCancel(long id) {
		// TODO Auto-generated method stub
		mState = STATE_PAUSE;
		if (mListener != null){
			mListener.onCancel(id);
		}
	}

	@Override
	public void onProgress(long id, long total, long current) {
		// TODO Auto-generated method stub
		startPos = current;
		this.total = total;
		if (mListener != null){
			mListener.onProgress(id, total, current);
		}
	}

	@Override
	public void onPrepare(long id) {
		// TODO Auto-generated method stub
		mState = STATE_PERPARE;
		if (mListener != null){
			mListener.onPrepare(id);
		}
	}

}
