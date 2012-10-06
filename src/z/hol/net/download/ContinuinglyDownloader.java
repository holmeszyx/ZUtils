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


/**
 * 文件断点续传下载器.<br>
 * 子类可以重写{@link #saveBreakpoint(long, long, long)}来保存断点信息.<br>
 * 重写{@link #onBlockComplete()}, {@link #onDownloadError(int)}来监听下载状态.
 * @author holmes
 *
 */
public class ContinuinglyDownloader implements Runnable, OnRedirectListener{
	public static final String TEMP_FILE_EX_NAME = ".zdt";
	public static final int ERROR_CODE_SDCARD_NO_FOUND = 10404;
	public static final int MAX_REAPEAT_TIMES = 3;
	public static final int MAX_TRY_AGAIN_TIMES = 5;
	
	private boolean useTempFile = true;
	private boolean autoTryAgain = true;
	private int mMaxTryAgainTimes = MAX_TRY_AGAIN_TIMES;
	private int mAlreadyTryTimes = 0;
	private long blockSize;
	private long startPos;
	// private long endPos;
	private RandomAccessFile file;
	private String filePath;
	private String url;
	private long maxRemain;
	private int mThreadIndex;
	private CountDownLatch mCountDownLatch;
	private ConcurrentCanceler mCanceler;
	private int mErrorTimes = 0;
	private boolean mIsBlockComplete = false;
	
	public ContinuinglyDownloader(String url, long blockSize, long startPos, int threadIndex, String filePath){
		this.url = url;
		this.filePath = filePath;
		mThreadIndex = threadIndex;
		initParams(blockSize, startPos);
		System.out.println(threadIndex + " remain" + maxRemain);
		mCanceler = new ConcurrentCanceler();
	}
	
	private void initParams(long blockSize, long startPos){
		this.startPos = startPos;
		this.blockSize = blockSize;
		maxRemain = this.blockSize * (mThreadIndex + 1) - startPos;
	}
	
	public void useTempFile(boolean use){
		useTempFile = use;
	}
	
	public void setCountDown(CountDownLatch countDownLatch){
		mCountDownLatch = countDownLatch;
	}
	
	public String getSaveFilePath(){
		return filePath;
	}
	
	/**
	 * 初始化文件
	 * @throws DowloadException
	 */
	private void initSavaFile() throws DowloadException{
		File saveFile = new File(filePath);
		File path = saveFile.getParentFile();
		if (!path.exists()){
			path.mkdirs();
		}
		String realSaveFile = filePath;
		if (useTempFile){
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
			file = new RandomAccessFile(realSaveFile, "rw");
			file.seek(startPos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new DowloadException("save file " + filePath + " no found", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 当下载文件大小未知时，先获取文件大小
	 */
	private boolean prepareFileSize(){
		if (startPos <= 0){
			try {
				blockSize = MultiThreadDownload.getUrlContentLength(url, this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			if (blockSize != -1){
				initParams(blockSize, 0);
				onPerpareFileSizeDone(blockSize);
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
		url = newUrl;
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
		if (blockSize <= 0){
			return 0;
		}
		long current = blockSize - maxRemain;
		if (current < 0){
			current = 0;
		}
		return AbsDownloadManager.computePercent(blockSize, current);
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
				onDownloadError(404);
				return;
			}
			try {
				System.out.println("get file size error. " + url);
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startDownload();
			return;
		}
		
		mErrorTimes = 0;
		
		// System.out.println("init file");
		try {
			initSavaFile();
		} catch (DowloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("start download");
		mIsBlockComplete = false;
		onStart(startPos, maxRemain, blockSize);
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
			if (isAleadyComplete(startPos, maxRemain, blockSize)){
				restoreTryTimes();
				mIsBlockComplete = true;
				return;
			}
			if (file == null){
				onDownloadError(ERROR_CODE_SDCARD_NO_FOUND);
				return;
			}
			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
			fillHttpHeader(conn);
			if (conn.getResponseCode() == 206){
				in = conn.getInputStream();
				saveFile(in);
				if (!isCanceled()){
					mIsBlockComplete = true;
					// onBlockComplete();
				}
			}else{
				System.out.println(mThreadIndex + " http status code is " + conn.getResponseCode());
				restoreTryTimes();
				onDownloadError(conn.getResponseCode());
			}
			conn.disconnect();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			invokeTryAgainError(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			invokeTryAgainError(0);
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
					if (file != null){
						// the file will be null
						// when sdcard no found
						// 当没有sd卡时，file可能为null
						file.close();
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
			System.out.println("try reconnect for download," + url);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			doDownload();
		}
	}
	
	/**
	 * 当下载完成后，恢复临时文件
	 */
	private void restoreTempFile(){
		if (useTempFile){
			File realSaveFile = new File(filePath + TEMP_FILE_EX_NAME);
			File originFile = new File(filePath);
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
	 * 是否要重连
	 * @return
	 */
	private boolean isNeedTryAgain(){
		return (mAlreadyTryTimes > 0) ? true : false;
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
		if (autoTryAgain){
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
		CC cc = new CC();
		byte[] buff = new byte[512];
		int readLen = getExpectedReadLen();
		int len = 0;
		cc.start();
		while ((len = in.read(buff, 0, readLen)) != -1){
			restoreTryTimes();
			file.write(buff, 0, len);
			maxRemain -= len;
			startPos += len;
			readLen = getExpectedReadLen();
			cc.end();
			if (cc.cost() > CC.SECEND){
				// 大于1秒
				saveBreakpoint(startPos, maxRemain, blockSize);
				cc.start();
			}
			if (isCanceled()){
				break;
			}
			if (readLen == 0){
				break;
			}
		}
		//块下载完成
		saveBreakpoint(startPos, maxRemain, blockSize);
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
		return (maxRemain > 512) ? 512 : (int) maxRemain;
	}
	
	/**
	 * 填充 HTTP 头
	 */
	private void fillHttpHeader(HttpURLConnection conn) throws ProtocolException{
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
		conn.setRequestProperty("Referer", getReferer(url));
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Range", "bytes=" + startPos + "-");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.168 Safari/535.19");
		conn.setRequestProperty("Connection", "Keep-Alive");
		//conn.setConnectTimeout(30000);
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
