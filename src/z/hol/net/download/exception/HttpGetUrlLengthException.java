package z.hol.net.download.exception;

/**
 * 获取Url服务器异常
 * @author holmes
 *
 */
public class HttpGetUrlLengthException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5979789125474339514L;
	
	private final int mStatusCode;

	public HttpGetUrlLengthException(int statusCode) {
		super();
		// This is Auto-generated constructor stub
		mStatusCode = statusCode;
	}

	public HttpGetUrlLengthException(int statusCode, String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// This is Auto-generated constructor stub
		mStatusCode = statusCode;
	}

	public HttpGetUrlLengthException(int statusCode, String detailMessage) {
		super(detailMessage);
		// This is Auto-generated constructor stub
		mStatusCode = statusCode;
	}

	public HttpGetUrlLengthException(int statusCode, Throwable throwable) {
		super(throwable);
		// This is Auto-generated constructor stub
		mStatusCode = statusCode;
	}

	
	/**
	 * 获取服务器返回的状态码
	 * @return
	 */
	public int getHttpStatusCode(){
		return mStatusCode;
	}
}
