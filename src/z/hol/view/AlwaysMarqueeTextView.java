package z.hol.view;

import android.content.Context;
import android.graphics.Rect;
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
		init();
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AlwaysMarqueeTextView(Context context) {
		super(context);
		init();
	}
	
	private void init(){
		setSingleLine(true);
		setEllipsize(TruncateAt.MARQUEE);
		setMarqueeRepeatLimit(-1);
	}
	
	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
	    if(focused)
	        super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		// TODO Auto-generated method stub
		   if(hasWindowFocus)
		        super.onWindowFocusChanged(hasWindowFocus);
	}

	@Override
	public boolean isFocused() {
		//return super.isFocused();
		return true;
	}
}
