package z.hol.net.download.app;


import z.hol.model.SimpleApp;
import z.hol.net.download.AbsDownloadManager;
import z.hol.net.download.ContinuinglyDownloader.DownloadListener;
import z.hol.net.download.task.AppTask;

public class AppDownloadTask implements AppTask, DownloadListener{
	
	private AppContinuinglyDownloader mDownloader;
	private SimpleApp mApp;
	private Thread mThread;
	private int mState = STATE_INVALID;
	private DownloadListener mListener;
	private AppStatusSaver mStatusSaver;
	
	private long mStartPos = 0l;
	private long mTotal = 0l;
	private String mSavePath = null;
	private boolean mIsWait = false;
	
	private boolean mIsNeedRedownload = false;
	
	public AppDownloadTask(SimpleApp app, String savePath, long startPos, AppStatusSaver saver, DownloadListener listener){
		mApp = app;
		mStatusSaver = saver;
		mListener = listener;
		mSavePath = savePath;
		this.mStartPos = startPos;
	}
	
	public SimpleApp getApp(){
		return mApp;
	}
	
	@Override
	public void setStartPos(long startPos){
		this.mStartPos = startPos;
	}
	
	public void setTotal(long total){
		this.mTotal = total;
	}
	
	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		if (mIsWait){
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
		return mStartPos;
	}

	@Override
	public long getTotal() {
		// TODO Auto-generated method stub
		return mTotal;
	}

	@Override
	public long getTaskId() {
		// TODO Auto-generated method stub
		return mApp.getAppId();
	}
	
	@Override
	public void waitForStart() {
		// TODO Auto-generated method stub
		mIsWait = true;
	}

	@Override
	public void notifyStart() {
		// TODO Auto-generated method stub
		mIsWait = false;
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
	public void redownload() {
		// TODO Auto-generated method stub
		mIsNeedRedownload = true;
		cancel();
		//restore();
		
		// 改为到下载管理器中去控制
		//if (mState == STATE_PAUSE || mState == STATE_COMPLETE){
		//	// 已经是非运行状态
		//	startPos = 0l;
		//	doStart();
		// }else{
		// }
	}
	
	/**
	 * 重置下载的状态, 在非运行状态才有效
	 */
	@SuppressWarnings("unused")
	private void restore(){
		if (mState == STATE_PAUSE || mState == STATE_COMPLETE){
			mStartPos = 0;
		}
	}

	@Override
	public boolean isNeedRedownload() {
		// TODO Auto-generated method stub
		return mIsNeedRedownload;
	}

	@Override
	public int getPercent() {
		// TODO Auto-generated method stub
		if (mDownloader == null){
			if (mTotal > 0){
				return AbsDownloadManager.computePercent(mTotal, mStartPos);
			}
			return 0;
		}
		return mDownloader.getBlockPercent();
	}
	
	private void doStart(){
		if (mState == STATE_RUNNING || (mState == STATE_COMPLETE && !mIsNeedRedownload)){
			// 有任务正在运行，或者已完成(非重新下载)，不用再执行
			return;
		}
		if (mIsNeedRedownload){
			mIsNeedRedownload = false;
			mStartPos = 0l;
		}
		mState = STATE_RUNNING;
		prepareDownloader();
		mThread = new Thread(mDownloader);
		mThread.start();
	}
	
	private void prepareDownloader(){
		mDownloader = new AppContinuinglyDownloader(mApp, getFileSavePath(), mStartPos, mStatusSaver, this);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (mDownloader != null){
			mDownloader.cancel();
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
		if (mIsNeedRedownload){
			redownload();
		}
	}

	@Override
	public void onProgress(long id, long total, long current) {
		// TODO Auto-generated method stub
		mStartPos = current;
		this.mTotal = total;
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
