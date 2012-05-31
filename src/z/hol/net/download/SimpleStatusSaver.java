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
	
	SimpleStateSaverDatabaseHelper dbHelper;
	
	public SimpleStatusSaver(Context context){
		dbHelper = new SimpleStateSaverDatabaseHelper(context);
	}

	@Override
	public void addAppDownload(SimpleApp app) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(APP._ID, app.getAppId());
		values.put(APP.STATE, Task.STATE_PAUSE);
		values.put(APP.LEN, app.getSize());
		values.put(APP.LEN_FORMATED, app.getFormatedSize());
		values.put(APP.ICON, app.getIcon());
		values.put(APP.NAME, app.getName());
		values.put(APP.PACKAGE, app.getPackageName());
		values.put(APP.SAVE_FILE, AppDownloadUtils.getAppSavePath(app.getPackageName()));
		values.put(APP.URL, app.getAppUrl());
		values.put(APP.VERSION_CODE, app.getVersionCode());
		values.put(APP.VERSION_NAME, app.getVersionName());
		//db.execSQL("insert into app_download_task() ", );
		values.put(APP.START_POS, 0);
		db.insert(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, null, values);
		db.close();
	}

	@Override
	public void updateAppSize(long appId, long size) {
		// TODO Auto-generated method stub
		 SQLiteDatabase db = getWritableDatabase();
		 ContentValues values = new ContentValues();
		 values.put(APP.LEN, size);
		 db.update(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, values, APP._ID+ "=" + appId, null);
		 db.close();
	}

	@Override
	public void updateAppDownloadPos(long appId, long currentPos) {
		// TODO Auto-generated method stub
		 SQLiteDatabase db = getWritableDatabase();
		 ContentValues values = new ContentValues();
		 values.put(APP.START_POS, currentPos);
		 db.update(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, values, APP._ID+ "=" + appId, null);
		 db.close();
	}

	@Override
	public void changeAppTaskState(long appId, int state) {
		// TODO Auto-generated method stub
		 SQLiteDatabase db = getWritableDatabase();
		 ContentValues values = new ContentValues();
		 values.put(APP.STATE, state);
		 db.update(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, values, APP._ID+ "=" + appId, null);
		 db.close();
	}

	@Override
	public AppDownloadTask getAppTask(long appId, AppDownloadTask task) {
		// TODO Auto-generated method stub
		 SQLiteDatabase db = getReadableDatabase();
		 Cursor c = db.query(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, APP_TASK_PROJECTION, APP._ID + "=" + appId, null, null, null, null);
		 if (c != null && c.moveToFirst()){
			 SimpleApp app = task.getApp();
			 app.setSize(c.getLong(2));
			 task.setStartPos(c.getLong(7));
		 }
		 if (c != null) c.close();
		 db.close();
		 
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
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, APP_TASK_PROJECTION, null, null, null, null, null);
		if (c != null){
			while (c.moveToNext()){
				SimpleApp app = new SimpleApp();
				app.setAppId(c.getLong(0));
				app.setIcon(c.getString(1));
				app.setSize(c.getLong(2));
				app.setFormatedSize(c.getString(3));
				app.setName(c.getString(4));
				app.setPackageName(c.getString(5));
				app.setAppUrl(c.getString(8));
				app.setVersionCode(c.getInt(9));
				app.setVersionName(c.getString(10));
				long startPos = c.getLong(7);
				int state = c.getInt(11);
				
				AppDownloadTask task = new AppDownloadTask(app, startPos, saver, listener);
				task.setStatus(state);
				tasks.add(task);
			}
		}
		if (c != null) c.close();
		db.close();
		
		return tasks;
	}
	
	@Override
	public void removeAppTask(long appId) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = getWritableDatabase();
		db.delete(SimpleStateSaverDatabaseHelper.TABLE_APP_TASK, APP._ID + "=" + appId, null);
		db.close();
	}

	 private SQLiteDatabase getWritableDatabase(){
		return dbHelper.getWritableDatabase();
	 }
	 
	 private SQLiteDatabase getReadableDatabase(){
		 return dbHelper.getReadableDatabase();
	 }
}
