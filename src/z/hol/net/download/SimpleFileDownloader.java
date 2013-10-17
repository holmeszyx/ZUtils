package z.hol.net.download;

import z.hol.net.download.AbsDownloadManager.DownloadUIHandler;

/**
 * 一个单文件下载器<br>
 * 没有断点续传，只带普通的断线自动重联<br>
 * 直接下载，主要用于对下载要求不高的地方
 * @author holmes
 *
 */
public class SimpleFileDownloader extends ContinuinglyDownloader{
	
	private DownloadUIHandler mUIHandler;
	private long id;

	public SimpleFileDownloader(long id, String url, String filePath, DownloadUIHandler uiHandler) {
		super(url, 0, -1, 0, filePath);
		// TODO Auto-generated constructor stub
		this.mUIHandler = uiHandler;
		this.id = id;
	}

	@Override
	protected boolean isAleadyComplete(long startPos, long remain,
			long blockSize) {
		// TODO Auto-generated method stub
		// return super.isAleadyComplete(startPos, remain, blockSize);
		if (startPos == blockSize){
			return true;
		}else{
			return false;
		}
	}

	@Override
	protected void onBlockComplete() {
		// TODO Auto-generated method stub
		super.onBlockComplete();
		mUIHandler.complete(id);
	}

	@Override
	protected void onCancel() {
		// TODO Auto-generated method stub
		super.onCancel();
		mUIHandler.cancel(id);
	}

	@Override
	protected void onDownloadError(int errorCode) {
		// TODO Auto-generated method stub
		super.onDownloadError(errorCode);
		mUIHandler.error(id, errorCode);
	}

	@Override
	protected void onPerpareFileSizeDone(long total) {
		// TODO Auto-generated method stub
		super.onPerpareFileSizeDone(total);
	}

	@Override
	protected void onPrepare() {
		// TODO Auto-generated method stub
		super.onPrepare();
		mUIHandler.prepare(id);
	}

	@Override
	protected void onStart(long startPos, long remain, long blockSize) {
		// TODO Auto-generated method stub
		super.onStart(startPos, remain, blockSize);
		mUIHandler.start(id, blockSize, startPos);
	}

	@Override
	protected void saveBreakpoint(long startPos, long remain, long blockSize) {
		// TODO Auto-generated method stub
		super.saveBreakpoint(startPos, remain, blockSize);
		mUIHandler.progress(id, blockSize, startPos);
	}

	public void start(){
		new Thread(this).start();
	}
	
}
