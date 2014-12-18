package z.hol.net.download.file;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import z.hol.model.SimpleFile;
import z.hol.net.download.AbsDownloadManager;
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
	
	@Override
	protected void onSaveRedirectUrlStateChanged(boolean newSaveState) {
	    // It is Auto-generated method stub
        List<Task> allTask = getTasks();
        if (allTask != null){
            for(Task task : allTask){
                FileDownloadTask ft = (FileDownloadTask) task;
                ft.setSaveRedirectUrl(newSaveState);
            }
        }
	}
	
	public boolean addTask(SimpleFile file){
		return addTask(file, true);
	}
	
	/**
	 * 添加一个文件下载
	 * @param file
	 * @param autoStrat
	 * @return
	 */
	public boolean addTask(SimpleFile file, boolean autoStrat){
		String savePath = file.getFileSavePath();
		FileDownloadTask task = new FileDownloadTask(file, savePath, -1, mStatusSaver, this);
		task.setTaskId(getTaskIdWithSubId(file.getSubId(), file.getType()));
		task.setSaveRedirectUrl(isSaveRedirectUrl());
		if (!hasTask(task)){
			//mStatusSaver.addDownload(task.getSimpeFile(), task.getFileSavePath());
			task.setTaskId(obtainTaskId());
			mStatusSaver.addTask(task.getSimpeFile(), task.getFileSavePath());
		}
		//return super.addTask(task, autoStrat);
		return addTask(task, autoStrat);
	}
	
	/**
	 * 添加一个已完成的任务.
	 * 注：对于添加的任务, 必须要谨慎对待.
	 * 管理器，不会做有效性验证
	 * @param file
	 * @return true 添加成功, false 失败(可能文件不存在; 下载任务已存在)
	 */
	public boolean addCompleteTask(SimpleFile file){
		String savePath = file.getFileSavePath();
		File completedFile = new File(savePath);
		if (!completedFile.exists()){
			// 文件不存在
			return false;
		}
		final long fileSize = completedFile.length();
		FileDownloadTask task = new FileDownloadTask(file, savePath, -1, mStatusSaver, this);
		// 先尝试匹配下载task id
		task.setTaskId(getTaskIdWithSubId(file.getSubId(), file.getType()));
		if (!hasTask(task)){
			// 无匹配， 则生成一个id
			task.setTaskId(obtainTaskId());
			mStatusSaver.addTask(task.getSimpeFile(), task.getFileSavePath());
			long taskId = task.getTaskId();
			// 修改task的状态
			task.setStatus(Task.STATE_COMPLETE);
			task.setSize(fileSize);
			task.setStartPos(fileSize);
			mStatusSaver.beginTransaction();
			try {
				mStatusSaver.changeTaskState(taskId, Task.STATE_COMPLETE);
				mStatusSaver.updateDownloadPos(taskId, fileSize);
				mStatusSaver.updateTaskSize(taskId, fileSize);
				mStatusSaver.setTransactionSuccessful();
			} finally{
				mStatusSaver.endTransaction();
			} 
		}
		boolean added = addTask(task, false);
		return added;
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
	
	public int getTaskState(long subId, int type){
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
			task.setSaveRedirectUrl(isSaveRedirectUrl());
			if (task.getTaskId() > lastId){
				lastId = task.getTaskId();
			}
			int taskStatus = task.getStatus();
//			if (taskStatus == Task.STATE_RUNNING || taskStatus == Task.STATE_PERPARE || taskStatus == Task.STATE_WAIT){
//				task.setStatus(Task.STATE_PAUSE);
//				//task.set
//			}
			if (taskStatus != Task.STATE_COMPLETE){
				task.setStatus(Task.STATE_PAUSE);
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
		if (task instanceof FileDownloadTask){
			FileDownloadTask fileTask = (FileDownloadTask) task;
			fileTask.onCancel(fileTask.getTaskId());
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
