package z.hol.net.download;

import java.util.HashMap;

import z.hol.net.download.ContinuinglyDownloader.DownloadListener;
import android.os.Handler;
import android.os.Message;

/**
 * 下载管理器
 * @author holmes
 *
 */
public abstract class AbsDownloadManager {
	
	/**
	 * 任务
	 * @author holmes
	 *
	 */
	public static interface Task{
		public static final int STATE_INVALID = -1;
		public static final int STATE_PERPARE = 0;
		public static final int STATE_RUNNING = 1;
		public static final int STATE_PAUSE = 2;
		public static final int STATE_COMPLETE = 3;
		
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
	
	private HashMap<Long, Task> taskMap;
	public AbsDownloadManager(){
		taskMap = new HashMap<Long, AbsDownloadManager.Task>();
	}
	
	
	/**
	 * 计算百分比
	 * @param total
	 * @param current
	 * @return
	 */
	public static int computePercent(long total, long current){
		return (int) (current * 100 / total);
	}
	
	/**
	 * 添加一个任务
	 * @param task
	 * @param autoStart 是否自动运行任务
	 * @return
	 */
	public boolean addTask(Task task, boolean autoStart){
		if (taskMap.get(task.getTaskId()) == null){
			taskMap.put(task.getTaskId(), task);
			if (autoStart){
				task.start();
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
	 * 开始一个任务
	 * @param taskId
	 * @return
	 */
	public boolean startTask(long taskId){
		Task task = getTask(taskId);
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
	}
	
	/**
	 * 取消一个任务
	 * @param taskId
	 * @return
	 */
	public boolean cancelTask(long taskId){
		Task task = getTask(taskId);
		if (task != null){
			task.cancel();
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
			task.cancel();
			taskMap.remove(taskId);
			return true;
		}
		return false;
	}
	
	/**
	 * 获取一个任务
	 * @param taskId
	 * @return
	 */
	public Task getTask(long taskId){
		return taskMap.get(taskId);
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
	public static abstract class DownloadUIHandler extends Handler implements DownloadListener{

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
		
	}	
}
