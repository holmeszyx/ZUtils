package z.hol.utils;

/**
 * 字数剩余处理类
 * @author holmes
 *
 */
public class TextRemain {
	
	private int mLimit = 0;
	private int mCount = 0;
	private int mPage = 1;
	
	
	/**
	 * 得到限制数
	 * @return
	 */
	public int getLimit() {
		return mLimit;
	}

	/**
	 * 设置限制数<br />
	 * 注意设置完这个值后不会马上改变页数。要改变页数，可以再制造refresh()
	 * 
	 * @param limit
	 */
	public void setLimit(int limit) {
		if (limit < 0){
			throw new IllegalArgumentException("you limit is " + limit + " ,But limit must be more than or equal to 0");
		}
		this.mLimit = limit;
	}

	/**
	 * 得到总数
	 * @return
	 */
	public int getCount() {
		return mCount;
	}

	/**
	 * 设置总数<br />
	 * 注意，设置完这个值后会立即刷新当前的页数
	 * 
	 * @param count
	 */
	public void setCount(int count) {
		this.mCount = count;
		setPageCount();
	}

	/**
	 * 得到当前页剩余数
	 * @return
	 */
	public int getRemain(){	
		return mPage * mLimit - mCount;
	}
	
	/**
	 * 得到剩余数，与页数无关。直接得到与限制的差数
	 * @return
	 */
	public int getRawRemain(){
		return mLimit - mCount;
	}
	
	/**
	 * 得到页数,向上取整
	 * @return
	 */
	public int getPage(){
		if (mPage < 1){
			mPage = 1;
		}
		return mPage;
	}
	
	/**
	 * 刷新数据
	 */
	public void refresh(){
		setPageCount();
	}
	
	/**
	 * 设置分页数
	 */
	private void setPageCount(){
		if (mLimit == 0){
			//除数不能为0
			return;
		}
		double dPage = (double) mCount/ (double) mLimit;
		dPage = Math.ceil(dPage);
		mPage = (int) dPage;
		if (mPage < 1){
			mPage = 1;
		}
	}

}
