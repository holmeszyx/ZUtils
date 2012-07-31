package z.hol.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * View切换容器
 * @author holmes
 *
 */
public class FrameContainer extends FrameLayout{
	
	private View[] mChilderView;
	private int mCount = 0;
	private int mCurrentViewIndex = 0;

	public FrameContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public FrameContainer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public FrameContainer(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mCount = getChildCount();
		mChilderView = new View[mCount];
		for (int i = 0; i < mCount; i ++){
			View c = getChildAt(i);
			mChilderView[i] = c;
			c.setVisibility(View.GONE);
		}
		if (isInEditMode()){
			showChild(mCurrentViewIndex);
		}
		//showChild(mCurrentViewIndex);
	}
	
	public void switchChild(int index){
		showChild(index);
	}
	
	public void switchNext(){
		int next = getCurrentChlidIndex() + 1;
		if (next >= mCount) next = 0;
		switchChild(next);
	}
	
	public View getCurrentChildView(){
		return mChilderView[mCurrentViewIndex];
	}
	
	public int getCurrentChlidIndex(){
		return mCurrentViewIndex;
	}
	
	private void showChild(int index){
		if (index < 0 || index > mCount - 1 ){
			return;
		}
		if (index != mCurrentViewIndex){
			getCurrentChildView().setVisibility(View.GONE);
			mCurrentViewIndex = index;
			getCurrentChildView().setVisibility(View.VISIBLE);
		}else{
			if (getCurrentChildView().getVisibility() != View.VISIBLE){
				getCurrentChildView().setVisibility(View.VISIBLE);
			}
		}
		getCurrentChildView().requestFocus();
		getCurrentChildView().requestFocusFromTouch();
	}
	
}
