package z.hol.net.download;

import z.hol.model.RecommendedApp;
import z.hol.net.download.AbsDownloadManager.Task;
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
	public void addAppDownload(RecommendedApp app) {
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
		 if (c!= null && c.moveToFirst()){
			 RecommendedApp app = task.getApp();
			 app.setSize(c.getLong(2));
			 task.setStartPos(c.getLong(7));
		 }
		 if (c != null) c.close();
		 db.close();
		 
		return task;
	}

	 private SQLiteDatabase getWritableDatabase(){
		return dbHelper.getWritableDatabase();
	 }
	 
	 private SQLiteDatabase getReadableDatabase(){
		 return dbHelper.getReadableDatabase();
	 }
}
