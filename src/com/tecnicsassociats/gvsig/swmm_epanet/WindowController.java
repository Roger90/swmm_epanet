package com.tecnicsassociats.gvsig.swmm_epanet;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFileChooser;

import com.iver.andami.Launcher;
import com.iver.andami.PluginServices;
import com.jeta.forms.components.panel.FormPanel;


public class WindowController implements ActionListener{

	private InpExtension model;
	private FormPanel form;
	private boolean readyShp = false;
	private boolean readyOut = false;
	private File dirShp; 	
	private File dirOut;
	private Properties prop;
	

	public WindowController (InpExtension model, MainWindow view) {

		this.model = model;
		this.form = view.getForm();
		this.prop = InpExtension.getIniProperties();	
		this.model.sExport = "SWMM_";		
		
		try{
			
			form.getLabel(Constants.LBL_TITLE).setText(PluginServices.getText(null, "lbl_title"));		
			form.getLabel(Constants.LBL_DIR_SHP).setText(PluginServices.getText(null, "lbl_dir_shp"));
			form.getLabel(Constants.LBL_DIR_OUT).setText(PluginServices.getText(null, "lbl_dir_out"));
			form.getLabel(Constants.LBL_FILE_OUT).setText(PluginServices.getText(null, "lbl_file_out"));	
			form.getLabel(Constants.LBL_VERSION).setText(PluginServices.getText(null, "lbl_version"));			
			form.getButton(Constants.BTN_ACCEPT).setText(PluginServices.getText(null, "btn_accept"));
			form.getButton(Constants.BTN_CANCEL).setText(PluginServices.getText(null, "btn_cancel"));
			form.getButton(Constants.BTN_HELP_TEMPLATE).setText(PluginServices.getText(null, "btn_help_template"));			
			
			form.getRadioButton(Constants.OPT_EPANET).setActionCommand("EPANET_");
			form.getRadioButton(Constants.OPT_SWMM).setActionCommand("SWMM_");
			form.getButton(Constants.BTN_FOLDER_SHP).setActionCommand("shp");		
			form.getButton(Constants.BTN_FOLDER_OUT).setActionCommand("out");
			form.getButton(Constants.BTN_ACCEPT).setActionCommand("accept");
			form.getButton(Constants.BTN_CANCEL).setActionCommand("cancel");		
			form.getButton(Constants.BTN_HELP_TEMPLATE).setActionCommand("help");				

		} catch (NullPointerException e){
			System.out.println(e.getMessage());
		}
		
		dirShp = new File(InpExtension.getIniProperties().getProperty("FOLDER_SHP", new File(Launcher.getAppHomeDir()).getParent()));
		if (dirShp.exists()){
			form.getTextComponent(Constants.TXT_DIR_SHP).setText(dirShp.getAbsolutePath());
			readyShp = true;
		}
		dirOut = new File(InpExtension.getIniProperties().getProperty("FOLDER_OUT", new File(Launcher.getAppHomeDir()).getParent()));
		if (dirOut.exists()){
			form.getTextComponent(Constants.TXT_DIR_OUT).setText(dirOut.getAbsolutePath());		
			readyOut = true;
		}

	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		System.out.println(command);		
		if (command.equalsIgnoreCase("shp")){
			chooseFolderShp();
		}
		else if (command.equalsIgnoreCase("out")){
			chooseFolderOut();
		}
		else if (command.equalsIgnoreCase("accept")){
			executeAccept();
		}
		else if (command.equalsIgnoreCase("cancel")){
			PluginServices.getMDIManager().closeWindow(PluginServices.getMDIManager().getActiveWindow());	
		}
		else if (command.equalsIgnoreCase("help")){
			openPDF();
		}
		else if (command.equalsIgnoreCase("EPANET_") || command.equalsIgnoreCase("SWMM_")){
			this.model.sExport = command;
		}

	}
	
	
	private void openPDF(){
		if (this.model.fileHelp != null) {
			try {
				Desktop.getDesktop().open(this.model.fileHelp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	
	private void executeAccept() {

		if (!readyShp) {
			InpExtension.showError("dir_shp_not_selected", "", "inp_descr");	
			return;
		} 
		if (!readyOut) {
			InpExtension.showError("dir_out_not_selected", "", "inp_descr");
			return;
		} 		
		
		this.model.fileOut = null;
		String sFileOut = form.getTextField(Constants.TXT_FILE_OUT).getText().trim();
		if (!sFileOut.equals("")){
			sFileOut = dirOut.getAbsolutePath() + File.separator + sFileOut;
			this.model.fileOut = new File(sFileOut);
		}
		
		// Connect to sqlite database
		if (!this.model.connectDB())
			return;

		// Check if all necessary files exist
		if (!this.model.checkFiles(dirShp.getAbsolutePath(), dirOut.getAbsolutePath()))
			return;					
		
		// Process all shapes and output to INP file
		this.model.processALL();
		
	}
	
	
	private void chooseFolderShp() {

		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(PluginServices.getText(null, "folder_shp"));
		File file = new File(InpExtension.getIniProperties().getProperty("FOLDER_SHP", new File(Launcher.getAppHomeDir()).getParent()));
		chooser.setCurrentDirectory(file);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			dirShp = chooser.getSelectedFile();
			form.getTextComponent(Constants.TXT_DIR_SHP).setText(dirShp.getAbsolutePath());
			prop.put("FOLDER_SHP", dirShp.getAbsolutePath());
			InpExtension.saveIniProperties();				
			readyShp = true;				
		}

	}
	
	
	private void chooseFolderOut() {

		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(PluginServices.getText(null, "folder_out"));
		File file = new File(InpExtension.getIniProperties().getProperty("FOLDER_OUT", new File(Launcher.getAppHomeDir()).getParent()));	
		chooser.setCurrentDirectory(file);		
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			dirOut = chooser.getSelectedFile();
			form.getTextComponent(Constants.TXT_DIR_OUT).setText(dirOut.getAbsolutePath());
			prop.put("FOLDER_OUT", dirOut.getAbsolutePath());
			InpExtension.saveIniProperties();				
			readyOut = true;				
		}

	}

}