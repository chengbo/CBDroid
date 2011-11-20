package net.chengbo.cbdroid;

public class BizException extends Exception {

	private static final long serialVersionUID = 3415862155080794694L;
	
	private String mCode;
	private String mDescription;

	public BizException(String code, String description) {
		mCode = code;
		mDescription = description;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return mCode;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return mDescription;
	}

}