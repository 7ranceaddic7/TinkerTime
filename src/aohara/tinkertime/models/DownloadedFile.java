package aohara.tinkertime.models;

import java.net.URL;
import java.util.Date;

public class DownloadedFile implements UpdateListener {
	
	private String fileName;
	private Date lastUpdatedOn;
	private URL downloadLink, pageUrl;
	
	public DownloadedFile(String newestFileName, Date lastUpdated, URL pageUrl){
		update(newestFileName, lastUpdated, pageUrl);
	}

	public String getNewestFileName() {
		return fileName;
	}

	public Date getUpdatedOn() {
		return lastUpdatedOn;
	}

	public URL getDownloadLink() {
		return downloadLink;
	}

	public URL getPageUrl() {
		return pageUrl;
	}
	
	public void update(String newestFileName, Date lastUpdated, URL pageUrl){
		this.fileName = newestFileName;
		this.lastUpdatedOn =lastUpdated;
		this.pageUrl = pageUrl;
	}
	
	@Override
	public boolean equals(Object o){
		return (
			o instanceof DownloadedFile
			&& ((DownloadedFile)o).getPageUrl().equals(getPageUrl())
		);
	}

	@Override
	public void setUpdateAvailable(URL pageUrl, String newestFileName) {
		// No action
	}
}