package z.hol.utils;

/**
 * 32位的bitmap数据结构
 * @author holmes
 *
 */
public class IntBitSet {

	private int mBitpool;
	
	public IntBitSet() {
		// TODO Auto-generated constructor stub
		this(0);
	}
	
	public IntBitSet(int bitpool){
		mBitpool = bitpool;
	}
	
	/**
	 * 设置bit位的状态为true
	 * @param index
	 */
	public void set(int index){
		set(index, true);
	}
	
	/**
	 * 设置一个bit位的状态值
	 * @param index
	 * @param state
	 */
	public void set(int index, boolean state){
		if (index < 0){
			throw new IndexOutOfBoundsException("index < 0 : " + index);
		}
		if (state){
			internalSetTrue(index);
		}else{
			internalSetFalse(index);
		}
	}
	
	/**
	 * 翻转bit位
	 * @param index
	 */
	public void flip(int index){
		if (index < 0){
			throw new IndexOutOfBoundsException("index < 0 : " + index);
		}
		mBitpool = mBitpool ^ (1 << index);
	}
	
	/**
	 * 获取一个bit位的状态
	 * @param index
	 * @return
	 */
	public boolean get(int index){
		if (index < 0){
			throw new IndexOutOfBoundsException("index < 0 : " + index);
		}
		return (mBitpool & 1 << index) != 0;
	}
	
	/**
	 * 获取bit池的值
	 * @return
	 */
	public int getValue(){
		return mBitpool;
	}
	
	/**
	 * 清空所有bit位
	 */
	public void clean(){
		mBitpool = 0;
	}
	
	private void internalSetTrue(int index){
		mBitpool = mBitpool | 1 << index;
	}
	
	private void internalSetFalse(int index){
		mBitpool = mBitpool & ~(1 << index);
	}
}
