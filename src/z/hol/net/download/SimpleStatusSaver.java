package z.hol.net.download;

import java.util.ArrayList;
import java.util.List;

import z.hol.model.SimpleApp;
import z.hol.net.download.AbsDownloadManager.Task;
import z.hol.net.download.ContinuinglyDownloader.DownloadListener;
import z.hol.net.download.utils.AppDownloadUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SimpleStatusSaver implements AppStatusSaver{
	
	private SimpleStateSaverDatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	public SimpleStatusSaver(Context context){
		this(context, false);
	}
	
	public SimpleStatusSaver(Context context, boolean readable){
		dbHelper = new SimpleStateSaverDatabaseHelper(context);
		if (readable)
			db = getReadableDb();
		else
			db = getWriteableDb();
	}

	@Override
	public void addAppDownload(SimpleApp app, String saveFile) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put(APP._ID, app.getAppId());
		values.put(APP.STATE, Task.STATE_PAUSE);
		values.put(APP.LEN, app.getSize());
		values.put(APP.LEN_FORMATED, app.getFormatedSize());
		values.put(APP.ICON, app.getIcon());
		values.put(APP.NAME, app.getName());
		values.put(APP.PACKAGE, app.getPackageName());
		if (saveFile == null){
			saveFile = AppDownloadUtils.getAppSavePath(app.getPackageName());
		}
		values.put(APP.SAVE_FILE, saveFile);
		values.put(APP.URL, app.getAppUrl());
		values.put(APP.VERSION_CODE, app.getVersionCode());
		values.put(APP.VERSION_NAME, app.getVersionName());
		//db.execSQL("insert into app_download_task() ", );
		values.put(APP.START_POS, 0);
		db.insert(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, null, values);
	}

	@Override
	public void updateAppSize(long appId, long size) {
		// TODO Auto-generated method stub
		 ContentValues values = new ContentValues();
		 values.put(APP.LEN, size);
		 db.update(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, values, APP._ID+ "=" + appId, null);
	}

	@Override
	public void updateAppDownloadPos(long appId, long currentPos) {
		// TODO Auto-generated method stub
		 ContentValues values = new ContentValues();
		 values.put(APP.START_POS, currentPos);
		 db.update(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, values, APP._ID+ "=" + appId, null);
	}

	@Override
	public void changeAppTaskState(long appId, int state) {
		// TODO Auto-generated method stub
		 ContentValues values = new ContentValues();
		 values.put(APP.STATE, state);
		 db.update(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, values, APP._ID+ "=" + appId, null);
	}

	@Override
	public void changUrl(long appId, String url) {
		// TODO Auto-generated method stub
		 ContentValues values = new ContentValues();
		 values.put(APP.URL, url);
		 db.update(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, values, APP._ID+ "=" + appId, null);
	}

	@Override
	public AppDownloadTask getAppTask(long appId, AppDownloadTask task) {
		// TODO Auto-generated method stub
		 Cursor c = db.query(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, APP_TASK_PROJECTION, APP._ID + "=" + appId, null, null, null, null);
		 if (c != null && c.moveToFirst()){
			 SimpleApp app = task.getApp();
			 long appSize = c.getLong(2);
			 app.setSize(appSize);
			 task.setStartPos(c.getLong(7));
			 task.setTotal(appSize);
		 }
		 if (c != null) c.close();
		 
		return task;
	}
	
	
	@Override
	public List<AppDownloadTask> getAppTaskList(AppStatusSaver saver, DownloadListener listener) {
		// TODO Auto-generated method stub
//		APP._ID,		// 0
//		APP.ICON,	// 1
//		APP.LEN,		// 2
//		APP.LEN_FORMATED,	// 3
//		APP.NAME,	// 4
//		APP.PACKAGE,		// 5
//		APP.SAVE_FILE,	// 6
//		APP.START_POS,	// 7
//		APP.URL,		// 8
//		APP.VERSION_CODE,	// 9
//		APP.VERSION_NAME		//10
//		APP.STATE	// 11
		
		
		List<AppDownloadTask> tasks = new ArrayList<AppDownloadTask>();
		Cursor c = db.query(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, APP_TASK_PROJECTION, null, null, null, null, null);
		if (c != null){
			while (c.moveToNext()){
				SimpleApp app = new SimpleApp();
				app.setAppId(c.getLong(0));
				app.setIcon(c.getString(1));
				long appSize = c.getLong(2);
				app.setSize(appSize);
				app.setFormatedSize(c.getString(3));
				app.setName(c.getString(4));
				app.setPackageName(c.getString(5));
				app.setAppUrl(c.getString(8));
				app.setVersionCode(c.getInt(9));
				app.setVersionName(c.getString(10));
				String saveFile = c.getString(6);
				long startPos = c.getLong(7);
				int state = c.getInt(11);
				
				AppDownloadTask task = new AppDownloadTask(app, saveFile, startPos, saver, listener);
				task.setTotal(appSize);
				task.setStatus(state);
				tasks.add(task);
			}
		}
		if (c != null) c.close();
		
		return tasks;
	}
	
	@Override
	public void removeAppTask(long appId) {
		// TODO Auto-generated method stub
		db.delete(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, APP._ID + "=" + appId, null);
	}

	@Override
	public SQLiteDatabase getWriteableDb() {
		// TODO Auto-generated method stub
		return dbHelper.getWritableDatabase();
	}

	@Override
	public SQLiteDatabase getReadableDb() {
		// TODO Auto-generated method stub
		return dbHelper.getReadableDatabase();
	}

	@Override
	public void closeDb() {
		// TODO Auto-generated method stub
		if (isOpened())
			db.close();
	}
	
	@Override
	public boolean isOpened() {
		// TODO Auto-generated method stub
		if (db != null)
			return db.isOpen();
		
		return false;
	}

	@Override
	public void beginTransaction() {
		// TODO Auto-generated method stub
		db.beginTransaction();
	}

	@Override
	public void setTransactionSuccessful() {
		// TODO Auto-generated method stub
		db.setTransactionSuccessful();
	}

	@Override
	public void endTransaction() {
		// TODO Auto-generated method stub
		db.endTransaction();
	}

}
