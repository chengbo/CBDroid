package net.chengbo.cbdroid.content;

import net.chengbo.cbdroid.BizException;
import net.chengbo.cbdroid.R;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public final class StateObserver {
	private View mContentView;
	private View mLoadingView;
	private View mErrorView;

	@SuppressWarnings("rawtypes")
	private CBContentResolver mResolver;

	private final Observer mObserver = new Observer();

	public void setView(View parent, int contentViewId, int loadingViewId, int errorViewId) {
		if (parent != null) {
			mLoadingView = parent.findViewById(loadingViewId);
			mErrorView = parent.findViewById(errorViewId);
			mErrorView.findViewById(R.id.retry).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mResolver.startQuery();
				}

			});
			mContentView = parent.findViewById(contentViewId);
		}
	}

	public void setResolver(@SuppressWarnings("rawtypes") CBContentResolver resolver) {
		mResolver = resolver;
		mResolver.registerContentObserver(mObserver);
	}

	private void updateViews() {
		setViewVisible(mLoadingView, false);
		setViewVisible(mErrorView, false);
		setViewVisible(mContentView, false);

		if (mResolver.isLoading()) {
			setViewVisible(mLoadingView, true);
		} else {
			if (mResolver.hasError()) {
				setViewVisible(mErrorView, true);
				if (mErrorView instanceof ViewGroup) {
					ViewGroup group = (ViewGroup) mErrorView;
					setErrorDescription(group);
				}
			} else {
				setViewVisible(mContentView, true);
			}
		}
	}

	private void setErrorDescription(ViewGroup viewGroup) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			View child = viewGroup.getChildAt(i);

			if (child instanceof TextView) {
				if (child instanceof Button) {
					if (mResolver.getException() instanceof BizException) {
						child.setVisibility(View.GONE); // BizException重试是没有意义的，所以隐藏掉
					}
				} else {
					((TextView) child).setText(mResolver.getErrorDescription());
				}
			} else if (child instanceof ViewGroup) {
				setErrorDescription((ViewGroup) child);
			}
		}
	}

	private static synchronized void setViewVisible(View view, boolean visible) {
		if (view != null) {
			int visibility = visible ? View.VISIBLE : View.GONE;
			if (view != null && visibility != view.getVisibility()) {
				view.setVisibility(visibility);
			}
		}
	}

	private class Observer implements ContentObserver {

		@Override
		public void onChanged() {
			updateViews();
		}

	}
}