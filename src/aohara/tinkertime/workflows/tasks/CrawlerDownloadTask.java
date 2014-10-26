package aohara.tinkertime.workflows.tasks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import aohara.common.workflows.Workflow;
import aohara.common.workflows.Workflow.WorkflowTask;
import aohara.common.workflows.tasks.FileTransferTask;
import aohara.tinkertime.crawlers.Crawler;
import aohara.tinkertime.workflows.ModWorkflowBuilder.ModDownloadType;

public class CrawlerDownloadTask extends WorkflowTask {
	
	private final Crawler<?> crawler;
	private final ModDownloadType type;
	private final Path dest;
	
	public CrawlerDownloadTask(Crawler<?> crawler, ModDownloadType type, Path dest){
		this.crawler = crawler;
		this.type = type;
		this.dest = dest;
	}
	
	private URL getUrl() throws IOException{
		switch(type){
		case Page: return crawler.getApiUrl();
		case File: return crawler.getDownloadLink();
		case Image: return crawler.getImageUrl();
		default: return null;
		}
	}

	@Override
	public int getTargetProgress() throws IOException {
		return getUrl().openConnection().getContentLength();
	}

	@Override
	public String getTitle() {
		URL url = null;
		try {
			url = getUrl();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return String.format("Crawler is Downloading %s", url);
	}

	@Override
	public boolean call(Workflow workflow) throws IOException, URISyntaxException {		
		return new FileTransferTask(getUrl().toURI(), dest).call(workflow);
	}
}
