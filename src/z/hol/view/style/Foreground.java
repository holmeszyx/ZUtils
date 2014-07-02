package z.hol.view.style;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * TextView，通过正则匹配，来改变TextView中符合的文字的颜色<br>
 * 主要用于类似SNS的消分块，如 “@ ##”等等 
 * @author holmes
 *
 */
public class Foreground {

	public static final Pattern SNS_AT_PATTERN = Pattern.compile("@.*?\\s");
	public static final Pattern CALL_LOG_LABEL_PATTERN = Pattern.compile("\\(.+\\)");
	public static final Pattern COUNT_LABEL_PATTERN = Pattern.compile("\\(\\d+\\)$");
	
	/**
	 * 添加文字前景色（文字颜色）
	 * @param textView 要改变颜色的textView
	 * @param pattern 正则
	 * @param color 要加的颜色值
	 * @return 如果有需要改变的颜色则为true
	 */
	public static boolean addForeground(TextView textView, Pattern pattern, int color){
		return addForeground(textView, pattern, color, null);
	}
	
	/**
	 * 添加文字前景色（文字颜色）
	 * @param textView 要改变颜色的textView
	 * @param pattern 正则
	 * @param color 要加的颜色值
	 * @param matchFilter 正则配置的二重过滤
	 * @return 如果有需要改变的颜色则为true
	 */
	public static boolean addForeground(TextView textView, Pattern pattern, int color, Linkify.MatchFilter matchFilter){
		SpannableString ss = SpannableString.valueOf(textView.getText());
		boolean hasMatches = false;
		if (addForeground(ss, pattern, color, matchFilter)){
			textView.setText(ss);
			hasMatches = true;
		}
		return hasMatches;
	}
	
	/**
	 * 添加文字前景色（文字颜色）
	 * @param s 整体文字块
	 * @param pattern 正则
	 * @param color 要加的颜色
	 * @return 如果有需要改变的颜色则为true
	 */
	public static boolean addForeground(Spannable s, Pattern pattern, int color){
		return addForeground(s, pattern, color, null);
	}
	
	/**
	 * 
	 * 添加文字前景色（文字颜色）
	 * @param s 整体文字块
	 * @param pattern 正则
	 * @param matchFilter 正则配置的二重过滤
	 * @return 如果有需要改变的颜色则为true
	 */
	public static boolean addForeground(Spannable s, Pattern pattern, int color, Linkify.MatchFilter matchFilter){
		boolean hasMatches = false;
		Matcher m = pattern.matcher(s);
		while(m.find()){
			boolean allowed = true;
			int start = m.start();
			int end = m.end();
			if (matchFilter != null){
				allowed = matchFilter.acceptMatch(s, start, end);
			}
			
			if (allowed){
				applyForeground(s, start, end, color);
				hasMatches = true;
			}
		}
		return hasMatches;
	}
	
	/**
	 * 应用改变颜色
	 * @param s 整体文字块
	 * @param start 需要变色的文字起始位置
	 * @param end 需要变色的文字结束位置
	 * @param color 要改成的颜色
	 */
	public static void applyForeground(Spannable s, int start, int end, int color){
		ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(color);
		s.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	} 
}
