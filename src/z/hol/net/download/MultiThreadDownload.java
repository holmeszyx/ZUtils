package z.hol.net.download;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import z.hol.net.download.exception.HttpGetUrlLengthException;

public class MultiThreadDownload {
	public static final int DEFAULT_THREAD_COUNT = 3;

	private static int sGetMethodLengthUsage = 0;

	private int mThreadCount;
	private String mUrl;
	private String fileName;
	
	
	public MultiThreadDownload(String url, int threadCount, String savedFile){
		mUrl = url;
		mThreadCount = threadCount;
		this.fileName = savedFile;
	}
	
	public MultiThreadDownload(String url, int threadCount){
		this(url, threadCount, null);
	}
	
	public MultiThreadDownload(String url){
		this(url, DEFAULT_THREAD_COUNT);
	}
	
	public void startDownload(){
		long[] blocks = new long[mThreadCount];
		long length = -1;
		try {
			if (fileName == null || fileName.trim().length() == 0){
				fileName = mUrl;
				scaleFileName();
			}
			length = getUrlContentLength(mUrl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpGetUrlLengthException e) {
			// This is Auto-generated catch block
			e.printStackTrace();
		}
		if (length == -1){
			return;
		}
		computeBlock(length, blocks, mThreadCount);
		File saveFile = new File(fileName);
		if (!saveFile.exists()){
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		CountDownLatch countDownLatch = new CountDownLatch(3);
		for (int i = 0; i < mThreadCount; i ++){
			long normalBlock = blocks[0];
			long startPos = normalBlock * i;
			System.out.println(i + " startPos is " + startPos);
			ContinuinglyDownloader fileContinuingly = new ContinuinglyDownloader(mUrl, blocks[i], startPos, i, fileName);
			fileContinuingly.setCountDown(countDownLatch);
			startThread(fileContinuingly);
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("文件下载完成");
		
	}
	
	private void startThread(Runnable run){
		new Thread(run).start();
	}
	
	
	public static long getUrlContentLength(String fileUrl) throws IOException, HttpGetUrlLengthException{
		return getUrlContentLength(fileUrl, null);
	}

	public static long getUrlContentLength(String fileUrl, OnRedirectListener listener) throws IOException, HttpGetUrlLengthException{
		long length = -1;
		if (sGetMethodLengthUsage > 2){
		    length = getUrlContentLength(fileUrl, false, listener);
		}else{
			boolean isThrowGetlengthException = false;
			HttpGetUrlLengthException getLengthExcep = null;
		    try {
				length = getUrlContentLength(fileUrl, true, listener);
			} catch (HttpGetUrlLengthException e) {
				// This is Auto-generated catch block
				isThrowGetlengthException = true;
				getLengthExcep = e;
			}
		    if (length == -1){
		        try {
					length = getUrlContentLength(fileUrl, false, listener);
					isThrowGetlengthException = false;	// 如果Get方法, 有效, 则不用再抛异常
				} catch (HttpGetUrlLengthException e) {
					// This is Auto-generated catch block
					isThrowGetlengthException = true;
					getLengthExcep = e;
				}
		        sGetMethodLengthUsage ++;
		    }
		    
		    if (isThrowGetlengthException){
		    	throw getLengthExcep;
		    }
		}
		return length;
	}
	
	/**
	 * 获取一个文件的大小<br>
	 * 可以选Http方法HEAD或GET
	 * 
	 * @param fileUrl
	 * @param methodHead 是否使用HEAD方法，否则为GET
	 * @throws HttpGetUrlLengthException 
	 */
	public static long getUrlContentLength(String fileUrl, boolean methodHead, OnRedirectListener listener) throws IOException, HttpGetUrlLengthException{
		long length = -1;
		URL url = new URL(fileUrl);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (methodHead){
			conn.setRequestMethod("HEAD");
		}else{
			conn.setRequestMethod("GET");
		}
		conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
		conn.setRequestProperty("Referer", ContinuinglyDownloader.getReferer(url));
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.168 Safari/535.19");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setConnectTimeout(15000);
		conn.setReadTimeout(12000);
		conn.connect();
		
		boolean isThrowsHttpGetException = false;	// 是否抛服务器异常
		int statusCode = conn.getResponseCode();
		if (statusCode == 200){
			length = conn.getContentLength();
		}else{
			// 服务器异常
			System.out.println("http status code is " + statusCode);
			isThrowsHttpGetException = true;
		}

		String newUrl = conn.getURL().toString();
		if (!newUrl.equals(url.toString())){
			// 有重定向
			if (listener != null){
				listener.onRedirect(url.toString(), newUrl);
			}
		}
		conn.disconnect();
		
		if (isThrowsHttpGetException){
			throw new HttpGetUrlLengthException(statusCode, "http status code is " + statusCode);
		}
		return length;
	}
	
	private void scaleFileName(){
		int pos = fileName.lastIndexOf("/");
		String name = fileName.substring(pos + 1);
		fileName = name;
	}
	
	public static void computeBlock(long total, long[] blocks, int blockCount){
		if (blockCount != blocks.length){
			throw new IllegalArgumentException("Block count not equle blocks length.");
		}
		for (int i = 0; i < blockCount; i ++){
			blocks[i] = total / blockCount;
			if (i == blockCount - 1){
				blocks[i] += total % blockCount;
			}
		}
	}
	
	/**
	 * 跳转监听
	 * @author holmes
	 *
	 */
	public static interface OnRedirectListener{
		
		/**
		 * 跳转
		 * @param originUrl 原始Url
		 * @param newUrl	跳转后的新Url
		 */
		public void onRedirect(String originUrl, String newUrl);
	}
}
