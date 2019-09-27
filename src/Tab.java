import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.text.Style;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;

import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane.*;

public class Tab 
{
	protected String path;											//path of the file content
	protected RSyntaxTextArea textArea = new RSyntaxTextArea(); 	//create text container
	protected RTextScrollPane text_area_with_scroll;
	protected String tabName;
	protected String content;
	
	public Tab(String text, String name, String file_path)
	{
		path = file_path;
		tabName = name;
		content = text;
		
//		text_pane.setText(text);			   					//copy content from file to container
//		text_pane.setLayout(new BorderLayout());  				//not yet
//		
//        text_pane_with_scroll = new JScrollPane(text_pane);  	//make container scrollable
		
	    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
	    textArea.setCodeFoldingEnabled(true);
	    textArea.setText(content);
	    text_area_with_scroll = new RTextScrollPane(textArea);
        
       
	}
}
