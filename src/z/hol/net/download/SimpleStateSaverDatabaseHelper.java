package z.hol.net.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleStateSaverDatabaseHelper extends SQLiteOpenHelper{
	public static final int VERSION = 3;
	public static final String DATABASE = "download_status.db";
	public static final String TABLE_APP_TASK = "app_download_task";
	public static final String TABLE_FILE_TASK = "file_download_task";
	
	public SimpleStateSaverDatabaseHelper(Context context){
		this(context, DATABASE, null, VERSION);
	}

	public SimpleStateSaverDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		 db.execSQL("CREATE TABLE IF NOT EXISTS app_download_task" +
			 		"( _id INTEGER PRIMARY KEY, len INTEGER, " +
			 		"len_formated TEXT, state INTEGER, url TEXT, save_file TEXT, start_pos INTEGER, " +
			 		"pkg TEXT, name TEXT, ver_name TEXT, ver_code INTEGER, icon TEXT," +
			 		" data1 TEXT, data2 TEXT, data3 TEXT" +	
			 		")"
			 		);
		 
		 db.execSQL("CREATE TABLE IF NOT EXISTS file_download_task" +
			 		"( _id INTEGER PRIMARY KEY, len INTEGER, " +
			 		"len_formated TEXT, state INTEGER, url TEXT, save_file TEXT, start_pos INTEGER, " +
			 		"name TEXT, _int1 INTEGER, _int2 INTEGER," +
			 		" data1 TEXT, data2 TEXT, data3 TEXT, data4 TEXT, data5 TEXT" +
			 		")"
				 );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		// db.execSQL("DROP TABLE IF EXISTS app_download_task");
		// onCreate(db);
		if (oldVersion == 1){
			 db.execSQL("CREATE TABLE IF NOT EXISTS file_download_task" +
				 		"( _id INTEGER PRIMARY KEY, len INTEGER, " +
				 		"len_formated TEXT, state INTEGER, url TEXT, save_file TEXT, start_pos INTEGER, " +
				 		"name TEXT, _int1 INTEGER, _int2 INTEGER," +
				 		" data1 TEXT, data2 TEXT, data3 TEXT, data4 TEXT, data5 TEXT" +
				 		")"
				 		);
		}else if (oldVersion == 2){
			db.execSQL("DROP TABLE app_download_task");
			db.execSQL("CREATE TABLE IF NOT EXISTS app_download_task" +
				 		"( _id INTEGER PRIMARY KEY, len INTEGER, " +
				 		"len_formated TEXT, state INTEGER, url TEXT, save_file TEXT, start_pos INTEGER, " +
				 		"pkg TEXT, name TEXT, ver_name TEXT, ver_code INTEGER, icon TEXT," +
				 		" data1 TEXT, data2 TEXT, data3 TEXT" +	
				 		")"
				 		);
		}
	}

}
