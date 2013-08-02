package z.hol.net.download.file;

import java.io.File;

import z.hol.model.SimpleFile;
import z.hol.net.download.AbsDownloadManager;
import z.hol.net.download.ContinuinglyDownloader;
import z.hol.net.download.ContinuinglyDownloader.DownloadListener;
import z.hol.net.download.task.FileTask;

public class FileDownloadTask implements FileTask, DownloadListener{
	
	private long mId;

	private SimpleFile mFile;
	private Thread mThread;
	private int mState = STATE_INVALID;
	private DownloadListener mListener;
	private FileStatusSaver mStatusSaver;
	private FileContinuinglyDownloader mDownloader;
	
	private String mSavePath = null;
	private long mStartPos = 0l;
	private long mTotal = 0l;
	private boolean mIsWait = false;
	
	private boolean mIsNeedRedownload = false;	
	
	public FileDownloadTask(SimpleFile file, String savePath, long startPos, FileStatusSaver statusSaver, DownloadListener listener) {
		// TODO Auto-generated constructor stub
		mFile = file;
		mSavePath = savePath;
		mStatusSaver = statusSaver;
		mListener = listener;
		mStartPos = startPos;
		file.setFileSavePath(mSavePath);
	}
	
	public long getSubType(){
		return mFile.getType();
	}
	
	public long getAddTime(){
		return mFile.getAddTime();
	}
	
	public long getDoneTime(){
		return mFile.getDoneTime();
	}

	/**
	 * 设置下载任务状态
	 */
	void setStatus(int state){
		mState = state;
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
	public void start() {
		// TODO Auto-generated method stub
		doStart();
	}
	
	private void doStart(){
		if (mState == STATE_RUNNING || (mState == STATE_COMPLETE && !mIsNeedRedownload)){
			// 有任务正在运行，或者已完成(非重新下载)，不用再执行
			return;
		}
		if (mIsNeedRedownload){
			mIsNeedRedownload = false;
			mStartPos = 0l;
			File file = new File(mSavePath);
			if (file.exists()){
				file.delete();
			}else{
				file = new File(mSavePath + ContinuinglyDownloader.TEMP_FILE_EX_NAME);
				if (file.exists()){
					file.delete();
				}
			}
			file = null;
		}
		mState = STATE_RUNNING;
		prepareDownloader();
		mThread = new Thread(mDownloader);
		mThread.start();
	}
	
	private void prepareDownloader(){
		mDownloader = new FileContinuinglyDownloader(mFile, mSavePath, mStartPos, mStatusSaver, this);
	}
	
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (mDownloader != null){
			mDownloader.cancel();
		}
	}

	@Override
	public void goon() {
		// TODO Auto-generated method stub
		if (mState == STATE_PAUSE){
			mStatusSaver.getDownloadTask(getTaskId(), this);
			doStart();
		}	
	}

	@Override
	public void redownload() {
		// TODO Auto-generated method stub
		mIsNeedRedownload = true;
		cancel();
		restoreParamsForRedownload();
	}
	
	/**
	 * 重置参数
	 */
	private synchronized void restoreParamsForRedownload(){
		if (mState == STATE_PAUSE || mState == STATE_COMPLETE){
			// 非运行状态
			mStartPos = 0l;
			mDownloader = null;
		}
	}
	
	@Override
	public boolean isNeedRedownload() {
		// TODO Auto-generated method stub
		return mIsNeedRedownload;
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		if (mIsWait){
			return STATE_WAIT;
		}
		return mState;
	}

	@Override
	public long getTaskId() {
		// TODO Auto-generated method stub
		return mId;
	}
	
	/**
	 * 设置TaskId
	 * @param id
	 */
	void setTaskId(long id){
		mId = id;
		mFile.setId(mId);
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

	@Override
	public String getFileSavePath() {
		// TODO Auto-generated method stub
		return mSavePath;
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
	public void setStartPos(long startPos) {
		// TODO Auto-generated method stub
		mStartPos = startPos;
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
	
	void setSize(long size){
		mTotal = size;
	}

	public SimpleFile getSimpeFile(){
		return mFile;
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
	public void onPrepare(long id) {
		// TODO Auto-generated method stub
		mState = STATE_PERPARE;
		if (mListener != null){
			mListener.onPrepare(id);
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
		if (mIsNeedRedownload){
			// 重置数据, 并且将会在下载管理器中被重新启动
			restoreParamsForRedownload();
		}
		if (mListener != null){
			mListener.onCancel(id);
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
}
