package aohara.tinkertime.workflows;

import aohara.common.workflows.Workflow;
import aohara.tinkertime.Config;
import aohara.tinkertime.content.ArchiveInspector;
import aohara.tinkertime.controllers.ModStateManager;
import aohara.tinkertime.models.Mod;
import aohara.tinkertime.models.Module;
import aohara.tinkertime.workflows.tasks.DeleteModTask;

/**
 * Workflow that will result in a Mod being disabled and deleted from the FS.
 * 
 * @author Andrew O'Hara
 */
public class DeleteModWorkflow extends Workflow {

	public DeleteModWorkflow(Mod mod, Config config, ModStateManager sm) {
		super("Deleting " + mod.getName());
		if (mod.isEnabled()){
			for (Module module : ArchiveInspector.inspectArchive(config, mod).getModules()){
				queueDelete(config.getGameDataPath().resolve(module.getName()));
			}
		}
		addTask(new DeleteModTask(this, mod, config, sm));
	}
}
