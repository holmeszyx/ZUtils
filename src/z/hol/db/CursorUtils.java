package z.hol.db;

import android.database.Cursor;

public class CursorUtils {
	
	public static int getInt(Cursor c, int index){
		if (!c.isNull(index)){
			return c.getInt(index);
		}
		return 0;
	}
	
	public static int getInt(Cursor c, int index, int defaultValue){
		if (!c.isNull(index)){
			return c.getInt(index);
		}
		return defaultValue;
	}
	
	public static long getLong(Cursor c, int index){
		if (!c.isNull(index)){
			return c.getLong(index);
		}
		return 0l;
	}
	
	public static long getLong(Cursor c, int index, long defaultValue){
		if (!c.isNull(index)){
			return c.getLong(index);
		}
		return defaultValue;
	}
	
	public static String getString(Cursor c, int index){
		if (!c.isNull(index)){
			return c.getString(index);
		}
		return null;
	}
}
