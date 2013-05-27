package z.hol.net.download;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Message;

/**
 * 下载管理器<br>
 * 如果要限制最大同时运行的task数量，一定要控制好{@link #taskStarted()}和 {@link #taskStoped()}
 * @author holmes
 *
 */
public abstract class AbsDownloadManager implements DownloadTaskListener{
	private static final int DEFAULT_MAX_RUNNING = 2;
	
	/**
	 * 任务<br>
	 * {@link Task#STATE_WAIT} --> {@link Task#STATE_PERPARE} --> {@link Task#STATE_RUNNING}
	 * --> {@link Task#STATE_PAUSE} --> {@link Task#STATE_COMPLETE}
	 * @author holmes
	 *
	 */
	public static interface Task{
		public static final int STATE_INVALID = -1;
		public static final int STATE_PERPARE = 0;
		public static final int STATE_RUNNING = 1;
		public static final int STATE_PAUSE = 2;
		public static final int STATE_COMPLETE = 3;
		public static final int STATE_WAIT = 4;
		
		
		/**
		 * 等待开始
		 */
		public void waitForStart();
		
		/**
		 * 结束等等，可以开始运行<br>
		 * 一般只需要改变task的等待状态
		 */
		public void notifyStart();
		
		/**
		 * 开始
		 */
		public void start();
		
		/**
		 * 取消
		 */
		public void cancel();
		
		/**
		 * 继续
		 */
		public void goon();
		
		/**
		 * 重新下载<br>
		 * task要重置一些状态
		 */
		public void redownload();
		
		/**
		 * 是否需要重新下载<br>
		 * 一般在检测到中断状态时使用。
		 * 中断状态有 Cancel, Error, Complete
		 * @return
		 */
		public boolean isNeedRedownload();
		
		/**
		 * 获取下载状态
		 * @return
		 */
		public int getStatus();
		
		/**
		 * 获取任务ID
		 * @return
		 */
		public long getTaskId();
		
		/**
		 * 得到当前进度百分比
		 * @return
		 */
		public int getPercent();
	}
	
	private ConcurrentHashMap<Long, Task> mTaskMap;
	private ConcurrentLinkedQueue<Task> mWaitQueue;
	private AtomicInteger mRunningTask;
	private AtomicInteger mCompletedTask;
	private int mMaxRunning = DEFAULT_MAX_RUNNING;
	private DownloadTaskListener mDownloadTaskListener;
	private List<DownloadUIHandler> mDownloadUIHandlerList;
	
	public AbsDownloadManager(){
		mTaskMap = new ConcurrentHashMap<Long, AbsDownloadManager.Task>();
		mWaitQueue = new ConcurrentLinkedQueue<AbsDownloadManager.Task>();
		mRunningTask = new AtomicInteger(0);
		mCompletedTask = new AtomicInteger(0);
		mDownloadUIHandlerList = new LinkedList<DownloadUIHandler>();
		setDownloadTaskListener(this);
	}
	
	public void setDownloadTaskListener(DownloadTaskListener listener){
		mDownloadTaskListener = listener;
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
	
	/**
	 * 设置最大同时运行的task数量
	 * @param limit 同时运行的task数，如果为 0 则无限制
	 */
	public void setMaxRunningTaskLimit(int limit){
		if (limit < 0){
			throw new IllegalArgumentException("the limit must be greater than or equal to 0, but now you give a " + limit);
		}
		mMaxRunning = limit;
	}
	
	/**
	 * 获取最大同时运行Task数
	 * @return
	 */
	public int getMaxRunningTaskLimit(){
		return mMaxRunning;
	}
	
	
	/**
	 * 计算百分比
	 * @param total
	 * @param current
	 * @return
	 */
	public static int computePercent(long total, long current){
		if (total <= 0){
			return 0;
		}
		return (int) (current * 100 / total);
	}
	
	/**
	 * 添加一个任务
	 * @param task
	 * @param autoStart 是否自动运行任务
	 * @return
	 */
	public boolean addTask(Task task, boolean autoStart){
		if (!hasTask(task)){
			if (task.getStatus() == Task.STATE_COMPLETE && !task.isNeedRedownload()){
				taskCompleted();
			}
			mTaskMap.put(task.getTaskId(), task);
			invokeDownloadAdd(task.getTaskId());
			if (autoStart){
				startTask(task);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 添加一个任务，并自动运行
	 * @param task
	 * @return
	 */
	public boolean addTask(Task task){
		return addTask(task, true);
	}
	
	/**
	 * Task是否存在
	 * @param task
	 * @return
	 */
	public boolean hasTask(Task task){
		if (mTaskMap.get(task.getTaskId()) == null)
			return false;
		else
			return true;
	}
	
	/**
	 * 开始一个任务
	 * @param taskId
	 * @return
	 */
	public boolean startTask(long taskId){
		Task task = getTask(taskId);
		return startTask(task);
		/*
		if (task != null){
			int status = task.getStatus();
			if (status == Task.STATE_PAUSE){
				task.goon();
			}else{
				task.start();
			}
			return true;
		}
		return false;
		*/
	}
	
	private boolean startTask(Task task){
		if (task != null){
			if (isNeedToWait()){
				task.waitForStart();
				waitTask(task);
				invokeDownloadWait(task.getTaskId());
				return false;
			}
			taskStarted();
			task.notifyStart();
			int status = task.getStatus();
			if (status == Task.STATE_PAUSE){
				task.goon();
			}else{
				task.start();
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 重新下载
	 * @param taskId
	 */
	public void redownload(long taskId){
		Task task = getTask(taskId);
		redownload(task);
	}
	
	/**
	 * 重新下载
	 * @param task
	 */
	private void redownload(Task task){
		if (task != null){
			cancelTask(task);
			int status = task.getStatus();
			task.redownload();
			if (status == Task.STATE_COMPLETE || status == Task.STATE_PAUSE){
				// 可以直接启动
				if (status == Task.STATE_COMPLETE){
					// 因为需要改task的状态统计数
					//removeTask(task.getTaskId());
					//addTask(task, false);
					completedTaskRemoved();
				}
				startTask(task);
			}
		}
	}
	
	private boolean isTaskNeedRedownload(long taskId){
		Task task = getTask(taskId);
		if (task != null){
			return task.isNeedRedownload();
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private void internalRedownload(Task task){
		startTask(task);
	}
	
	/**
	 * 一个任务已运行<br>
	 * 正在运行的任务数加一
	 */
	protected void taskStarted(){
		mRunningTask.incrementAndGet();
		System.out.println("runing+: " + getRunningTaskCount());
	}
	
	/**
	 * 一个任务已停止运行<br>
	 * 暂停，完成，出错<br>
	 * 正在运行的任务数减一
	 */
	protected void taskStoped(){
		if (getRunningTaskCount() == 0){
			System.out.println("runing-: empty");
			return;
		}
		mRunningTask.decrementAndGet();
		System.out.println("runing-: " + getRunningTaskCount());
	}
	
	/**
	 * 一个任务完成
	 */
	protected void taskCompleted(){
		mCompletedTask.incrementAndGet();
	}
	
	/**
	 * 一个完成的任务被移除
	 */
	protected void completedTaskRemoved(){
		mCompletedTask.decrementAndGet();
	}
	
	/**
	 * 获取运行已任务的数量
	 * @return
	 */
	public int getRunningTaskCount(){
		return mRunningTask.get();
	}
	
	/**
	 * 获取等待任务的数量
	 * @return
	 */
	public int getWaitingTaskCount(){
		return mWaitQueue.size();
	}
	
	public int getCompletedTaskCount(){
		return mCompletedTask.get();
	}
	
	/**
	 * 获取任务的总数量<br>
	 * <b>任何状态的任务</b>
	 * @return
	 */
	public int getTaskCount(){
		return mTaskMap.size();
	}
	
	/**
	 * 是否需要将task放入等等队列<br>
	 * 主要是有很task在运行
	 * @return
	 */
	private boolean isNeedToWait(){
		if (mMaxRunning == 0 || getRunningTaskCount() < mMaxRunning){
			return false;
		}
		return true;
	}
	
	/**
	 * 将Task放入等待队列
	 * @param task
	 * @return
	 */
	private boolean waitTask(Task task){
		return mWaitQueue.add(task);
	}
	
	/**
	 * 从等待队列拿出一个task
	 * @return
	 */
	private Task notifyTask(){
		return mWaitQueue.poll();
	}
	
	private boolean cancelWaitTask(Task task){
		if (task == null){
			return false;
		}
		onCancelWaitTask(task);
		task.notifyStart();
		return mWaitQueue.remove(task);
	}
	
	/**
	 * 当等待的Task被取消时执行
	 * @param task
	 */
	protected void onCancelWaitTask(Task task){
		
	}
	
	/**
	 * 启动一个等待的任务
	 */
	public void startWaitTask(){
		if (!isNeedToWait()){
			Task task = notifyTask();
			if (task != null){
				startTask(task);
			}
		}
	}
	
	/**
	 * 取消所有任务
	 */
	public void cancelAllTask(){
		mWaitQueue.clear();
		Set<Entry<Long, Task>> taskSet = mTaskMap.entrySet();
		for (Entry<Long, Task> taskEntry : taskSet){
			Task task = taskEntry.getValue();
			cancelTask(task);
		}
	}
	
	/**
	 * 取消一个任务
	 * @param taskId
	 * @return
	 */
	public boolean cancelTask(long taskId){
		Task task = getTask(taskId);
		return cancelTask(task);
	}
	
	/**
	 * 取消一个任务
	 * @param task
	 * @return
	 */
	protected boolean cancelTask(Task task){
		if (task != null){
			task.cancel();
			if (task.getStatus() == Task.STATE_WAIT){
				cancelWaitTask(task);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 移除一个任务
	 * @param taskId
	 * @return
	 */
	public boolean removeTask(long taskId){
		Task task = getTask(taskId);
		if (task != null){
			cancelTask(task);
			mTaskMap.remove(taskId);
			if (task.getStatus() == Task.STATE_COMPLETE){
				completedTaskRemoved();
			}
			onTaskRemove(taskId);
			invokeDownloadRemove(taskId);
			return true;
		}
		return false;
	}
	
	/**
	 * 当TASK移除后
	 * @param taskId
	 */
	protected void onTaskRemove(long taskId) {
		
	}
	
	/**
	 * 获取一个任务
	 * @param taskId
	 * @return
	 */
	public Task getTask(long taskId){
		return mTaskMap.get(taskId);
	}
	
	/**
	 * 获取task的状态
	 * @param taskId
	 * @return
	 */
	public int getTaskState(long taskId){
		Task task = getTask(taskId);
		if (task == null){
			return Task.STATE_INVALID;
		}
		return task.getStatus();
	}
	
	/**
	 * task是否是激活的, 即状态为
	 * {@link Task#STATE_RUNNING}, {@link Task#STATE_PERPARE}, 
	 * {@link Task#STATE_WAIT}, {@link Task#STATE_COMPLETE}。
	 * 注意思，包括完成状态
	 * @param taskId
	 * @return
	 */
	public boolean isTaskActive(long taskId){
		int status = getTaskState(taskId);
		if (status == Task.STATE_RUNNING ||
				status == Task.STATE_PERPARE ||
				status == Task.STATE_WAIT ||
				status == Task.STATE_COMPLETE){
			return true;
		}
		return false;
	}
	
	/**
	 * 获取的Task的列表
	 * @return tasks的列表,如果没有task则返回null
	 */
	public List<Task> getTasks(){
		int count = getTaskCount();
		if (count == 0) return null;
		
		int size = count < 16 ? 16 : count;
		List<Task> tasks = new ArrayList<AbsDownloadManager.Task>(size);
		tasks.addAll(mTaskMap.values());
		return tasks;
	}
	
	private void invokeDownloadAdd(long id){
		if (mDownloadTaskListener != null){
			mDownloadTaskListener.onAdd(id);
		}
	}
	
	private void invokeDownloadWait(long id){
		if (mDownloadTaskListener != null){
			mDownloadTaskListener.onWait(id);
		}
	}
	
	private void invokeDownloadRemove(long id){
		if (mDownloadTaskListener != null){
			mDownloadTaskListener.onRemove(id);
		}
	}
	
	@Override
	public void onComplete(long id) {
		// TODO Auto-generated method stub
		taskCompleted();
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.complete(id);
		}
		if (isTaskNeedRedownload(id)){
			completedTaskRemoved();
			startTask(id);
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
		afterOnPauseUiCallback(id);
		if (isTaskNeedRedownload(id)){
			startTask(id);
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
		afterOnPauseUiCallback(id);
		if (isTaskNeedRedownload(id)){
			startTask(id);
		}
	}
	
	/**
	 * 当pause的UI回调完成后(其它任务操作前，如重新下载任务)，
	 * 默认如果是一个移除的任务(即移除后，中断操作才触发),
	 * 将会自动 {@link #taskStoped()} 和 {@link #startWaitTask()}
	 * @param id
	 */
	protected boolean afterOnPauseUiCallback(long id){
		if (getTask(id) == null){
			// 一个已移除的任务
			taskStoped();
			startWaitTask();
			return true;
		}
		return false;
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

	@Override
	public void onPrepare(long id) {
		// TODO Auto-generated method stub
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.prepare(id);
		}
	}
	
	@Override
	public void onAdd(long id) {
		// TODO Auto-generated method stub
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.taskAdd(id);
		}
	}

	@Override
	public void onWait(long id) {
		// TODO Auto-generated method stub
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.taskWait(id);
		}
	}
	
	@Override
	public void onRemove(long id) {
		// TODO Auto-generated method stub
		Iterator<DownloadUIHandler> iter = mDownloadUIHandlerList.iterator();
		while(iter.hasNext()){
			DownloadUIHandler uiHandler = iter.next();
			uiHandler.taskRemove(id);
		}
	}
	
	/**
	 * 下载管理器，回调事件
	 * @author holmes
	 *
	 */
	public static class Event{
		public static final int TYPE_START = 1;
		public static final int TYPE_PROGRESS = 2;
		public static final int TYPE_CANCEL = 3;
		public static final int TYPE_ERROR = 4;
		public static final int TYPE_COMPLETE = 5;
		public static final int TYPE_ADD = 6;
		public static final int TYPE_WAIT = 7;
		public static final int TYPE_PREPARE = 8;
		public static final int TYPE_REMOVE = 9;
		
		public int type;
		public long id;
		public long total;
		public long current;
		public int errorCode;
		
		public static Event obtain(){
			return new Event();
		}
		
		public static Event obtain(int type){
			Event e = obtain();
			e.type = type;
			return e;
		}
	}
	
	/**
	 * 下载管理器的UI回调<br>
	 * 重写{@link DownloadUIHandler#filterId(long)} 来加入ID过滤
	 * @author holmes
	 *
	 */
	public static abstract class DownloadUIHandler extends Handler implements DownloadTaskListener{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.obj != null && msg.obj instanceof Event){
				Event e = (Event) msg.obj;
				//if (!filterId(e.id)) return;
				 
				switch (e.type){
				case Event.TYPE_START:
					onStart(e.id, e.total, e.current);
					break;
				case Event.TYPE_CANCEL:
					onCancel(e.id);
					break;
				case Event.TYPE_COMPLETE:
					onComplete(e.id);
					break;
				case Event.TYPE_ERROR:
					onError(e.id, e.errorCode);
					break;
				case Event.TYPE_PROGRESS:
					onProgress(e.id, e.total, e.current);
					break;
				case Event.TYPE_ADD:
					onAdd(e.id);
					break;
				case Event.TYPE_WAIT:
					onWait(e.id);
					break;
				case Event.TYPE_PREPARE:
					onPrepare(e.id);
					break;
				case Event.TYPE_REMOVE:
					onRemove(e.id);
					break;
				}
			}
		}
		
		/**
		 * 过滤ID
		 * @param id
		 * @return 如果需要则返回true(默认),否则返回false，则排除此id
		 */
		protected boolean filterId(long id){
			return true;
		}
		
		void start(long id, long total, long current){
			if (!filterId(id)) return;
			
			Event e = Event.obtain();
			e.type = Event.TYPE_START;
			e.id = id;
			e.total = total;
			e.current = current;
			obtainMessage(e.type, e).sendToTarget();
		}
		
		void error(long id, int errorCode){
			if (!filterId(id)) return;
			
			Event e = Event.obtain();
			e.type = Event.TYPE_ERROR;
			e.id = id;
			e.errorCode = errorCode;
			obtainMessage(e.type, e).sendToTarget();
		}
		
		void cancel(long id){
			if (!filterId(id)) return;
			
			Event e = Event.obtain();
			e.type = Event.TYPE_CANCEL;
			e.id = id;
			obtainMessage(e.type, e).sendToTarget();
		}
		
		void progress(long id, long total, long current){
			if (!filterId(id)) return;
			
			Event e = Event.obtain();
			e.type = Event.TYPE_PROGRESS;
			e.id = id;
			e.total = total;
			e.current = current;
			obtainMessage(e.type, e).sendToTarget();
		}
		
		void complete(long id){
			if (!filterId(id)) return;
			
			Event e = Event.obtain();
			e.type = Event.TYPE_COMPLETE;
			e.id = id;
			obtainMessage(e.type, e).sendToTarget();
		}
		
		void prepare(long id){
			if (!filterId(id)) return;
			
			Event e = Event.obtain();
			e.type = Event.TYPE_PREPARE;
			e.id = id;
			obtainMessage(e.type, e).sendToTarget();
		}
		
		void taskAdd(long id){
			if (!filterId(id)) return;
			
			Event e = Event.obtain();
			e.type = Event.TYPE_ADD;
			e.id = id;
			obtainMessage(e.type, e).sendToTarget();
		}
		
		void taskWait(long id){
			if (!filterId(id)) return;
			
			Event e = Event.obtain();
			e.type = Event.TYPE_WAIT;
			e.id = id;
			obtainMessage(e.type, e).sendToTarget();
		}
		
		void taskRemove(long id){
			if (!filterId(id)) return;
			
			Event e = Event.obtain();
			e.type = Event.TYPE_REMOVE;
			e.id = id;
			obtainMessage(e.type, e).sendToTarget();
		}
	}	
}
