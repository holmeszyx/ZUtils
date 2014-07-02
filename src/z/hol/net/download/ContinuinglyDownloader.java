package z.hol.net.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import z.hol.general.CC;
import z.hol.general.ConcurrentCanceler;
import z.hol.net.download.MultiThreadDownload.OnRedirectListener;
import z.hol.net.download.exception.HttpGetUrlLengthException;


/**
 * 文件断点续传下载器.<br>
 * 子类可以重写{@link #saveBreakpoint(long, long, long)}来保存断点信息.<br>
 * 重写{@link #onBlockComplete()}, {@link #onDownloadError(int)}来监听下载状态.
 * @author holmes
 *
 */
public class ContinuinglyDownloader implements Runnable, OnRedirectListener{
	/** 暂时文件后缀 */
	public static final String TEMP_FILE_EX_NAME = ".zdt";

	/** SD卡没有 */
	public static final int ERROR_CODE_SDCARD_NO_FOUND = 10404;
	/** 下载的URL不正确 */
	public static final int ERROR_CODE_DOWNLOAD_URL_INCORRECT = 10804;
	/** 下载出错 */
	public static final int ERROR_CODE_DOWNLOAD_ERROR = 10805;
	/** 获取文件大小的服务状态码基础偏移 */
	public static final int ERROR_CDOE_BASE_GET_FILE_SIZE_HTTP_OFFSET = 20000;
	/** 获取文件大小的非服务器状态异常(超时等) */
	public static final int ERROR_CODE_GET_FILE_SIZE_EXCEPTION = 10500;
	
	public static final int MAX_REAPEAT_TIMES = 3;
	public static final int MAX_TRY_AGAIN_TIMES = 5;
	
	/** 是否使用临时文件后缀名 {@link #TEMP_FILE_EX_NAME} */
	private boolean mUseTempFile = true;
	/** 是否自动网络异常重试 */
	private boolean mAutoTryAgain = true;
	private int mMaxTryAgainTimes = MAX_TRY_AGAIN_TIMES;
	/** 下载异常后尝试次数(网络异常) */
	private int mAlreadyTryTimes = 0;
	/** 当前下载的数据块总大小 */
	private long mBlockSize;
	/** 当前下载的数据块起始位置 */
	private long mStartPos;
	// private long endPos;

	/** 保存文件的回调间隔计时器 */
	private CC mSaveCC = new CC();
	private RandomAccessFile mFile;
	private String mFilePath;
	private String mUrl;
	/** 剩余需要下载的数据大小 */
	private long mMaxRemain;
	private int mThreadIndex;
	
	private CountDownLatch mCountDownLatch;
	private ConcurrentCanceler mCanceler;
	/** 获取文件大小出错次数 */
	private int mErrorTimes = 0;
	/** 
	 * 获取文件大小出错的状态码 
	 * (大于 {@value #ERROR_CDOE_BASE_GET_FILE_SIZE_HTTP_OFFSET} 的状态码,
	 * 就为服务器返回的错误)
	 */
	private int mGetSizeErrorCode = ERROR_CODE_GET_FILE_SIZE_EXCEPTION;
	private boolean mIsBlockComplete = false;
	
	public ContinuinglyDownloader(String url, long blockSize, long startPos, int threadIndex, String filePath){
		this.mUrl = url;
		this.mFilePath = filePath;
		mThreadIndex = threadIndex;
		initParams(blockSize, startPos);
		System.out.println(threadIndex + " remain " + mMaxRemain);
		mCanceler = new ConcurrentCanceler();
	}
	
	private void initParams(long blockSize, long startPos){
		this.mStartPos = startPos;
		this.mBlockSize = blockSize;
		mMaxRemain = this.mBlockSize * (mThreadIndex + 1) - startPos;
	}
	
	public void useTempFile(boolean use){
		mUseTempFile = use;
	}
	
	public void setCountDown(CountDownLatch countDownLatch){
		mCountDownLatch = countDownLatch;
	}
	
	public String getSaveFilePath(){
		return mFilePath;
	}
	
	/**
	 * 初始化文件
	 * @throws DowloadException
	 */
	private void initSavaFile() throws DowloadException{
		File saveFile = new File(mFilePath);
		File path = saveFile.getParentFile();
		if (!path.exists()){
			path.mkdirs();
		}
		String realSaveFile = mFilePath;
		if (mUseTempFile){
			realSaveFile = realSaveFile + TEMP_FILE_EX_NAME;
			File realFile = new File(realSaveFile);
			if (!realFile.exists()){
				if (saveFile.exists()){
					saveFile.renameTo(realFile);
				}
			}
			realFile = null;
		}
		path = null;
		saveFile = null;
		try {
			mFile = new RandomAccessFile(realSaveFile, "rw");
			mFile.seek(mStartPos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new DowloadException("save file " + mFilePath + " no found", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 当下载文件大小未知时，先获取文件大小
	 */
	private boolean prepareFileSize(){
		if (mStartPos <= 0){
			try {
				mBlockSize = MultiThreadDownload.getUrlContentLength(mUrl, this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (HttpGetUrlLengthException e) {
				// 服务器返回错误
				e.printStackTrace();
				mGetSizeErrorCode = ERROR_CDOE_BASE_GET_FILE_SIZE_HTTP_OFFSET + e.getHttpStatusCode();
				return false;
			}
			if (mBlockSize != -1){
				initParams(mBlockSize, 0);
				onPerpareFileSizeDone(mBlockSize);
				return true;
			}else{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void onRedirect(String originUrl, String newUrl) {
		// TODO Auto-generated method stub
		System.out.println("redi");
		mUrl = newUrl;
	}
	
	/**
	 * 需要重新获取文件大小时，并获取文件大小成功会执行
	 */
	protected void onPerpareFileSizeDone(long total){
		
	}
	
	/**
	 * 获取当前块的下载百分比
	 * @return
	 */
	public int getBlockPercent(){
		if (mBlockSize <= 0){
			return 0;
		}
		long current = mBlockSize - mMaxRemain;
		if (current < 0){
			current = 0;
		}
		return AbsDownloadManager.computePercent(mBlockSize, current);
	}
	
	/**
	 * 开始下载
	 */
	private void startDownload(){
		if (isCanceled()){
			onCancel();
			return;
		}
		
		onPrepare();
		if (!prepareFileSize()){
			mErrorTimes ++;
			if (mErrorTimes > MAX_REAPEAT_TIMES){
				onDownloadError(mGetSizeErrorCode);
				return;
			}

			System.out.println("get file size error. " + mUrl);
			
			// 延迟3秒
			for (int times = 0; times < 30; times ++){
				if (isCanceled()){
					onCancel();
					return;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// This is Auto-generated catch block
					e.printStackTrace();
				}
			}
			startDownload();
			return;
		}
		
		mErrorTimes = 0;
		mGetSizeErrorCode = ERROR_CODE_GET_FILE_SIZE_EXCEPTION;
		
		// System.out.println("init file");
		try {
			initSavaFile();
		} catch (DowloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("start download");
		mIsBlockComplete = false;
		onStart(mStartPos, mMaxRemain, mBlockSize);
		doDownload();
	}
	
	/**
	 * 文件下载
	 */
	private void doDownload(){
		InputStream in = null;
		try {
			if (isCanceled()){
				restoreTryTimes();
				onCancel();
				return;
			}
			if (isAleadyComplete(mStartPos, mMaxRemain, mBlockSize)){
				restoreTryTimes();
				mIsBlockComplete = true;
				return;
			}
			if (mFile == null){
				onDownloadError(ERROR_CODE_SDCARD_NO_FOUND);
				return;
			}
			URL httpUrl = new URL(mUrl);
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
			fillHttpHeader(conn);
			int responseCode = conn.getResponseCode();
			if (responseCode == 206 || responseCode == 200){
				// 不支持断点续传
				if (responseCode == 200){
					mAutoTryAgain = false;
					onDoNotSupportBreakpoint();
				}
				
				in = conn.getInputStream();
				saveFile(in);
				if (!isCanceled()){
					mIsBlockComplete = true;
					// onBlockComplete();
				}
			}else{
				System.out.println(mThreadIndex + " http status code is " + responseCode);
				restoreTryTimes();
				onDownloadError(conn.getResponseCode());
			}
			conn.disconnect();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			invokeTryAgainError(ERROR_CODE_DOWNLOAD_URL_INCORRECT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			invokeTryAgainError(ERROR_CODE_DOWNLOAD_ERROR);
		}finally{
			if (in != null){
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!isNeedTryAgain()){
				try {
					if (mFile != null){
						// the file will be null
						// when sdcard no found
						// 当没有sd卡时，file可能为null
						mFile.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mIsBlockComplete){
					restoreTempFile();
				}
				if (!isCanceled() && mIsBlockComplete){
					onBlockComplete();
				}
			}
		}
		
		if (isNeedTryAgain()){
			System.out.println("try reconnect for download," + mUrl);
			// 延迟5秒
			for (int times = 0; times < 50; times ++){
				if (isCanceled()){
					restoreTryTimes();
					onCancel();
					return;
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// This is Auto-generated catch block
					e.printStackTrace();
				}
			}
			doDownload();
		}
	}
	
	/**
	 * 当下载完成后，恢复临时文件
	 */
	private void restoreTempFile(){
		if (mUseTempFile){
			File realSaveFile = new File(mFilePath + TEMP_FILE_EX_NAME);
			File originFile = new File(mFilePath);
			if (realSaveFile.exists()){
				if (originFile.exists()){
					originFile.delete();
				}
				realSaveFile.renameTo(originFile);
			}
			realSaveFile = null;
			originFile = null;
		}
	}
	
	/**
	 * 是否要重连，必须在至少一次 {@link #invokeTryAgainError(int)}之后执行。
	 * @return
	 */
	private boolean isNeedTryAgain(){
		// return (mAlreadyTryTimes > 0) ? true : false;
		// 在 invokeTryAgainError 之后， mAlreadyTryTimes 会自增 1
		// 所以 mAlreadyTryTimes 必大于 0
		// 但为了防止无限重试，又加上小于最大连续尝试次数
		return (mAlreadyTryTimes > 0 && mAlreadyTryTimes <= mMaxTryAgainTimes);
	}
	
	/**
	 * 重置重连次数
	 */
	private void restoreTryTimes(){
		mAlreadyTryTimes = 0;
	}
	
	/**
	 * 自动重连失败
	 * @param errorCode
	 */
	private void invokeTryAgainError(int errorCode){
		if (mAutoTryAgain){
			mAlreadyTryTimes ++;
			if (mAlreadyTryTimes > mMaxTryAgainTimes){
				onDownloadError(errorCode);
			}
		}else{
			onDownloadError(errorCode);
		}
	}
	
	/**
	 * 是否已经下载完成<br>
	 * 解决有时候暂停时，已经下载完成
	 * @return
	 */
	protected boolean isAleadyComplete(long startPos, long remain, long blockSize){
		return false;
	}
	
	/**
	 * 保存下载的内容
	 */
	private void saveFile(InputStream in) throws IOException{
		byte[] buff = new byte[4096];
		int readLen = getExpectedReadLen();
		int len = 0;
		mSaveCC.start();
		while ((len = in.read(buff, 0, readLen)) != -1){
			restoreTryTimes();
			mFile.write(buff, 0, len);
			mMaxRemain -= len;
			mStartPos += len;
			readLen = getExpectedReadLen();
			mSaveCC.end();
			if (mSaveCC.cost() > CC.SECEND){
				// 大于1秒
				saveBreakpoint(mStartPos, mMaxRemain, mBlockSize);
				mSaveCC.start();
			}
			if (isCanceled()){
				break;
			}
			if (readLen == 0){
				break;
			}
		}
		//块下载完成
		saveBreakpoint(mStartPos, mMaxRemain, mBlockSize);
		if (isCanceled()){
			onCancel();
		}
		
	}
	
	/**
	 * 保存断点信息
	 * @param startPos 开始位置
	 * @param remain 剩余
	 * @param blockSize 块大小
	 */
	protected void saveBreakpoint(long startPos, long remain, long blockSize){
		
	}
	
	/**
	 * 获取期望的剩余下载量
	 */
	private int getExpectedReadLen(){
		return (mMaxRemain > 512) ? 512 : (int) mMaxRemain;
	}
	
	/**
	 * 填充 HTTP 头
	 */
	private void fillHttpHeader(HttpURLConnection conn) throws ProtocolException{
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
		conn.setRequestProperty("Referer", getReferer(mUrl));
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Range", "bytes=" + mStartPos + "-");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.168 Safari/535.19");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setConnectTimeout(40000);
		conn.setReadTimeout(30000);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		startDownload();
		System.out.println(mThreadIndex + " is end");
		if (mCountDownLatch != null){
			mCountDownLatch.countDown();
		}
	}
	
	/**
	 * 不支持断电续传
	 */
	protected void onDoNotSupportBreakpoint(){
		initParams(mBlockSize, 0);
		try {
			mFile.setLength(mMaxRemain);
			mFile.seek(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 块下载完成
	 */
	protected void onBlockComplete(){
		mIsBlockComplete = true;
	}

	/**
	 * 下载出错
	 * @param errorCode
	 */
	protected void onDownloadError(int errorCode){
		
	}
	
	/**
	 * 取消
	 */
	protected void onCancel(){
		
	}
	
	/**
	 * 开始
	 */
	protected void onStart(long startPos, long remain, long blockSize){
		
	}
	
	/**
	 * 准备下载
	 */
	protected void onPrepare(){
		
	}
	
	/**
	 * 取消
	 */
	public void cancel(){
		mCanceler.cancel();
	}
	
	/**
	 * 是否已取消
	 * @return
	 */
	public boolean isCanceled(){
		return mCanceler.isCanceled();
	}
	
	public static String getReferer(URL url){
		StringBuilder sb = new StringBuilder();
		sb.append(url.getProtocol());
		sb.append("://");
		sb.append(url.getHost());
		return sb.toString();
	}
	
	public static String getReferer(String url){
		try {
			return getReferer(new URL(url));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 下载状态回调
	 * @author holmes
	 *
	 */
	public static interface DownloadListener{
		/**
		 * 下载完成
		 * @param id
		 */
		public void onComplete(long id);
		
		/**
		 * 准备下载
		 * @param id
		 */
		public void onPrepare(long id);
		
		/**
		 * 下载开始
		 * @param id
		 * @param total
		 * @param current
		 */
		public void onStart(long id, long total, long current);
		
		/**
		 * 下载出错
		 * @param id
		 * @param errorCode
		 */
		public void onError(long id, int errorCode);
		
		/**
		 * 下载取消
		 * @param id
		 */
		public void onCancel(long id);
		
		/**
		 * 下载进行中，进度
		 * @param id
		 * @param total
		 * @param current
		 */
		public void onProgress(long id, long total, long current);
	} 	
}
