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

import org.eurocarbdb.application.glycanbuilder.FileUtils;
import org.eurocarbdb.application.glycanbuilder.GlycanAction;
import org.eurocarbdb.application.glycanbuilder.STOCK_ICON;
import org.eurocarbdb.application.glycanbuilder.logutility.LogUtils;
import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.eurocarbdb.application.glycoworkbench.GlycoWorkbench;
import org.eurocarbdb.application.glycoworkbench.NotesDocument;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class NotesPanel extends DocumentPanel<NotesDocument> implements ActionListener, DocumentListener, Printable  {    

    private static final long serialVersionUID = 0L;       
   
    // components    
    protected JTextPane theTextPane;
    protected JToolBar  theToolBar;

    boolean ignore_text_change = false;

    //

    public NotesPanel()  {
    super();
    }

    protected void initComponents() {
    setLayout(new BorderLayout());    

    // create text pane
    add(new JScrollPane(theTextPane = new JTextPane()),BorderLayout.CENTER);
    theTextPane.getDocument().addDocumentListener(this);
    
    // create toolbar
    add(theToolBar = createToolBar(),BorderLayout.SOUTH);
    }
    
    public NotesDocument getDocumentFromWorkspace(GlycanWorkspace workspace) {
    return ( workspace!=null ) ?workspace.getNotes() :null;
    }
 

    public void setDocumentFromWorkspace(GlycanWorkspace workspace) {
    if( theDocument!=null )
        theDocument.removeDocumentChangeListener(this);

    theDocument = getDocumentFromWorkspace(workspace);
    if( theDocument==null )
        theDocument = new NotesDocument();

    theDocument.addDocumentChangeListener(this);

    updateView();
    updateActions();
    }     
   

    protected void createActions() {

    // file
    theActionManager.add("new",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.REFRESH, Plugin.DEFAULT_ICON_SIZE),"Clear",KeyEvent.VK_N, "",this);
    theActionManager.add("open",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.DOCUMENT_OPEN, Plugin.DEFAULT_ICON_SIZE),"Open",KeyEvent.VK_O, "",this);
    theActionManager.add("save",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.DOCUMENT_SAVE, Plugin.DEFAULT_ICON_SIZE),"Save",KeyEvent.VK_S, "",this);
    theActionManager.add("saveas",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.DOCUMENT_SAVE_AS, Plugin.DEFAULT_ICON_SIZE),"Save as...",KeyEvent.VK_A, "",this);

    // print
    theActionManager.add("print",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.DOCUMENT_PRINT, Plugin.DEFAULT_ICON_SIZE),"Print...",KeyEvent.VK_P, "",this);

    // edit
    theActionManager.add("undo",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.UNDO, Plugin.DEFAULT_ICON_SIZE),"Undo",KeyEvent.VK_U, "",this);
    theActionManager.add("redo",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.REDO, Plugin.DEFAULT_ICON_SIZE),"Redo", KeyEvent.VK_R, "",this);

    theActionManager.add("add",FileUtils.defaultThemeManager.getImageIcon("add"),"Add peak",KeyEvent.VK_D, "",this);
    theActionManager.add("cut",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.CUT, Plugin.DEFAULT_ICON_SIZE),"Cut",KeyEvent.VK_T, "",this);
    theActionManager.add("copy",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.COPY, Plugin.DEFAULT_ICON_SIZE),"Copy",KeyEvent.VK_C, "",this);
    theActionManager.add("paste",GlycoWorkbench.getDefaultThemeManager().getResizableIcon(STOCK_ICON.PASTE, Plugin.DEFAULT_ICON_SIZE),"Paste", KeyEvent.VK_P, "",this);
    theActionManager.add("delete",GlycoWorkbench.getDefaultThemeManager().getResizableIcon("deleteNew", Plugin.DEFAULT_ICON_SIZE),"Delete",KeyEvent.VK_DELETE, "",this);
    theActionManager.add("selectall",FileUtils.defaultThemeManager.getImageIcon("selectall"),"Select all",KeyEvent.VK_A, "",this);

    }
 
    protected void updateActions() {
    boolean has_selection = (theTextPane.getSelectionStart()!=-1);

    theActionManager.get("save").setEnabled(theDocument.hasChanged());

    theActionManager.get("undo").setEnabled(theDocument.getUndoManager().canUndo());
    theActionManager.get("redo").setEnabled(theDocument.getUndoManager().canRedo());

    theActionManager.get("cut").setEnabled(has_selection);
    theActionManager.get("copy").setEnabled(has_selection);
    theActionManager.get("delete").setEnabled(has_selection);
    }   

    private JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    toolbar.add(theActionManager.get("new"));
    toolbar.add(theActionManager.get("open"));    
    toolbar.add(theActionManager.get("save"));
    toolbar.add(theActionManager.get("saveas"));

    toolbar.addSeparator();
    
    toolbar.add(theActionManager.get("print"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("undo"));
    toolbar.add(theActionManager.get("redo"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("cut"));
    toolbar.add(theActionManager.get("copy"));
    toolbar.add(theActionManager.get("paste"));
    toolbar.add(theActionManager.get("delete"));      

    return toolbar;
    }
    
    private JPopupMenu createPopupMenu() {
    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("cut"));
    menu.add(theActionManager.get("copy"));
    menu.add(theActionManager.get("paste"));
    menu.add(theActionManager.get("delete"));

    return menu;
    }
         
    //-----------------
    // data

    public boolean checkDocumentChanges() {    
    if( theDocument.hasChanged() && theDocument.wasSaved() && !theDocument.isEmpty() ) {
        int ret = JOptionPane.showConfirmDialog(this,"Save changes to notes?", null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if( ret == JOptionPane.CANCEL_OPTION ) 
        return false;        
        if( ret == JOptionPane.YES_OPTION ) {
        if( !theApplication.onSaveAs(theDocument) )
            return false;
        }
    }    
    
    return true;
    }


    //-----------
    // Visualization

    protected void updateData() {
    }

    protected void updateView() {
    if( !ignore_text_change ) {
        ignore_text_change = true;
        theTextPane.setText(theDocument.getText());
        ignore_text_change = false;
    }
    }   
        
    
    //-----------------
    // actions           
        
    public void onPrint() {
    try {        
        PrinterJob pj = theWorkspace.getPrinterJob();
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet(); 
        if( pj.printDialog(aset) ) {             
        Doc doc = new SimpleDoc(theDocument.getText(), DocFlavor.STRING.TEXT_PLAIN, null); 
        PrintService ps = pj.getPrintService();
        DocPrintJob job = ps.createPrintJob(); 
        job.print(doc, aset);                        
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
    }    
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
    if (pageIndex > 0) 
        return NO_SUCH_PAGE;

    theTextPane.print(graphics);
    return PAGE_EXISTS;
    }

    public void onUndo()  {
    try {
        theDocument.getUndoManager().undo();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    public void onRedo()  {
    try {
        theDocument.getUndoManager().redo();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }
     
    public void cut() {    
    theTextPane.cut();
    }

    public void copy() {
    theTextPane.copy();    
    }
 
    public void delete() {    
    theTextPane.cut();
    }

    public void paste() {
    theTextPane.paste();
    }

    public void actionPerformed(ActionEvent e) {

    String action = GlycanAction.getAction(e);

    if( action.equals("new") )
        theApplication.onNew(theDocument);
    else if( action.equals("open") )
        theApplication.onOpen(null,theDocument,false);
    else if( action.equals("save") )
        theApplication.onSave(theDocument);
    else if( action.equals("saveas") )
        theApplication.onSaveAs(theDocument);

    else if( action.equals("print") )
        onPrint();

    else if( action.equals("undo") )
        onUndo();
    else if( action.equals("redo") )
        onRedo();

    else if( action.equals("cut") )
        cut();
    else if( action.equals("copy") )
        copy();
    else if( action.equals("paste") )
        paste();
    else if( action.equals("delete") )
        delete();

    /*
    else if( action.equals("selectall") )
        selectAll();
    else if( action.equals("selectnone") )
        resetSelection();
    */

    updateActions();
    }     
    
    public void changedUpdate(DocumentEvent e) {
    if( !ignore_text_change ) {
        ignore_text_change = true;
        theDocument.setText(theTextPane.getText());
        ignore_text_change = false;
    }
    }
    
    public void insertUpdate(DocumentEvent e) {
    if( !ignore_text_change ) {
        ignore_text_change = true;
        theDocument.setText(theTextPane.getText());
        ignore_text_change = false;
    }
    }
    
    public void removeUpdate(DocumentEvent e) {
    if( !ignore_text_change ) {
        ignore_text_change = true;
        theDocument.setText(theTextPane.getText());
        ignore_text_change = false;
    }
    }

}
 