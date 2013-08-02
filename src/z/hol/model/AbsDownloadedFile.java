package z.hol.model;

import java.io.Serializable;

import z.hol.utils.FileUtils;
import android.text.TextUtils;

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
	private String formatedSize = null;
	private String url;
	private String name;
	
	private String mSavePath;
	
	public AbsDownloadedFile(){
		
	}
	
	public AbsDownloadedFile(AbsDownloadedFile file){
		id = file.id;
		mSub_id = file.mSub_id;
		size = file.size;
		formatedSize = file.formatedSize;
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
	
	public long getSubId(){
		return mSub_id;
	}
	
	public void setSubId(long id){
		this.mSub_id = id;
	}
	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getFormatedSize() {
		if (TextUtils.isEmpty(formatedSize)){
			formatedSize = FileUtils.formatFileSize(size);
		}
		return formatedSize;
	}
	public void setFormatedSize(String formatedSize) {
		this.formatedSize = formatedSize;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void setFileSavePath(String path){
		mSavePath = path;
	}

	public String getFileSavePath(){
		return mSavePath;
	}
	
}
