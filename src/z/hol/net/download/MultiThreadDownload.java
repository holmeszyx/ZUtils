package z.hol.net.download;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class MultiThreadDownload {
	public static final int DEFAULT_THREAD_COUNT = 3;

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

	public static long getUrlContentLength(String fileUrl) throws IOException{
		long length = -1;
		URL url = new URL(fileUrl);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
		conn.setRequestProperty("Referer", ContinuinglyDownloader.getReferer(url));
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.168 Safari/535.19");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setConnectTimeout(30000);
		conn.connect();
		
		if (conn.getResponseCode() == 200){
			length = conn.getContentLength();
		}else{
			System.out.println("http status code is " + conn.getResponseCode());
		}
		conn.disconnect();
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
	
}
