package z.hol.model;

import java.io.Serializable;

/**
 * 被下载的文件的基本信息,
 * 文件的id, size, 下载url, name
 * @author holmes
 *
 */
public abstract class AbsDownloadedFile implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8595623302597197686L;
	
	private long id;
	private long mSub_id;
	private long size;
	private String formattedSize = null;
	private String url;
	private String name;
	
	private String mSavePath;
	
	public AbsDownloadedFile(){
		
	}
	
	public AbsDownloadedFile(AbsDownloadedFile file){
		id = file.id;
		mSub_id = file.mSub_id;
		size = file.size;
		formattedSize = file.formattedSize;
		url = file.url;
		name = file.name;
		mSavePath = file.mSavePath;
	}
	
	/**
	 * 和task id相同,
	 * 如果需要独立的id, 请使用 {@link #getSubId()} 以及 {@link #setSubId(long)}
	 * @return
	 */
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * 获取子id.一般配合 type.组成唯一性
	 * @return
	 */
	public long getSubId(){
		return mSub_id;
	}
	
	public void setSubId(long id){
		this.mSub_id = id;
	}
	
	/**
	 * 下载的文件大小.
	 * @return 在没有成功获取文件大小时，可能为0
	 */
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	/**
	 * 获取格式化后的文件大小
	 * @return
	 */
	public String getFormattedSize() {
		return formattedSize;
	}
	
	public void setFormattedSize(String formatedSize) {
		this.formattedSize = formatedSize;
	}
	
	/**
	 * 下载的url
	 * @return
	 */
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * 下载的任务名
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 下载文件的存储路径
	 * @param path
	 */
	public void setFileSavePath(String path){
		mSavePath = path;
	}

	public String getFileSavePath(){
		return mSavePath;
	}
	
}
