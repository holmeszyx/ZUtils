package z.hol.general;

/**
 * 用于分析处理性能
 * @author holmes
 *
 */
public class CC {
	public static final long SECEND = 1000;

	private long mStartTime = 0;
	private long mEndTime = 0;
	private static ThreadLocal<CC> localCC = new ThreadLocal<CC>(){
		
		@Override
		protected CC initialValue() {
			return new CC();
		}
	};
	
	public CC(long start){
		mStartTime = start;
	}
	
	public CC(){
		this(0);
	}
	
	public static CC obtaion(){
		return localCC.get();
	}
	
	/**
	 * 记录开始点
	 */
	public void start(){
		mStartTime = System.currentTimeMillis();
	}
	
	/**
	 * 记录结束点
	 */
	public void end(){
		mEndTime = System.currentTimeMillis();
	}
	
	/**
	 * 计算性能开销
	 * @return
	 */
	public long cost(){
		return mEndTime - mStartTime;
	}
	
	/**
	 * 带单位的性能开销，毫秒
	 * @return 类似 942ms
	 */
	public String costStr(){
		return cost() + "ms";
	}
}
