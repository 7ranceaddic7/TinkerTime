package io.andrewohara.tinkertime.controllers;

import io.andrewohara.common.Listenable;
import io.andrewohara.common.workflows.tasks.TaskCallback;
import io.andrewohara.tinkertime.controllers.ModExceptions.CannotDeleteModException;
import io.andrewohara.tinkertime.controllers.ModExceptions.ModNotDownloadedException;
import io.andrewohara.tinkertime.controllers.ModExceptions.ModUpdateFailedException;
import io.andrewohara.tinkertime.controllers.ModExceptions.NoModSelectedException;
import io.andrewohara.tinkertime.controllers.workflows.ModWorkflowBuilder;
import io.andrewohara.tinkertime.controllers.workflows.ModWorkflowBuilderFactory;
import io.andrewohara.tinkertime.db.ModLoader;
import io.andrewohara.tinkertime.io.crawlers.CrawlerFactory.UnsupportedHostException;
import io.andrewohara.tinkertime.models.mod.Mod;
import io.andrewohara.tinkertime.models.mod.ModFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Controller for initiating Asynchronous Tasks for Mod Processing.
 *
 * All Mod-Related Actions are to be initiated through this Controller.
 * All Asynchronous tasks initiated are executed by the executors of this class,
 * and the tasks are represented by {@link io.andrewohara.common.workflows.Workflow} classes.
 *
 * @author Andrew O'Hara
 */
@Singleton
public class ModManager extends Listenable<TaskCallback> {

	private final TaskLauncher taskLauncher;
	private final ModWorkflowBuilderFactory workflowBuilderFactory;
	private final ModLoader modLoader;
	private final ModFactory modFactory;

	private Mod selectedMod;

	@Inject
	ModManager(ModWorkflowBuilderFactory workflowBuilderFactory, ModLoader modLoader, TaskLauncher taskLauncher, ModFactory modFactory){
		this.workflowBuilderFactory = workflowBuilderFactory;
		this.modLoader = modLoader;
		this.taskLauncher = taskLauncher;
		this.modFactory = modFactory;
	}

	// -- Interface --------------------------------------------------------

	public Mod getSelectedMod() throws NoModSelectedException {
		if (selectedMod == null){
			throw new NoModSelectedException();
		}
		return selectedMod;
	}

	public void selectMod(Mod mod){
		this.selectedMod = mod;
	}

	public void updateMod(Mod mod, boolean forceUpdate) throws ModUpdateFailedException, ModNotDownloadedException {
		if (!mod.isUpdateable()){
			throw new ModUpdateFailedException(mod, "Mod is a local zip only, and cannot be updated.");
		}
		try {
			ModWorkflowBuilder builder = workflowBuilderFactory.createBuilder(mod);
			builder.updateMod(forceUpdate);
			taskLauncher.submitDownloadWorkflow(builder);
		} catch (UnsupportedHostException e) {
			throw new ModUpdateFailedException(e);
		}
	}

	public boolean downloadNewMod(URL url) throws MalformedURLException, UnsupportedHostException {
		if (modLoader.getByUrl(url) == null){
			Mod newMod = modFactory.newMod(url);
			ModWorkflowBuilder builder = workflowBuilderFactory.createBuilder(newMod);
			builder.downloadNewMod();
			taskLauncher.submitDownloadWorkflow(builder);
			return true;
		}
		return false;
	}

	public void addModZip(Path zipPath){
		Mod newMod = modFactory.newLocalMod(zipPath);
		ModWorkflowBuilder builder = workflowBuilderFactory.createBuilder(newMod);
		builder.addLocalMod(zipPath);
		taskLauncher.submitDownloadWorkflow(builder);
	}

	public void updateMods() throws ModUpdateFailedException, ModNotDownloadedException{
		for (Mod mod : modLoader.getMods()){
			if (mod.isUpdateable()){
				updateMod(mod, false);
			}
		}
	}

	public void toggleMod(final Mod mod) {
		ModWorkflowBuilder builder = workflowBuilderFactory.createBuilder(mod);
		try {
			if (mod.isEnabled()){
				builder.disableMod();
			} else {
				builder.enableMod();
			}
			taskLauncher.submitFileWorkflow(builder);
		} catch (ModNotDownloadedException e){
			// Ignore user input if mod not downloaded
		}
	}

	public void deleteMod(Mod mod) throws CannotDeleteModException {
		if (mod.isBuiltIn()){
			throw new CannotDeleteModException(mod, "Built-in");
		}
		ModWorkflowBuilder builder = workflowBuilderFactory.createBuilder(mod);
		builder.deleteMod();
		taskLauncher.submitFileWorkflow(builder);
	}

	public void checkForModUpdates() throws UnsupportedHostException{
		for (final Mod mod : modLoader.getMods()){
			try {
				if (mod.isUpdateable()){
					ModWorkflowBuilder builder = workflowBuilderFactory.createBuilder(mod);
					builder.checkForUpdates(true);
					taskLauncher.submitDownloadWorkflow(builder);
				}
			} catch (UnsupportedHostException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public void tryUpdateModManager() throws UnsupportedHostException, MalformedURLException {
		Mod tempMod = modFactory.newModManagerMod();
		ModWorkflowBuilder builder = workflowBuilderFactory.createBuilder(tempMod);
		builder.checkForUpdates(false);
		builder.downloadModInBrowser(tempMod);
		taskLauncher.submitDownloadWorkflow(builder);
	}
}
