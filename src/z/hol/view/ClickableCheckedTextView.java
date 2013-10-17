package z.hol.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckedTextView;

/**
 * 自带选择事件的CheckedTextView<br>
 * 可以像普通CheckedBox 来使用，但文字将显示在选择框的左边
 * @author holmes
 *
 */
public class ClickableCheckedTextView extends CheckedTextView{
	private OnCheckedChangeListener mCheckedChange;

	public ClickableCheckedTextView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	public ClickableCheckedTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	
	public ClickableCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		this.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ClickableCheckedTextView.this.toggle();
				if (mCheckedChange != null){
					mCheckedChange.onCheckedChanged(ClickableCheckedTextView.this, ClickableCheckedTextView.this.isChecked());
				}
			}
		});
	}
	
	public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener){
		this.mCheckedChange = onCheckedChangeListener;
	}
	
	public interface OnCheckedChangeListener{
		public void onCheckedChanged(ClickableCheckedTextView view, boolean isCheck);
	}

}
