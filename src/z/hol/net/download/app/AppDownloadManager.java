package z.hol.net.download.app;

import java.util.List;

import z.hol.model.SimpleApp;
import z.hol.net.download.AbsDownloadManager;
import z.hol.net.download.utils.AppDownloadUtils;
import android.content.Context;

public class AppDownloadManager extends AbsDownloadManager{

	private Context mContext;
	private AppStatusSaver mStatusSaver;
	private static AppDownloadManager sDownloadManager;
	private final static byte[] sLock = new byte[0];
	
	private AppDownloadManager(Context context){
		super();
		mContext = context;
		mStatusSaver = new SimpleStatusSaver(mContext.getApplicationContext());
		setDownloadTaskListener(this);
	}
	
	public static AppDownloadManager getInstance(Context context){
		if (sDownloadManager == null){
			synchronized (sLock) {
				if (sDownloadManager == null){
					sDownloadManager = new AppDownloadManager(context.getApplicationContext());
					sDownloadManager.resotreAppTasks();
				}
			}
		}
		return sDownloadManager;
	}
	
	public void closeStatusDb(){
		mStatusSaver.closeDb();
	}
	
	public AppStatusSaver getStatusSaver(){
		return mStatusSaver;
	}
	
	public boolean addTask(SimpleApp app){
		return addTask(app, true);
	}
	
	public boolean addTask(SimpleApp app, boolean autoStrat){
		String appSavePath = AppDownloadUtils.getAppSavePath(app.getPackageName());
		AppDownloadTask task = new AppDownloadTask(app, appSavePath, -1, mStatusSaver, this);
		if (!hasTask(task)){
			mStatusSaver.addAppDownload(task.getApp(), task.getFileSavePath());
		}
		return super.addTask(task, autoStrat);
	}
	
	/**
	 * 恢复应用下载任务
	 */
	private void resotreAppTasks(){
		List<AppDownloadTask> tasks = mStatusSaver.getAppTaskList(mStatusSaver, this);
		for (int i = 0; i < tasks.size(); i ++){
			AppDownloadTask task = tasks.get(i);
			int taskStatus = task.getStatus();
			if (taskStatus == Task.STATE_RUNNING || taskStatus == Task.STATE_PERPARE || taskStatus == Task.STATE_WAIT){
				task.setStatus(Task.STATE_PAUSE);
			}
			addTask(task, false);
		}
	}
	
	@Override
	protected void onTaskRemove(long taskId) {
		// TODO Auto-generated method stub
		super.onTaskRemove(taskId);
		mStatusSaver.removeAppTask(taskId);
	}
	
	@Override
	protected void onCancelWaitTask(Task task) {
		// TODO Auto-generated method stub
		super.onCancelWaitTask(task);
		if (task instanceof AppDownloadTask){
			AppDownloadTask appTask = (AppDownloadTask) task;
			appTask.onCancel(appTask.getTaskId());
		}
	}

	@Override
	public void onComplete(long id) {
		// TODO Auto-generated method stub
		super.onComplete(id);
		launchWaitTask();
	}

	@Override
	protected boolean afterOnPauseUiCallback(long id) {
		// TODO Auto-generated method stub
		if (super.afterOnPauseUiCallback(id)){
			return true;
		}
		
		// 替换以前的oncancel的重写
		Task task = getTask(id);
		if (task != null && task.getStatus() != Task.STATE_WAIT){
			// 如果不是暂停的等待Task
			launchWaitTask();
			return true;
		}else if (task == null){
			// 移除的任务被取消
			launchWaitTask();
			return true;
		}
		return false;
	}

	/**
	 * 尝试启动等待的Task
	 */
	private void launchWaitTask(){
		taskStoped();
		startWaitTask();
	}
}
