package net.chengbo.cbdroid.content;

import java.io.IOException;

import net.chengbo.cbdroid.BizException;
import android.os.AsyncTask;
import android.util.Log;

public abstract class CBContentResolver<T> {
	private final static String LOG_TAG = "CBContentResolver";

	private boolean mIsLoading;
	private boolean mHasError;
	private String mErrorCode;
	private String mErrorDescription;
	private Exception mException;

	private ContentObserver mObserver;

	/**
	 * 子类应重写此方法，实现自己不同的查询逻辑。
	 * 
	 * @return 查询得到的业务实体。
	 * @throws IOException
	 * @throws BizException
	 */
	public abstract T query() throws IOException, BizException;

	/**
	 * 子类可重写此方法，实现自己不同的加载后逻辑。此方法是在{@link ContentObserver}触发
	 * {@link ContentObserver#onChanged()}事件之前执行。
	 * 
	 * @see #registerContentObserver(ContentObserver)
	 * @see #onPostLoaded(Object)
	 * @param result
	 *            异步加载得到的数据
	 */
	public void onLoaded(T result) {

	}

	/**
	 * 子类可重写此方法，实现自己不同的加载后逻辑。此方法是在{@link ContentObserver}触发
	 * {@link ContentObserver#onChanged()}事件之后执行。
	 * 
	 * @see #registerContentObserver(ContentObserver)
	 * @see #onLoaded(Object)
	 * @param result
	 *            异步加载得到的数据
	 */
	public void onPostLoaded(T result) {

	}

	/**
	 * 子类可重写此方法，实现自己不同的错误处理逻辑。
	 * 
	 * @param code
	 *            错误码
	 * @param description
	 *            错误描述
	 */
	public void onError(String code, String description) {

	}

	/**
	 * 获取当前的加载状态。
	 * 
	 * @return {@code true}为正在加载；反之为{@code false}。
	 */
	public boolean isLoading() {
		return mIsLoading;
	}

	/**
	 * 获取当前是否有错误发生。
	 * 
	 * @return {@code true}为有错误发生；反之为{@code false}。
	 */
	public boolean hasError() {
		return mHasError;
	}

	/**
	 * 获取异常错误码。
	 * 
	 * @return 异常错误码。
	 */
	public String getErrorCode() {
		return mErrorCode;
	}

	/**
	 * 获取异常错误描述。
	 * 
	 * @return 异常描述。
	 */
	public String getErrorDescription() {
		return mErrorDescription;
	}

	/**
	 * 获取异常。
	 * 
	 * @return 异常实例。
	 */
	public Exception getException() {
		return mException;
	}

	/**
	 * 开始异步查询数据。
	 */
	public void startQuery() {
		mHasError = false;
		mIsLoading = true;
		if (mObserver != null) {
			mObserver.onChanged();
		}

		AsyncTask<Void, Void, T> task = new AsyncTask<Void, Void, T>() {

			@Override
			protected T doInBackground(Void... params) {
				try {
					return query();
				} catch (IOException e) {
					mException = e;
					mHasError = true;
					mErrorDescription = "网络错误，请稍后再试。";
					Log.e(LOG_TAG, e.toString());
				} catch (BizException e) {
					mException = e;
					mHasError = true;
					mErrorCode = e.getCode();
					mErrorDescription = e.getDescription();
				}

				return null;
			}

			@Override
			protected void onPostExecute(T result) {
				if (mHasError) {
					onError(mErrorCode, mErrorDescription);
				} else {
					onLoaded(result);
				}
				mIsLoading = false;
				if (mObserver != null) {
					mObserver.onChanged();
				}
				if (!mHasError) {
					onPostLoaded(result);
				}
			}
		};

		task.execute();
	}

	/**
	 * 注册一个{@link ContentObserver}，当开始加载，加载错误，加载完毕时，它会得到通知。
	 * 
	 * @param observer
	 *            注册的{@link ContentObserver}。
	 */
	public void registerContentObserver(ContentObserver observer) {
		mObserver = observer;
	}
}