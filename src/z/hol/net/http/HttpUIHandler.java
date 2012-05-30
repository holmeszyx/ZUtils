package z.hol.net.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


/**
 * 主要用于http通信过程中与UI发送状态信息<br />
 * 重写回调，工作线程中使用回调对应的发送方法，来让本handler的线程执行回调。
 * 
 * @author holmes
 */
public abstract class HttpUIHandler extends Handler implements HttpProgress{
	public static final int HTTP_START = 0x60000;
	public static final int HTTP_END = 0x60001;
	public static final int HTTP_ERROR = 0x60002;
	
	public static final int ERROR_CODE_NO_RESPONSE = -1;
	public static final int ERROR_CODE_NO_ERROR = 0;
	public static final int ERROR_CODE_SERVER_DATA_PARSE_ERROR = -10;
	
	public int errorCode = 0;
	
	public Object obj;
	
	
	
	public HttpUIHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HttpUIHandler(Callback callback) {
		super(callback);
		// TODO Auto-generated constructor stub
	}

	public HttpUIHandler(Looper looper, Callback callback) {
		super(looper, callback);
		// TODO Auto-generated constructor stub
	}

	public HttpUIHandler(Looper looper) {
		super(looper);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		switch(msg.what){
		case HTTP_START:
			this.onHttpStart();
			break;
		case HTTP_END:
			this.onHttpEnd(msg.obj, errorCode);
			break;
		case HTTP_ERROR:
			this.onHttpError(errorCode);
			break;
		}
		
	}
	
	/**
	 * 发送http 开始
	 */
	public void httpStart(){
		this.sendEmptyMessage(HTTP_START);
	}
	
	/**
	 * 发送http 结束
	 * @param obj
	 */
	public void httpEnd(Object obj){
		Message msg = obtainMessage(HTTP_END, obj);
		this.sendMessage(msg);
	}
	
	/**
	 * 发送http 结束
	 */
	public void httpEnd(){
		httpEnd(obj);
	}
		
	/**
	 * 发送http 出错，主要是有异常，服务器无响应
	 * @param errorCode
	 */
	public void httpError(int errorCode){
		this.errorCode = errorCode;
		this.sendEmptyMessage(HTTP_ERROR);
	}
	
	/**
	 * 发送http 出错，主要是有异常，服务器无响应
	 */
	public void httpError(){
		this.errorCode = ERROR_CODE_NO_RESPONSE;
		this.sendEmptyMessage(HTTP_ERROR);
	}
}
