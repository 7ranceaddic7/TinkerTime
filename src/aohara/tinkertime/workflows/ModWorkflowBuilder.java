package aohara.tinkertime.workflows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import thirdParty.ZipNode;
import aohara.common.workflows.ConflictResolver;
import aohara.common.workflows.WorkflowBuilder;
import aohara.common.workflows.tasks.UnzipTask;
import aohara.tinkertime.TinkerConfig;
import aohara.tinkertime.controllers.ModStateManager;
import aohara.tinkertime.crawlers.CrawlerFactory.UnsupportedHostException;
import aohara.tinkertime.models.FileUpdateListener;
import aohara.tinkertime.models.Mod;
import aohara.tinkertime.models.ModStructure;
import aohara.tinkertime.models.UpdateableFile;
import aohara.tinkertime.workflows.tasks.CacheCrawlerPageTask;
import aohara.tinkertime.workflows.tasks.CheckForUpdateTask;
import aohara.tinkertime.workflows.tasks.CrawlerDownloadTask;
import aohara.tinkertime.workflows.tasks.MarkModEnabledTask;
import aohara.tinkertime.workflows.tasks.MarkModUpdatedTask;
import aohara.tinkertime.workflows.tasks.MoveCrawlerDownloadToDestTask;
import aohara.tinkertime.workflows.tasks.NotfiyUpdateAvailableTask;

public class ModWorkflowBuilder extends WorkflowBuilder {
	
	public static enum ModDownloadType { Page, File, Image };
	
	public ModWorkflowBuilder(String workflowName) {
		super(workflowName);
	}
	
	/**
	 * Notifies the listeners if an update is available for the given file
	 */
	public void checkForUpdates(UpdateableFile file, FileUpdateListener... listeners) throws IOException, UnsupportedHostException {	
		DownloaderContext context = DirectDownloaderContext.fromUrl(file.getPageUrl(), null, null);
		addTask(new CacheCrawlerPageTask(context));
		addTask(new CheckForUpdateTask(context, file.getUpdatedOn(), file.getNewestFileName()));
		addTask(new NotfiyUpdateAvailableTask(context.crawler, listeners));
	}
	
	/**
	 * Notifies the listener of the file's latest version available.
	 */
	public void checkLatestVersion(UpdateableFile file, FileUpdateListener... listeners) throws UnsupportedHostException {
		DownloaderContext context = DirectDownloaderContext.fromUrl(file.getPageUrl(), null, null);
		addTask(new CacheCrawlerPageTask(context));
		addTask(new NotfiyUpdateAvailableTask(context.crawler, listeners));
	}
	
	/**
	 * Downloads the latest version of the mod referenced by the URL.
	 */
	public void downloadMod(URL pageUrl, TinkerConfig config, ModStateManager sm) throws IOException, UnsupportedHostException {
		ModDownloaderContext context = ModDownloaderContext.create(pageUrl, config);
		addTask(new CacheCrawlerPageTask(context));
		downloadWithCrawler(context, ModDownloadType.File);
		downloadWithCrawler(context, ModDownloadType.Image);
		addTask(new MarkModUpdatedTask(sm, context));
	}
	
	private void downloadWithCrawler(DownloaderContext context, ModDownloadType type) throws IOException{
		Path temp = Files.createTempFile("temp", ".download");
		temp.toFile().deleteOnExit();
		addTask(new CrawlerDownloadTask(context.crawler, type, temp));
		addTask(new MoveCrawlerDownloadToDestTask(context, type, temp));
	}
	
	/**
	 * Delete the mod's zip file, but do not mark the mod as deleted
	 * @param mod
	 * @param config
	 */
	public void deleteModZip(final Mod mod, final TinkerConfig config){
		delete(mod.getCachedZipPath(config));
	}
	
	/**
	 * Fully delete the mod and mark it as deleted.
	 * @param mod
	 * @param config
	 * @param sm
	 */
	public void deleteMod(Mod mod, TinkerConfig config, ModStateManager sm) {
		deleteModZip(mod, config);
		delete(mod.getCachedImagePath(config));
		addTask(MarkModUpdatedTask.notifyDeletion(sm, mod, config));
	}
	
	public void disableMod(Mod mod, TinkerConfig config, ModStateManager sm) throws IOException{
		for (ZipNode module : ModStructure.inspectArchive(config, mod).getModules()){
			
			if (!isDependency(module, config, sm)){
				delete(config.getGameDataPath().resolve(module.getName()));
			}
		}
		addTask(new MarkModEnabledTask(mod, sm, false));
	}
	
	public void enableMod(Mod mod, TinkerConfig config, ModStateManager sm, ConflictResolver cr) throws IOException{
		ModStructure structure = ModStructure.inspectArchive(config, mod);
		for (ZipNode module : structure.getModules()){
			addTask(new UnzipTask(config.getGameDataPath(), module, cr));
		}
		addTask(new MarkModEnabledTask(mod, sm, true));
	}
	
	// helpers
	
	private boolean isDependency(ZipNode module, TinkerConfig config, ModStateManager sm) throws IOException{
		int numDependencies = 0;
		for (Mod mod : sm.getMods()){
			try {
				if (ModStructure.inspectArchive(config, mod).usesModule(module)){
					numDependencies++;
				}
			} catch (FileNotFoundException ex){}
		}
		return numDependencies > 1;
	}
}
