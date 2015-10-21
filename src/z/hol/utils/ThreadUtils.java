package z.hol.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.HandlerThread;
import android.os.Process;

public class ThreadUtils {
    
    public static final boolean IS_OLD_ASYNC_TASK = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB;
    
    
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 3;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadUT #" + mCount.getAndIncrement());
        }
    };
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new SynchronousQueue<Runnable>();
    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     * 核心线程数为{@link #CORE_POOL_SIZE}，不限制并发总线程数!
     * 这就使得任务总能得到执行，且高效执行少量（<={@link #CORE_POOL_SIZE}）异步任务。
     * 线程完成任务后保持{@link #KEEP_ALIVE}秒销毁，这段时间内可重用以应付短时间内较大量并发，提升性能。
     * 它实际控制并执行线程任务。
     */
    public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);


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
	        task.executeOnExecutor(THREAD_POOL_EXECUTOR);
	    }
	}
}
