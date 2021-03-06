package io.andrewohara.tinkertime.views;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.google.inject.Inject;

import io.andrewohara.common.views.DecoratedComponent;
import io.andrewohara.common.views.Dialogs;
import io.andrewohara.tinkertime.controllers.InstallationManager;
import io.andrewohara.tinkertime.models.Installation;
import io.andrewohara.tinkertime.models.Installation.InvalidGameDataPathException;

public class InstallationSelectorView implements DecoratedComponent<JPanel> {

	private final JPanel panel = new JPanel();
	private final InstallationManager installationManager;
	private final JComboBox<Installation> installations;
	private final Dialogs dialogs;

	private JDialog dialog;

	@Inject
	InstallationSelectorView(InstallationManager installationManager, Dialogs dialogs){
		this.installationManager = installationManager;
		this.dialogs = dialogs;

		panel.setLayout(new BorderLayout());

		JLabel label = new JLabel("Please select a KSP GameData folder to use");
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		panel.add(label, BorderLayout.NORTH);
		panel.add(installations = new JComboBox<>(), BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new FlowLayout());
		controlPanel.add(new JButton(new AddAction()));
		controlPanel.add(new JButton(new RenameAction()));
		controlPanel.add(new JButton(new RemoveAction()));
		controlPanel.add(new JButton(new OkAction()));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(controlPanel, BorderLayout.SOUTH);
	}

	@Override
	public JPanel getComponent() {
		return panel;
	}

	public void toDialog(){
		// Load Installations
		installations.removeAllItems();
		for (Installation installation : installationManager.getInstallations()){
			installations.addItem(installation);
		}

		// Select currently configured installation
		try {
			Installation current = getConfigured();
			for (Installation choice : getChoices()){
				if (choice.equals(current)){
					installations.setSelectedItem(choice);
					break;
				}
			}
		} catch (NoSelectedInstallationException e1) {
			// Do Nothing
		}


		dialog = new JDialog();
		dialog.setTitle("KSP Installation Manager");
		dialog.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.add(getComponent());

		dialog.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					// If there is a configured installation, go ahead and close
					getConfigured();
					dialog.setVisible(false);
				} catch (NoSelectedInstallationException e1) {
					// If user does not want to select an installation, close the application
					if (JOptionPane.showConfirmDialog(
							panel,
							"An installation must be selected to continue.  Would you like to select one?",
							"No installation is selected",
							JOptionPane.YES_NO_OPTION
							) == JOptionPane.NO_OPTION){
						System.exit(1);
					}
				}
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	/////////////
	// Helpers //
	/////////////

	private String getNewName(Installation installation) throws NoNameEnteredException{
		String message = "Enter the new name for the installation";
		if (installation != null){
			message = String.format("%s at %s", message, installation.getGameDataPath());
		}

		String newName = JOptionPane.showInputDialog(panel, message, "Rename Installation", JOptionPane.QUESTION_MESSAGE);
		if (newName == null || newName.trim().isEmpty()) throw new NoNameEnteredException();
		return newName;
	}

	private Installation getSelected() throws NoSelectedInstallationException {
		Object selected = installations.getSelectedItem();
		if (selected == null) throw new NoSelectedInstallationException();
		return (Installation) selected;
	}

	private Installation getConfigured() throws NoSelectedInstallationException {
		Installation installation = installationManager.getSelectedInstallation();
		if (installation == null){
			throw new NoSelectedInstallationException();
		}
		return installation;
	}

	private void exceptionDialog(Exception e){
		exceptionDialog(e, "Error registering installation");
	}

	private void exceptionDialog(Exception e, String title){
		dialogs.errorDialog(panel, title, e);
	}

	private Collection<Installation> getChoices(){
		Collection<Installation> choices = new LinkedList<>();
		for (int i=0; i<installations.getItemCount(); i++){
			choices.add(installations.getItemAt(i));
		}
		return choices;
	}

	private void checkForDuplicates(String name, Path path) throws DuplicatedFieldException {
		for (Installation existing : getChoices()) {
			if (name != null && existing.getName().equals(name)){
				throw new DuplicatedFieldException("Name");
			} else if (path != null && existing.getGameDataPath().equals(path)){
				throw new DuplicatedFieldException("GameData Path");
			}
		}

	}

	/////////////
	// Actions //
	/////////////

	@SuppressWarnings("serial")
	private class AddAction extends AbstractAction {

		private AddAction(){
			super("Add");
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Path folder = FileChoosers.chooseGameDataFolder();
				String name = getNewName(null);
				checkForDuplicates(name, folder);
				Installation newInstallation = installationManager.newInstallation(name, folder);
				installations.addItem(newInstallation);
				installations.setSelectedItem(newInstallation);
			} catch (FileNotFoundException e1) {
				// Do Nothing
			} catch (InvalidGameDataPathException | NoNameEnteredException | DuplicatedFieldException | SQLException e1) {
				exceptionDialog(e1);
			}
		}
	}

	@SuppressWarnings("serial")
	private class RemoveAction extends AbstractAction {

		private RemoveAction(){
			super("Remove");
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Installation current = getSelected();
				installations.removeItem(current);
				current.delete();
			} catch (NoSelectedInstallationException e1) {
				// Do Nothing
			} catch (SQLException e) {
				exceptionDialog(e);
			}
		}

	}

	@SuppressWarnings("serial")
	private class RenameAction extends AbstractAction {

		private RenameAction(){
			super("Rename");
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Installation current = getSelected();
				String newName = getNewName(current);
				checkForDuplicates(newName, null);
				current.rename(newName);
				installations.repaint();
			} catch (NoSelectedInstallationException e2) {
				// Do Nothing
			} catch (NoNameEnteredException | DuplicatedFieldException | SQLException e1) {
				exceptionDialog(e1);
			}
		}
	}

	@SuppressWarnings("serial")
	private class OkAction extends AbstractAction {

		private OkAction(){
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				installationManager.changeInstallation(getSelected());
				if (dialog != null){
					dialog.setVisible(false);
				}

			} catch (NoSelectedInstallationException e1) {
				exceptionDialog(e1);
			}
		}
	}

	////////////////
	//Exceptions //
	////////////////

	@SuppressWarnings("serial")
	private static class NoNameEnteredException extends Exception {
		private NoNameEnteredException(){
			super("No name was entered for the installation");
		}
	}

	@SuppressWarnings("serial")
	private static class DuplicatedFieldException extends Exception {
		private DuplicatedFieldException(String type){
			super("Another installation is already using that " + type);
		}
	}

	@SuppressWarnings("serial")
	public static class NoSelectedInstallationException extends Exception {
		public NoSelectedInstallationException(){
			super("No Installation was selected");
		}
	}
}
