package z.hol.net.download.file;

import java.util.ArrayList;
import java.util.List;

import z.hol.db.CursorUtils;
import z.hol.model.SimpleFile;
import z.hol.net.download.AbsDownloadManager.Task;
import z.hol.net.download.ContinuinglyDownloader.DownloadListener;
import z.hol.net.download.SimpleStateSaverDatabaseHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SimpleFileStatusSaver implements FileStatusSaver{
	
	private SimpleStateSaverDatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	public SimpleFileStatusSaver(Context context) {
		// TODO Auto-generated constructor stub
		mDbHelper = new SimpleStateSaverDatabaseHelper(context);
		mDb = getWriteableDb();
	}

	@Override
	public SQLiteDatabase getWriteableDb() {
		// TODO Auto-generated method stub
		return mDbHelper.getWritableDatabase();
	}

	@Override
	public SQLiteDatabase getReadableDb() {
		// TODO Auto-generated method stub
		return mDbHelper.getWritableDatabase();
	}

	@Override
	public boolean isOpened() {
		// TODO Auto-generated method stub
		return mDb != null ? mDb.isOpen() : false;
	}

	@Override
	public void closeDb() {
		// TODO Auto-generated method stub
		if (mDb != null && mDb.isOpen()){
			mDb.close();
		}
	}

	@Override
	public void beginTransaction() {
		// TODO Auto-generated method stub
		mDb.beginTransaction();
	}

	@Override
	public void setTransactionSuccessful() {
		// TODO Auto-generated method stub
		mDb.setTransactionSuccessful();
	}

	@Override
	public void endTransaction() {
		// TODO Auto-generated method stub
		mDb.endTransaction();
	}

	@Override
	public void addTask(SimpleFile file, String saveFile) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put(File._ID, file.getId());
		values.put(File.URL, file.getUrl());
		values.put(File.START_POS, 0);
		values.put(File.STATE, Task.STATE_PAUSE);
		values.put(File.NAME, file.getName());
		values.put(File.LEN, file.getSize());
		values.put(File.LEN_FORMATED, file.getFormatedSize());

		if (saveFile == null){
			saveFile = file.getFileSavePath();
		}
		values.put(File.SAVE_FILE, saveFile);
		
		// commen datas
		// integes
		values.put(File.INT1, file.getLong1());
		values.put(File.INT2, file.getLong2());
		values.put(File.INT3, file.getLong3());
		values.put(File.INT4, file.getLong4());
		// strings
		values.put(File.DATA1, file.getData1());
		values.put(File.DATA2, file.getData2());
		values.put(File.DATA3, file.getData3());
		values.put(File.DATA4, file.getData4());
		values.put(File.DATA5, file.getData5());
		
		values.put(File.SUB_ID, file.getSubId());
		values.put(File.SUB_TYPE, file.getType());
		long addTime = System.currentTimeMillis();
		values.put(File.ADD_TIME, addTime);
		values.put(File.DONE_TIME, 0);
		
		file.setAddTime(addTime);

		mDb.insert(SimpleStateSaverDatabaseHelper.TABLE_FILE_TASK, null, values);
	}

	@Override
	public void changUrl(long id, String url) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put(File.URL, url);
		mDb.update(SimpleStateSaverDatabaseHelper.TABLE_FILE_TASK, values, File._ID + "=" + id, null);
	}

	@Override
	public void updateTaskSize(long id, long size) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put(File.LEN, size);
		mDb.update(SimpleStateSaverDatabaseHelper.TABLE_FILE_TASK, values, File._ID + "=" + id, null);
	}

	@Override
	public void updateDownloadPos(long id, long currentPos) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put(File.START_POS, currentPos);
		mDb.update(SimpleStateSaverDatabaseHelper.TABLE_FILE_TASK, values, File._ID + "=" + id, null);
	}

	@Override
	public void changeTaskState(long id, int state) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		values.put(File.STATE, state);
		mDb.update(SimpleStateSaverDatabaseHelper.TABLE_FILE_TASK, values, File._ID + "=" + id, null);	
	}

	@Override
	public void removeTask(long id) {
		// TODO Auto-generated method stub
		mDb.delete(SimpleStateSaverDatabaseHelper.TABLE_FILE_TASK, File._ID + "=" + id, null);	
	}


	@Override
	public FileDownloadTask getDownloadTask(long id, FileDownloadTask task) {
		// TODO Auto-generated method stub
		Cursor c = mDb.query(SimpleStateSaverDatabaseHelper.TABLE_FILE_TASK, FILE_PROJECTION, File._ID + "=" + id, null, null, null, null);
		if (c != null && c.moveToFirst()){
			SimpleFile file = task.getSimpeFile();
			long size = c.getLong(2);
			file.setSize(size);
			// 防止redirct后, url信息没更新
			file.setUrl(c.getString(1));
			task.setStartPos(c.getLong(6));
			task.setSize(size);
			c.close();
		}
		return task;
	}
	
//	File._ID,	// 0
//	File.URL,	// 1
//	File.LEN,	// 2
//	File.LEN_FORMATED,	// 3
//	File.STATE,	// 4
//	File.SAVE_FILE,	// 5
//	File.START_POS,	// 6
//	File.NAME,	// 7
//	File.INT1,	// 8
//	File.INT2,	// 9
//	File.INT3,	// 10
//	File.INT4,	// 11
//	File.DATA1,	// 12
//	File.DATA2,	// 13
//	File.DATA3,	// 14
//	File.DATA4,	// 15
//	File.DATA5	// 16
//	File.ADD_TIME,	// 17
//	File.DONE_TIME, // 18
//	File.SUB_ID, // 19
//	File.SUB_TYPE// 20
	
	@Override
	public List<FileDownloadTask> getDownloadTaskList(FileStatusSaver saver,
			DownloadListener listener) {
		// TODO Auto-generated method stub
		List<FileDownloadTask> tasks = new ArrayList<FileDownloadTask>();
		Cursor c = mDb.query(SimpleStateSaverDatabaseHelper.TABLE_FILE_TASK, FILE_PROJECTION, null, null, null, null, File._ID + " ASC");
		if (c != null){
			while (c.moveToNext()){
				SimpleFile file = new SimpleFile();
				long taskId = c.getLong(0);
				file.setId(taskId);
				file.setUrl(c.getString(1));
				long size = c.getLong(2);
				file.setSize(size);
				file.setFormatedSize(c.getString(3));
				file.setName(c.getString(7));

				file.setLong1(CursorUtils.getLong(c, 8));
				file.setLong2(CursorUtils.getLong(c, 9));
				file.setLong3(CursorUtils.getLong(c, 10));
				file.setLong4(CursorUtils.getLong(c, 11));
				file.setData1(CursorUtils.getString(c, 12));
				file.setData2(CursorUtils.getString(c, 13));
				file.setData3(CursorUtils.getString(c, 14));
				file.setData4(CursorUtils.getString(c, 15));
				file.setData5(CursorUtils.getString(c, 16));
				
				file.setSubId(c.getLong(19));
				file.setType(c.getInt(20));
				file.setAddTime(c.getLong(17));
				file.setDoneTime(c.getLong(18));

				String saveFile = c.getString(5);
				int state = c.getInt(4);
				long startPos = c.getLong(6);
				
				FileDownloadTask task = new FileDownloadTask(file, saveFile, startPos, saver, listener);
				task.setStatus(state);
				task.setSize(size);
				task.setTaskId(taskId);
				tasks.add(task);
			}
		}
		if (c != null) c.close();
		return tasks;
	}

}
