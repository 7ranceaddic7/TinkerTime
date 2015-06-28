package aohara.tinkertime.resources;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import aohara.tinkertime.TinkerConfig;
import aohara.tinkertime.controllers.ModExceptions.ModNotDownloadedException;
import aohara.tinkertime.controllers.ModMetaHelper;
import aohara.tinkertime.controllers.ModUpdateHandler;
import aohara.tinkertime.models.DefaultMods;
import aohara.tinkertime.models.Mod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Controller for Storing and Retrieving persistent mod state.
 * 
 * Any time a mod's information or state is updated, the updater must call
 * modUpdated as specified by the ModUpdateListener interface. 
 * 
 * @author Andrew O'Hara
 */
@Singleton
public class ModMetaLoader implements ModUpdateHandler {
	
	private static final Type MODS_TYPE = new TypeToken<Set<Mod>>() {}.getType();
	
	private final Gson gson;
	private final TinkerConfig config;
	private final ModMetaHelper modMetaHelper;
	private final Map<Mod, ModStructure> modCache = new LinkedHashMap<>();
	
	// -- Initializers ----------------------------------------
	
	@Inject
	ModMetaLoader(TinkerConfig config, Gson gson, ModMetaHelper modMetaHelper){
		this.config = config;
		this.gson = gson;
		this.modMetaHelper = modMetaHelper;
	}
	
	//-- Public Methods ----------------------------------------
	
	public void clear(){
		modCache.clear();
	}
	
	public synchronized Set<Mod> getMods(){
		if (modCache.isEmpty()){
			importMods(config.getModsListPath());
		}
		
		return modCache.keySet();
	}
	
	/**
	 * Call when a mod has been updated.
	 * 
	 * Updates the persistent mod data, and refreshes the mod views.
	 * @throws IOException 
	 */
	public synchronized void modUpdated(Mod mod) {
		modDeleted(mod);	
		
		cacheMod(mod);
		saveMods(modCache.keySet(), config.getModsListPath());
	}
	
	/**
	 * Call when a mod has been deleted.
	 * 
	 * Deleted the persistent mod data, and removes from mod views.
	 */
	public synchronized void modDeleted(Mod mod){
		// Search for all copies of the mode to delete
		// Potential duplicates due to legacy imports		
		for (Mod cached : new LinkedHashSet<>(modCache.keySet())){
			if (cached.equals(mod)){
				modCache.remove(cached);
			}
		}
		
		saveMods(modCache.keySet(), config.getModsListPath());
	}
	
	public synchronized void exportEnabledMods(Path path){
		Set<Mod> toExport = new HashSet<>();
		for (Mod mod : modCache.keySet()){
			try {
				if (isEnabled(mod)){
					toExport.add(mod);
				}
			} catch (ModNotDownloadedException e) {
				// Do not export this mod
			}
		}
		saveMods(toExport, path);
	}
	
	/**
	 * Loads the mods from the given file and adds them
	 * 
	 * @param path file to load mods from
	 * @param mm ModManager reference
	 * @throws IOException 
	 */
	public synchronized void importMods(Path path) {		
		for (Mod mod : loadMods(path)){
			cacheMod(mod);
		}
	}
	
	public synchronized boolean isEnabled(Mod mod) throws ModNotDownloadedException{
		for (Path filePath : getModFileDestPaths(mod)){
			if (filePath.getFileName().toString().contains(".") && !filePath.toFile().exists()){
				return false;
			}
		}
		return true;
	}
	
	public synchronized Set<Path> getModFilePaths(Mod mod) throws ModNotDownloadedException {
		try {
			return modCache.get(mod).getPaths();
		} catch (IOException | NullPointerException e) {
			throw new ModNotDownloadedException(mod, e.toString());
		}
	}
	
	public synchronized Set<Path> getModFileDestPaths(Mod mod) throws ModNotDownloadedException {
		Set<Path> paths = new LinkedHashSet<>();
		Path destFolder = config.getGameDataPath();
		for (Path path : getModFilePaths(mod)){
			paths.add(destFolder.resolve(path));
		}
		return paths;
	}
	
	public synchronized ModStructure getStructure(Mod mod){
		return modCache.get(mod);
	}
	
	// -- Private Methods ----------------------------------------
	
	/**
	 * Loads the mods from the given file and returns them.
	 * 
	 * @param path file to get mods from
	 * @param mm ModManager reference
	 * @return set of mods loaded from the file
	 */
	private Set<Mod> loadMods(Path path){
		Set<Mod> mods = new HashSet<>();
		
		try(FileReader reader = new FileReader(path.toFile())){
			// Try to load mods from file
			Set<Mod> newMods = gson.fromJson(reader, MODS_TYPE);
			for (Mod newMod : newMods){
				// If mod is updateable, or if the local zip file is available, add mod
				if (newMod.isUpdateable() || modMetaHelper.isDownloaded(newMod)){
					mods.add(newMod);
				}
			}
		} catch (FileNotFoundException e){
			// No Action
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		DefaultMods.ensureDefaults(mods);
		return mods;
	}
	
	private void saveMods(Set<Mod> mods, Path path){
		try(FileWriter writer = new FileWriter(path.toFile())){
			gson.toJson(mods, MODS_TYPE, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private final void cacheMod(Mod mod) {
		modCache.put(mod, new ModStructure(modMetaHelper.getZipPath(mod)));
	}
}