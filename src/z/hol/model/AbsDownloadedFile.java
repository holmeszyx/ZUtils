package z.hol.model;

/**
 * 被下载的文件 
 * @author holmes
 *
 */
public abstract class AbsDownloadedFile {

	private long id;
	private long size;
	private String formatedSize = null;
	private String url;
	private String name;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getFormatedSize() {
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
	
	
}
