package org.eurocarbdb.application.glycoworkbench;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

public class JSplitPaneCustom extends JSplitPane{
	public static HashSet<JSplitPane> toPaint=new HashSet<JSplitPane>();
	public static HashSet<JSplitPaneCustom> painted=new HashSet<JSplitPaneCustom>();
	
	public JSplitPaneCustom(){
		super();
	}
	
	public JSplitPaneCustom(int verticalSplit) {
		super(verticalSplit);
	}

	@Override
	public void paint(Graphics g){
		super.paint(g);
		if(toPaint.contains(this) && !painted.contains(this)){
			painted.add(this);
		}
	}
}