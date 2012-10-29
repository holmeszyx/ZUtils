package z.hol.view.test;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

public class UITestUtils {
	
	public String showMargins(View v){
		if (v.getLayoutParams() instanceof MarginLayoutParams){
			return showMargins((MarginLayoutParams) v.getLayoutParams());
		}
		return null;
	}

	public String showMargins(MarginLayoutParams layoutParams){
		String msg = null;
		msg = String.format("margin: left %d, top %d. right %d, bottom %d", 
				layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
		System.out.println(msg);
		return msg;
	}
	
	
}
