package z.hol.utils;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.HandlerThread;
import android.os.Process;

public class ThreadUtils {
    
    public static final boolean IS_OLD_ASYNC_TASK = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB;

    /** 
     * 一个带looper的，后台线程。
     * 主要是提供给handler用在一些性能，
     * 级别不高的地方
     * 
     */
    private static final HandlerThread sInternalWorkHandler = new HandlerThread("workHandler", Process.THREAD_PRIORITY_BACKGROUND);
    static{
    	sInternalWorkHandler.start();
    }
    
    /**
     * 获取一个全局的，后台使用的HandlerThread.
     * 主要是提供给handler用在一些性能，
     * 级别不高的地方
     * @return
     */
    public static HandlerThread getSingleHandlerThread(){
    	return sInternalWorkHandler;
    }
    
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
	public static void compatAsyncTaskExecute(AsyncTask<Void, ?, ?> task){
	    if (IS_OLD_ASYNC_TASK){
	        task.execute();
	    }else{
	        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	    }
	}
}
