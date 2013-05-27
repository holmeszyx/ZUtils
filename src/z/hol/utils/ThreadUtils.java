package z.hol.utils;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;

public class ThreadUtils {

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
}
