import java.awt.GridLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class Tab {
	protected JTextPane text_pane = new JTextPane(); //create text container
	protected JScrollPane text_pane_with_scroll;
	protected String tabName;
	protected String content;
	
	public Tab(String text, String name)
	{
		tabName = name;
		content = text;
								
		text_pane.setText(text);			   				//copy content from file to container
		text_pane.setLayout(new GridLayout(1, 1));  		//not yet
        text_pane_with_scroll = new JScrollPane(text_pane);  //make container scrollable
	}
}
