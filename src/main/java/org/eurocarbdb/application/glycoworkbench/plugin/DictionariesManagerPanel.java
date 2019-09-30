/*
 *   EuroCarbDB, a framework for carbohydrate bioinformatics
 *
 *   Copyright (c) 2006-2009, Eurocarb project, or third-party contributors as
 *   indicated by the @author tags or express copyright attribution
 *   statements applied by the authors.  
 *
 *   This copyrighted material is made available to anyone wishing to use, modify,
 *   copy, or redistribute it subject to the terms and conditions of the GNU
 *   Lesser General Public License, as published by the Free Software Foundation.
 *   A copy of this license accompanies this distribution in the file LICENSE.txt.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *   or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *   for more details.
 *
 *   Last commit: $Rev$ by $Author$ on $Date::             $  
 */
/**
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
 */

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.MolecularFramework.io.CarbohydrateSequenceEncoding;
import org.eurocarbdb.application.glycanbuilder.FileUtils;
import org.eurocarbdb.application.glycanbuilder.STOCK_ICON;
import org.eurocarbdb.application.glycanbuilder.converter.GlycanParser;
import org.eurocarbdb.application.glycanbuilder.converter.GlycanParserFactory;
import org.eurocarbdb.application.glycanbuilder.fileutil.ExtensionFileFilter;
import org.eurocarbdb.application.glycanbuilder.logutility.LogUtils;
import org.eurocarbdb.application.glycanbuilder.massutil.MassOptions;
import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.eurocarbdb.application.glycoworkbench.GlycoWorkbench;
import org.wggds.webservices.io.QueryResultProcessor;
import org.wggds.webservices.io.data.*;
import org.wggds.webservices.io.query.AllStructuresQuery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

public class DictionariesManagerPanel extends SortingTablePanel<ProfilerPlugin> implements ActionListener, ProfilerPlugin.DictionariesChangeListener {    

	// components 
	protected JToolBar theToolBar;

	public DictionariesManagerPanel(ProfilerPlugin profiler) {
		super();
		setProfiler(profiler);
	}

	protected void initComponents() {
		super.initComponents();

		// create toolbar
		theToolBar = createToolBar(); 
		add(theToolBar,BorderLayout.SOUTH);

		// create table
		theTable.setShowVerticalLines(false);
		theTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		theTable.setUseStyledText(false);
	}

	protected ProfilerPlugin getDocumentFromWorkspace(GlycanWorkspace workspace) {
		return null;
	}

	protected void setDocumentFromWorkspace(GlycanWorkspace workspace) {
	}


	public void setProfiler(ProfilerPlugin _theDocument) {
		// reset old list
		if( theDocument!=null )
			theDocument.removeDictionariesChangeListener(this);

		// set new list
		if( _theDocument!=null )
			theDocument = _theDocument;
		else
			theDocument = new ProfilerPlugin(this.theApplication);

		theDocument.addDictionariesChangeListener(this);

		// update view
		updateView();
		updateActions();
	}

	protected void createActions() {
		theActionManager.add("add",FileUtils.defaultThemeManager.getImageIcon("add"),"Add new dictionary",KeyEvent.VK_N, "",this);
		theActionManager.add("open",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.DOCUMENT_OPEN, Plugin.DEFAULT_ICON_SIZE),"Open existing dictionary",KeyEvent.VK_O, "",this);
		theActionManager.add("delete",GlycoWorkbench.getDefaultThemeManager().getResizableIcon("deleteNew", Plugin.DEFAULT_ICON_SIZE),"Delete selected dictionary",KeyEvent.VK_DELETE, "",this);
		theActionManager.add("edit",FileUtils.defaultThemeManager.getImageIcon("edit"),"Edit selected dictionary",KeyEvent.VK_E, "",this);
		theActionManager.add("setwggdsurl",FileUtils.defaultThemeManager.getImageIcon("wggds_logo_260"),"Edit WGGDS URL",KeyEvent.VK_M, "",this);
		theActionManager.add("wggdssync",FileUtils.defaultThemeManager.getImageIcon("wggds_logo_260"),"Update local copy",KeyEvent.VK_M, "",this);
		theActionManager.add("wggdsearchlive",FileUtils.defaultThemeManager.getImageIcon("wggds_logo_260"),"Search live",KeyEvent.VK_M, "",this);
		theActionManager.add("wggdsreset",FileUtils.defaultThemeManager.getImageIcon("wggds_logo_260"),"Restore to builtin version",KeyEvent.VK_M, "",this);
	}

	protected void updateActions() {    
		boolean has_selection = theTable.getSelectedRows().length>0;
		boolean writeable = has_selection && getSelectedDictionary().isOnFileSystem();

		theActionManager.get("delete").setEnabled(writeable);
		theActionManager.get("edit").setEnabled(has_selection);
		theActionManager.get("setwggdsurl").setEnabled(has_selection);

		if(has_selection){
			StructureDictionary dict=getSelectedDictionary();
			theActionManager.get("wggdssync").setEnabled(dict.isWggds() && writeable ? true : false);
			theActionManager.get("wggdsearchlive").setEnabled(dict.isWggds());

			theActionManager.get("wggdsearchlive").setSelected(dict.isLiveSearch());

			theActionManager.get("wggdsreset").setEnabled(dict.getSourceFile().equals("")==false && writeable ? true : false);
		}else{
			theActionManager.get("wggdssync").setEnabled(false);
			theActionManager.get("wggdsearchlive").setEnabled(false);
			theActionManager.get("wggdsreset").setEnabled(false);
		}
	}  

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		toolbar.add(theActionManager.get("add"));
		toolbar.add(theActionManager.get("open"));    

		toolbar.addSeparator();

		toolbar.add(theActionManager.get("delete"));
		toolbar.add(theActionManager.get("edit"));
		toolbar.add(theActionManager.get("setwggdsurl"));
		toolbar.add(theActionManager.get("wggdssync"));
		toolbar.add(theActionManager.get("wggdsearchlive").getJCheckBox("Search Live", false, new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				StructureDictionary dict=getSelectedDictionary();
				if(dict!=null){
					dict.setLiveSearch(((JCheckBox) arg0.getSource()).isSelected());
				}

				dict.save();

				updateActions();
			}
		}));

		return toolbar;
	}

	protected JPopupMenu createPopupMenu() {
		final JPopupMenu menu = new JPopupMenu();

		menu.add(theActionManager.get("delete"));
		menu.add(theActionManager.get("edit"));
		menu.add(theActionManager.get("setwggdsurl"));
		menu.add(theActionManager.get("wggdssync"));
		menu.add(theActionManager.get("wggdsreset"));
		menu.add(theActionManager.get("wggdsearchlive").getJCheckBox("Search Live", false, new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				StructureDictionary dict=getSelectedDictionary();

				if(dict!=null){
					dict.setLiveSearch(((JCheckBox) arg0.getSource()).isSelected());
				}

				menu.setVisible(false);

				dict.save();

				updateActions();
			}
		}));
		
		return menu;
	}

	// table model

	public Class<?> getColumnClass(int columnIndex) {
		if( columnIndex==0 )
			return String.class;
		if( columnIndex==1 )
			return Integer.class;
		if( columnIndex==2 )
			return String.class;
		return Object.class;
	}

	public String getColumnName(int columnIndex) {
		if( columnIndex==0 )
			return "Name";
		if( columnIndex==1 )
			return "Size";
		if( columnIndex==2 )
			return "Path";
		return null;
	}

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		if( theDocument==null )
			return 0;
		return theDocument.getDictionaries().size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		StructureDictionary sd = new ArrayList<StructureDictionary>(theDocument.getDictionaries()).get(rowIndex);

		if( columnIndex==0 ) 
			return sd.getDictionaryName();
		if( columnIndex==1 ) 
			return sd.size();
		if( columnIndex==2 ) 
			return (sd.isOnFileSystem()) ?sd.getFileName() :"";        

			return null;
	}       

	// Actions

	public StructureDictionary getSelectedDictionary() {
		if( theTable.getSelectedRow()>=0 )        
			return new ArrayList<StructureDictionary>(theDocument.getDictionaries()).get(theTable.getSelectedRow());
		return null;
	}

	public boolean createUserDatabase() {
		try {
			// ask name
			String name = JOptionPane.showInputDialog(this, "Specify a database name"); 
			if( name==null || name.length()==0 )
				return false;

			// specify a file        
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setSelectedFile(new File(name + ".gwd"));
			fileChooser.setDialogTitle("Specify a file to store the database" );
			fileChooser.addChoosableFileFilter(new ExtensionFileFilter("gwd", "GlycoWorkbench dictionary file"));
			fileChooser.setCurrentDirectory(theDocument.getWorkspace().getFileHistory().getRecentFolder());

			// visualizzo la dialog
			if( fileChooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION ) 
				return false;

			// aggiunge l'estension        
			String filename = fileChooser.getSelectedFile().getAbsolutePath();
			javax.swing.filechooser.FileFilter ff = fileChooser.getFileFilter();
			if( ff!=fileChooser.getAcceptAllFileFilter() && (ff instanceof ExtensionFileFilter) ) 
				filename = FileUtils.enforceExtension(filename,((ExtensionFileFilter)ff).getDefaultExtension());

			// chiede conferma prima di sovrascrivere il file
			File file = new File(filename);                    
			if( file.exists() ) {
				int retValue = JOptionPane.showOptionDialog(this, "File exists. Overwrite file: " + filename + "?",
								"Salva documento", JOptionPane.YES_NO_CANCEL_OPTION, 
								JOptionPane.QUESTION_MESSAGE, null, null, null);  
				if( retValue!=JOptionPane.YES_OPTION )
					return false;
			}            

			// create dictionary and save
			StructureDictionary toadd = new StructureDictionary(name);
			toadd.save(filename);
			theDocument.addUserDictionary(toadd);

			fireTableChanged();
			return true;
		}
		catch(Exception e) {
			LogUtils.report(e);
			return false;
		}
	}

	public void loadUserDatabase() {
		try { 
			// imposto la dialog per l'apertura del file      
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Select a database file");
			fileChooser.addChoosableFileFilter(new ExtensionFileFilter("gwd", "GlycoWorkbench dictionary file"));
			fileChooser.setCurrentDirectory(theDocument.getWorkspace().getFileHistory().getRecentFolder());

			// visualizzo la dialog
			if( fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION ) 
				return;

			// retrieve file path
			String filename = fileChooser.getSelectedFile().getAbsolutePath();
			if( !FileUtils.exists(filename) ) 
				return;

			// load dictionary
			StructureDictionary toadd = new StructureDictionary(filename,true,null);
			if( theDocument.containsDictionary(toadd) ) {
				JOptionPane.showMessageDialog(this,"The database selected is already loaded.", "Duplicate database", JOptionPane.ERROR_MESSAGE);
				return; 
			}

			// add dictionary
			theDocument.addUserDictionary(toadd);
			fireTableChanged();
		}
		catch(Exception e) {
			LogUtils.report(e);
		}
	}

	public void removeSelectedDatabase() {
		if( theTable.getSelectedRow()==-1 )
			return;

		int retValue = JOptionPane.showOptionDialog(this, "Are you sure you want to permanently remove the user database?",
						"", JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE, null, null, null);  
		if( retValue!=JOptionPane.YES_OPTION )
			return;

		theDocument.removeDictionary(getSelectedDictionary());
		fireTableChanged();
	}


	public void editSelectedDatabase() {
		try {
			StructureDictionary selected = getSelectedDictionary();
			if( selected!=null ) {
				theDocument.getDictionariesEditPanel().showDictionary(selected.getDictionaryName());
				theDocument.show("Structures");
			}
		}
		catch(Exception e) {
			LogUtils.report(e);
		}
	}

	//-----------
	// listeners


	public void actionPerformed(ActionEvent e) {

		String action = e.getActionCommand();       

		if( action.equals("add") ) 
			createUserDatabase();    
		else if( action.equals("open") ) 
			loadUserDatabase();    
		else if( action.equals("delete") ) 
			removeSelectedDatabase();    
		else if( action.equals("edit") ) 
			editSelectedDatabase();
		else if( action.equals("setwggdsurl"))
			setWggdsUrl();
		else if( action.equals("wggdssync"))
			wggdsSync();
		else if( action.equals("wggdsreset"))
			wggdsReset();


		updateActions();
	}

	public void wggdsReset(){
		StructureDictionary dict=getSelectedDictionary();

		if(dict!=null){
			try{
				dict.restore();
			}catch(Exception e){
				LogUtils.report(e);
			}
		}

		dict.save();
		
		fireTableChanged();
	}

	public void setWggdsUrl(){
		StructureDictionary selected=getSelectedDictionary();
	
		boolean isGlycoWorkbenchResource=selected.isGlycoWorkbenchResource();
		
		String url=JOptionPane.showInputDialog(this,"Set WGGDS URI",selected.getUri());
		if(url==null || url.length()==0){
			return;
		}else{
			selected.setUri(url);
		}

		selected.save();
		
		if(isGlycoWorkbenchResource){
			
			//LogUtils.report(new Exception(selected.getFileName()));
			
			theDocument.removeDictionary(selected);
			theDocument.addUserDictionary(selected);
		}
	}

	public void wggdsSync(){
		final StructureDictionary dict=getSelectedDictionary();
		Thread thread=new Thread(){
			public void run(){
				try{
					dict.clear();

					dict.setFireDocumentChanged(false);

					AllStructuresQuery query=new AllStructuresQuery();
					query.setCompleteInformation(CompleteInformation.Complete);
					query.setFormat(OutputFormat.XML);

					final GlycanParser glydeParser= GlycanParserFactory.getParser(CarbohydrateSequenceEncoding.glyde.getId());

					QueryResultProcessor queryProc=new QueryResultProcessor(){
						@Override
						public void processStructureResult(StructureResult structureResult){
							if(structureResult.getBiologicalSource()==null || structureResult.getBiologicalSource().size()==0){
								try{
									dict.add(new StructureType(glydeParser.readGlycan(structureResult.getSequence(), new MassOptions())));
								}catch(Exception e){
									//e.printStackTrace();
								}
							}else{
								for(BiologicalContext context:structureResult.getBiologicalSource()){
									try{
										StructureType type=new StructureType(glydeParser.readGlycan(structureResult.getSequence(), new MassOptions()));
										type.setSource(context.getTaxonomyName());

										dict.add(type);
									}catch(final Exception ex){
										SwingUtilities.invokeLater(new Runnable(){
											public void run(){
												LogUtils.report(ex);
											}
										});
									}
								}
							}

						}

						@Override
						public void processResultHeader(QueryResultHeader queryResultHeader){

						}

						@Override
						public void processError(final ServerSideError error){
							SwingUtilities.invokeLater(new Runnable(){
								public void run(){
									LogUtils.report(new Exception("WGGDS upstream server error\nError code: "+error.getErrorId().getId()+"\n>Message\n"+error.getMessage()));
								}
							});
						}
					};

					query.runQuery(dict.getUri(), queryProc);

					dict.setHasBeenSynced(true);
					dict.save();
				} catch (Exception e) {
					LogUtils.report(e);
				}finally{
					dict.setFireDocumentChanged(true);
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							dict.fireDocumentChanged();
						}
					});

				}
			}
		};
		
		thread.start();
	}

	public void dictionariesChanged(ProfilerPlugin.DictionariesChangeEvent e) {
		updateView();
		updateActions();
	}

	public void updateData() {
	}

}
