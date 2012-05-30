package z.hol.net.download;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import z.hol.model.SimpleApp;
import z.hol.net.download.ContinuinglyDownloader.DownloadListener;
import android.content.Context;

public class AppDownloadManager extends AbsDownloadManager implements DownloadListener{

	private Context mContext;
	private AppStatusSaver mStatusSaver;
	private static AppDownloadManager downloadManager;
	private List<DownloadUIHandler> mDownloadUIHandlerList;
	
	private AppDownloadManager(Context context){
		mContext = context;
		mStatusSaver = new SimpleStatusSaver(mContext.getApplicationContext());
		mDownloadUIHandlerList = new ArrayList<AppDownloadManager.DownloadUIHandler>();
	}
	
	public static AppDownloadManager getInstance(Context context){
		if (downloadManager == null){
			downloadManager = new AppDownloadManager(context);
		}
		return downloadManager;
	}
	
	public void registUIHandler(DownloadUIHandler uiHandler){
		mDownloadUIHandlerList.add(uiHandler);
	}
	
	public void unregistUIHandler(DownloadUIHandler uiHandler){
		mDownloadUIHandlerList.remove(uiHandler);
	}
	
	public void clearRegistedUIHandler(){
		mDownloadUIHandlerList.clear();
	}
	
	public boolean addTask(SimpleApp app){
		AppDownloadTask task = new AppDownloadTask(app, -1, mStatusSaver, this);
		return super.addTask(task);
	}
	
	public int getTaskState(long taskId){
		Task task = getTask(taskId);
		if (task == null){
			return Task.STATE_INVALID;
		}
		return task.getStatus();
	}

	@Override
	public void onComplete(long id) {
		// TODO Auto-generated method stub
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.complete(id);
		}
	}

	@Override
	public void onStart(long id, long total, long current) {
		// TODO Auto-generated method stub
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.start(id, total, current);
		}
	}

	@Override
	public void onError(long id, int errorCode) {
		// TODO Auto-generated method stub
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.error(id, errorCode);
		}
	}

	@Override
	public void onCancel(long id) {
		// TODO Auto-generated method stub
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.cancel(id);
		}
	}

	@Override
	public void onProgress(long id, long total, long current) {
		// TODO Auto-generated method stub
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.progress(id, total, current);
		}
	}

}
