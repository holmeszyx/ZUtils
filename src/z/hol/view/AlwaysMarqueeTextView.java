package z.hol.view;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 跑马灯的TextView
 * @author holmes
 *
 */
public class AlwaysMarqueeTextView extends TextView{

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public AlwaysMarqueeTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	
	private void init(){
		setSingleLine(true);
		setEllipsize(TruncateAt.MARQUEE);
	}

	@Override
	public boolean isFocused() {
		// TODO Auto-generated method stub
		//return super.isFocused();
		return true;
	}
}
