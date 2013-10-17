package z.hol.net.download.task;

import z.hol.model.SimpleApp;

/**
 * 应用下载Task
 * @author holmes
 *
 */
public interface AppTask extends FileTask{

	/**
	 * 得到一个App
	 * @return
	 */
	public SimpleApp getApp();
}
