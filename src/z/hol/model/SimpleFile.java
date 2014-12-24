package z.hol.model;

import java.io.Serializable;

/**
 * 子类重写 {@link #getFileSavePath()}
 * 有data1 ~ data5 五个字符串的通用字段.
 * 有long1 ~ long4 四个整形的通用字段.
 * 通用字段会自动保存在数据库中。从下载管理里面拿出来的通用字段
 * 不会丢失.
 * @author holmes
 *
 */
public class SimpleFile extends AbsDownloadedFile implements Serializable{
	public static final int TYPE_NORMAL_FILE = 1;
	public static final int TYPE_NORMAL_APP = 2;
	public static final int TYPE_FILE1 = 3;
	public static final int TYPE_FILE2 = 4;
	public static final int TYPE_FILE3 = 5;

	private static final long serialVersionUID = 538835497514263724L;
	
	protected long long1, long2, long3, long4;
	protected String data1, data2, data3, data4, data5;

	private int mType;
	private long mAddTime;
	private long mDoneTime;
	
	private long mStartPos = -1;
	
	public SimpleFile(){
		
	}
	
	public SimpleFile(SimpleFile file){
		super(file);

		long1 = file.long1;
		long2 = file.long2;
		long3 = file.long3;
		long4 = file.long4;
		
		data1 = file.data1;
		data2 = file.data2;
		data3 = file.data3;
		data4 = file.data4;
		data5 = file.data5;
		
		mType = file.mType;
		mAddTime = file.mDoneTime;
		mDoneTime = file.mDoneTime;
		
		mStartPos = file.mStartPos;
	}
	
	/**
	 * 下载任务类型.
	 * 一般与subId组成唯一性
	 * @return
	 */
	public int getType(){
		return mType;
	}
	
	public void setType(int type){
		mType = type;
	}
	
	/**
	 * 任务的添加时间
	 * @return
	 */
	public long getAddTime(){
		return mAddTime;
	}
	
	public void setAddTime(long time){
		mAddTime = time;
	}
	
	/**
	 * 任务的完成时间
	 * @return
	 */
	public long getDoneTime(){
		return mDoneTime;
	}
	
	public void setDoneTime(long time){
		mDoneTime = time;
	}
	
	/**
	 * 任务下载开始断点
	 * @param pos
	 */
	public void setStartPos(long pos){
		mStartPos = pos;
	}

	public long getStartPos(){
		return mStartPos;
	}

	public int getInt1() {
		return (int) long1;
	}


	public void setInt1(int int1) {
		this.long1 = int1;
	}


	public int getInt2() {
		return (int) long2;
	}


	public void setInt2(int int2) {
		this.long2 = int2;
	}


	public int getInt3() {
		return (int) long3;
	}


	public void setInt3(int int3) {
		this.long3 = int3;
	}


	public int getInt4() {
		return (int) long4;
	}


	public void setInt4(int int4) {
		this.long4 = int4;
	}

	public long getLong1() {
		return long1;
	}

	public void setLong1(long long1) {
		this.long1 = long1;
	}

	public long getLong2() {
		return long2;
	}

	public void setLong2(long long2) {
		this.long2 = long2;
	}

	public long getLong3() {
		return long3;
	}

	public void setLong3(long long3) {
		this.long3 = long3;
	}

	public long getLong4() {
		return long4;
	}

	public void setLong4(long long4) {
		this.long4 = long4;
	}

	public String getData1() {
		return data1;
	}


	public void setData1(String data1) {
		this.data1 = data1;
	}


	public String getData2() {
		return data2;
	}


	public void setData2(String data2) {
		this.data2 = data2;
	}


	public String getData3() {
		return data3;
	}


	public void setData3(String data3) {
		this.data3 = data3;
	}


	public String getData4() {
		return data4;
	}


	public void setData4(String data4) {
		this.data4 = data4;
	}


	public String getData5() {
		return data5;
	}


	public void setData5(String data5) {
		this.data5 = data5;
	}
	
	
}
