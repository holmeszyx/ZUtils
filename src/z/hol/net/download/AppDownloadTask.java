package z.hol.net.download;


import z.hol.model.SimpleApp;
import z.hol.net.download.AbsDownloadManager.Task;
import z.hol.net.download.ContinuinglyDownloader.DownloadListener;

public class AppDownloadTask implements Task, DownloadListener{
	
	private FileContinuinglyDownloader downloader;
	private SimpleApp mApp;
	private Thread mThread;
	private int mState = STATE_PERPARE;
	private DownloadListener mListener;
	private AppStatusSaver mStatusSaver;
	private long startPos;
	
	public AppDownloadTask(SimpleApp app, long startPos, AppStatusSaver saver, DownloadListener listener){
		mApp = app;
		mStatusSaver = saver;
		mListener = listener;
		this.startPos = startPos;
	}
	
	public SimpleApp getApp(){
		return mApp;
	}
	
	public void setStartPos(long startPos){
		this.startPos = startPos;
	}
	
	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return mState;
	}

	@Override
	public long getTaskId() {
		// TODO Auto-generated method stub
		return mApp.getAppId();
	}

	@Override
	public void goon() {
		// TODO Auto-generated method stub
		if (mState == STATE_PAUSE){
			mStatusSaver.getAppTask(getTaskId(), this);
			doStart();
			mState = STATE_RUNNING;
		}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		mStatusSaver.addAppDownload(mApp);
		doStart();
		mState = STATE_RUNNING;
	}
	
	@Override
	public int getPercent() {
		// TODO Auto-generated method stub
		if (downloader == null){
			return 0;
		}
		return downloader.getBlockPercent();
	}
	
	private void doStart(){
		if (mState == STATE_RUNNING || mState == STATE_COMPLETE){
			// 有任务正在运行，或者已完成，不用再执行
			return;
		}
		prepareDownlader();
		mThread = new Thread(downloader);
		mThread.start();
	}
	
	private void prepareDownlader(){
		downloader = new FileContinuinglyDownloader(mApp, startPos, mStatusSaver, this);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		downloader.cancel();
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
		if (mListener != null){
			mListener.onProgress(id, total, current);
		}
	}

}
