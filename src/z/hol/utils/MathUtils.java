package z.hol.utils;

public class MathUtils {

	/**
	 * 约束给定数值，使其值在一个界限(min <= num <= max)中.
	 * @param num	要约束的值
	 * @param min	最小值
	 * @param max	最大值
	 * @return	如果num < min 则返回 min,如果num > max 则返回 max，否则直接返回 num
	 */
	public static int constrain(int num, int min, int max){
		int i = num;
		if (i < min){
			i = min;
		}else if (i > max){
			i = max;
		}
		return i;
	}
}
