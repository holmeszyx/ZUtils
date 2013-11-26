package z.hol.utils;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;

public class ThreadUtils {
    
    public static final boolean IS_OLD_ASYNC_TASK = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB;

	/**
	 * 如果AsyncTask在运行的话，则取消一个AsyncTask
	 * @param task
	 */
	public static void cancelAsyncTask(@SuppressWarnings("rawtypes") AsyncTask task){
		if (task != null && task.getStatus() != Status.FINISHED){
			task.cancel(true);
		}
	}
	
	/**
	 * 一个Task是否在运行中
	 * @param task
	 * @return
	 */
	public static boolean isAsyncTaskRunning(@SuppressWarnings("rawtypes") AsyncTask task){
		if (task != null && task.getStatus() != Status.FINISHED){
			return true;
		}
		return false;
	}
	
	/**
	 * 让AsyncTask在多线程中执行
	 * @param task
	 */
    //@SuppressWarnings("unchecked")
    public static void compatAsyncTaskExecute(AsyncTask<Void, ?, ?> task){
	    if (IS_OLD_ASYNC_TASK){
	        task.execute();
	    }else{
	        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	    }
	}
}
