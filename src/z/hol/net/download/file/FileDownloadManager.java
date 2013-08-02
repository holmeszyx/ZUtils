package z.hol.net.download.file;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import z.hol.model.SimpleFile;
import z.hol.net.download.AbsDownloadManager;
import z.hol.net.download.app.AppDownloadTask;
import android.content.Context;

public class FileDownloadManager extends AbsDownloadManager{
	
	private static final AtomicLong TASK_ID_POOL = new AtomicLong(0);
	
	/**
	 * 申请一个Task id
	 * @return
	 */
	public static long obtainTaskId(){
		return TASK_ID_POOL.getAndIncrement();
	}

	private Context mContext;
	private FileStatusSaver mStatusSaver;
	private static FileDownloadManager sDownloadManager;
	private final static byte[] sLock = new byte[0];
	private ConcurrentHashMap<Long, Long> mSubIdToTaskMap = new ConcurrentHashMap<Long, Long>();
	
	private FileDownloadManager(Context context){
		super();
		mContext = context;
		mStatusSaver = new SimpleFileStatusSaver(mContext.getApplicationContext());
		setDownloadTaskListener(this);
	}
	
	public static FileDownloadManager getInstance(Context context){
		if (sDownloadManager == null){
			synchronized (sLock) {
				if (sDownloadManager == null){
					sDownloadManager = new FileDownloadManager(context.getApplicationContext());
					sDownloadManager.resotreDownloadTasks();
				}
			}
		}
		return sDownloadManager;
	}
	
	public void closeStatusDb(){
		mStatusSaver.closeDb();
	}
	
	public FileStatusSaver getStatusSaver(){
		return mStatusSaver;
	}
	
	public boolean addTask(SimpleFile file){
		return addTask(file, true);
	}
	
	public boolean addTask(SimpleFile file, boolean autoStrat){
		String savePath = file.getFileSavePath();
		FileDownloadTask task = new FileDownloadTask(file, savePath, -1, mStatusSaver, this);
		task.setTaskId(getTaskIdWithSubId(file.getSubId(), file.getType()));
		if (!hasTask(task)){
			//mStatusSaver.addDownload(task.getSimpeFile(), task.getFileSavePath());
			task.setTaskId(obtainTaskId());
			mStatusSaver.addTask(task.getSimpeFile(), task.getFileSavePath());
		}
		//return super.addTask(task, autoStrat);
		return addTask(task, autoStrat);
	}
	
	@Override
	public boolean addTask(Task task, boolean autoStart) {
		// TODO Auto-generated method stub
		boolean added = super.addTask(task, autoStart);
		if (added){
			SimpleFile file = ((FileDownloadTask) task).getSimpeFile();
			mSubIdToTaskMap.put(getSubHash(file.getSubId(), file.getType()), task.getTaskId());
		}
		return added;
	}
	
	@Override
	public boolean removeTask(long taskId) {
		// TODO Auto-generated method stub
		boolean removed = super.removeTask(taskId);
		if (removed){
			Set<Entry<Long, Long>> subIdTaskEntry = mSubIdToTaskMap.entrySet();
			Iterator<Entry<Long, Long>> iter = subIdTaskEntry.iterator();
			while (iter.hasNext()){
				Entry<Long, Long> e = iter.next();
				if (e.getValue() == taskId){
					long subid = e.getKey();
					iter.remove();
					mSubIdToTaskMap.remove(subid);
				}
			}
		}
		return removed;
	}
	
	private long getSubHash(long subId, int type){
		return subId * 7219 + type;
	}
	
	/**
	 * 通过SubId，获取taskID
	 * @param subId
	 * @param type
	 * @return
	 */
	public long getTaskIdWithSubId(long subId, int type){
		long hash = getSubHash(subId, type);
		Long taskId = mSubIdToTaskMap.get(hash);
		if (taskId == null){
			return  -1;
		}
		return taskId.longValue();
	}
	
	public Task getTask(long subId, int type){
		Task task = getTask(getTaskIdWithSubId(subId, type));
		return task;
	}
	
	public int getTaskStatus(long subId, int type){
		return getTaskState(getTaskIdWithSubId(subId, type));
	}
	
	/**
	 * 恢复下载任务
	 */
	private void resotreDownloadTasks(){
		List<FileDownloadTask> tasks = mStatusSaver.getDownloadTaskList(mStatusSaver, this);
		long lastId = 0;
		for (int i = 0; i < tasks.size(); i ++){
			FileDownloadTask task = tasks.get(i);
			if (task.getTaskId() > lastId){
				lastId = task.getTaskId();
			}
			int taskStatus = task.getStatus();
			if (taskStatus == Task.STATE_RUNNING || taskStatus == Task.STATE_PERPARE || taskStatus == Task.STATE_WAIT){
				task.setStatus(Task.STATE_PAUSE);
				//task.set
			}
			addTask(task, false);
		}
		TASK_ID_POOL.set(lastId + 1);
	}
	
	@Override
	protected void onTaskRemove(long taskId) {
		// TODO Auto-generated method stub
		super.onTaskRemove(taskId);
		mStatusSaver.removeTask(taskId);
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
