package z.hol.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

/**
 * 自动延迟关闭的数据库
 * @author holmes
 *
 */
public abstract class AutoCloseDatabase implements DatabaseHandler{
    /**
     * 默认延迟2分钟
     */
    public static final long DEFAULT_DELAY = 2 * 60 * 1000l;
    
    /**
     * 延迟
     */
    final private long mDelay;
    private Handler mHandler;
    private CloseRunnable mCloseRunnable;
    
    public AutoCloseDatabase(long delay, Handler handler){
        if (handler == null){
            throw new NullPointerException("handler is null, you can use AutoCloseDatabase(delay) constrator");
        }
        mDelay = delay;
        mHandler = handler;
        mCloseRunnable = new CloseRunnable(this);
    }
    
    public AutoCloseDatabase(long delay){
        mDelay = delay;
        mHandler = new Handler();
        mCloseRunnable = new CloseRunnable(this);
    }
    
    /**
     * 延迟关闭数据库
     */
    public void delayClose(){
        mHandler.removeCallbacks(mCloseRunnable);
        mHandler.postDelayed(mCloseRunnable, mDelay);
    }
    
    /**
     * 获取当前的数据库
     * @return  可能是null
     */
    public abstract SQLiteDatabase getCurrentDb();
    
    /**
     * 数据库是否可读
     * @return
     */
    public abstract boolean isDatabaseReadable();
    
    /**
     * 数据库是否可写
     * @return
     */
    public abstract boolean isDatabaseWriteable();
    
    /**
     * 确保有一个可读的数据库
     * @return 如果 {@link #isDatabaseReadable()} 返回false, 则会调用 {@link #getReadableDb()}.
     *          返回 true, 调用 {@link #getCurrentDb()}
     */
    public SQLiteDatabase ensureReadableDb(){
        delayClose();
        if (!isDatabaseReadable()){
            return getReadableDb();
        }
        return getCurrentDb();
    }
    
    /**
     * 确保有一个可写的数据库
     * @return 如果 {@link #isDatabaseWriteable()} 返回false, 则会调用 {@link #getWriteableDb()}.
     *          返回 true, 调用 {@link #getCurrentDb()}
     */
    public SQLiteDatabase ensureWriteableDb(){
        delayClose();
        if (!isDatabaseWriteable()){
            return getWriteableDb();
        }
        return getCurrentDb();
    }

    /**
     * 延迟关闭
     * @author holmes
     *
     */
    private static class CloseRunnable implements Runnable{
        private AutoCloseDatabase mDb;
        
        public CloseRunnable(AutoCloseDatabase db) {
            // TODO Auto-generated constructor stub
            mDb = db;
        }

        @Override
        public void run() {
            // It is Auto-generated method stub
            if (mDb != null){
                if (mDb.isOpened()){
                    mDb.closeDb();
                }
            }
        }
        
    }
}
