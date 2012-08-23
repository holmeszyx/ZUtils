package z.hol.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 单行GridView
 * @author holmes
 *
 */
public class OneRowGridView extends GridView{
    private int mInternalRequestedColumnWidth;
    
    
	
	public OneRowGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public OneRowGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public OneRowGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    public void setColumnWidth(int columnWidth) {
        super.setColumnWidth(columnWidth);
        if (mInternalRequestedColumnWidth != columnWidth) {
            mInternalRequestedColumnWidth = columnWidth;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mInternalRequestedColumnWidth != 0) {
            int n = (getAdapter() == null) ? 0 : getAdapter().getCount();
            int size = mInternalRequestedColumnWidth * n;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
