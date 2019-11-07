import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.text.Style;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane.*;

/*Tab will contain file's information and text area*/

public class Tab 
{
	protected String tabName;
	protected String fileName;
	protected String content;
	protected String path;											
	protected File file;
	protected int count = 0;
	protected boolean modified = false;
	protected boolean projectFile; 
	
	protected JPanel container = new JPanel();		
	//{
		protected RTextScrollPane text_area_with_scroll;					//add scroll feature -> textarea
		//{
			protected RSyntaxTextArea textArea = new RSyntaxTextArea(); 	//create textarea
		//}
		protected JLabel count_label;
	//}
	
	public Tab(String content, String tabName, String path, File file, boolean projectFile )
	{
		this.path = path;
		this.tabName = tabName;
		fileName = tabName;
		this.content = content;
		this.file = file;
		this.projectFile = projectFile;
		
	    textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);			 //requirement 6
	    SyntaxScheme scheme = textArea.getSyntaxScheme();
	    scheme.getStyle(Token.OPERATOR).foreground = Color.RED;						 //requirement 7
	    scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = scheme.getStyle(Token.COMMENT_EOL).foreground; //requirement 8
	    
	    //*****************SET UNECCESSARIES TO BLACK******************|
	    scheme.getStyle(Token.COMMENT_KEYWORD).foreground = Color.BLACK;	 
	    scheme.getStyle(Token.SEPARATOR).foreground=Color.BLACK;				
	    scheme.getStyle(Token.LITERAL_BOOLEAN).foreground = Color.BLACK;
	    scheme.getStyle(Token.LITERAL_BOOLEAN).foreground = Color.BLACK;
        scheme.getStyle(Token.ERROR_IDENTIFIER).foreground = Color.BLACK;
        scheme.getStyle(Token.ERROR_NUMBER_FORMAT).foreground = Color.BLACK;
        scheme.getStyle(Token.ERROR_STRING_DOUBLE).foreground = Color.BLACK;
        scheme.getStyle(Token.ERROR_CHAR).foreground = Color.BLACK;
        scheme.getStyle(Token.ERROR_CHAR).foreground = Color.BLACK;
        scheme.getStyle(Token.COMMENT_DOCUMENTATION).foreground = Color.LIGHT_GRAY;
        scheme.getStyle(Token.COMMENT_EOL).foreground = Color.LIGHT_GRAY;
        scheme.getStyle(Token.COMMENT_MULTILINE).foreground = Color.LIGHT_GRAY;
        scheme.getStyle(Token.COMMENT_MARKUP).foreground=Color.YELLOW;
	    //*************************************************************|
        
        
	    textArea.setCodeFoldingEnabled(true);
	    textArea.setText(content);
	    textArea.revalidate();					//idk what it does
	    text_area_with_scroll = new RTextScrollPane(textArea);
	    
	    container.setLayout(new BorderLayout());
	    
	    container.add(text_area_with_scroll, BorderLayout.CENTER);
	    
	    count_label = new JLabel("Keywords: " + Integer.toString(count_all_keyword(content)));
	    
	    container.add(count_label, BorderLayout.SOUTH);
	    
	    /*the function belows will continuously count the number of keywords
	     * everytime a word gets removed/added/changed
	     */
	    textArea.getDocument().addDocumentListener
	    (new DocumentListener() 
		    {
				@Override
				public void insertUpdate(DocumentEvent e) {
					update_keyword_count();
					modified = true;
					MainFrame.save_file.setEnabled( true );
					if( projectFile )
						MainFrame.save_project.setEnabled( true );
					MainFrame.save_all.setEnabled( true );
				}
	
				@Override
				public void removeUpdate(DocumentEvent e) {
					update_keyword_count();
					modified = true;
					MainFrame.save_file.setEnabled( true );
					if( projectFile )
						MainFrame.save_project.setEnabled( true );
					MainFrame.save_all.setEnabled( true );
				}
	
				@Override
				public void changedUpdate(DocumentEvent e) {
					update_keyword_count();
					modified = true;
					MainFrame.save_file.setEnabled( true );
					if( projectFile )
						MainFrame.save_project.setEnabled( true );
					MainFrame.save_all.setEnabled( true );
				}
				protected void update_keyword_count() 
				{
	                try 
	                {
	                	int number = count_all_keyword(get_updated_content());
	                    count_label.setText("Keywords: " + Integer.toString(number));
	                } 
	                catch (Exception e) 
	                {
	                    System.out.println("Error in counting keywords");
	                }
	            }	
		    }
	    );
	}
	
	public String get_updated_content() {
		return textArea.getText();
	}
	
	public RSyntaxTextArea getRSTA() {
		return textArea;
	}
		
	//count_all_keyword calls count_single_keyword() on "while", "if", "else", and "for" 
	public int count_all_keyword(String content_of_file) 
	{
		if(content_of_file.length() > 0) 
		{
			
			int number = count_single_keyword(content_of_file,"if")+
					count_single_keyword(content_of_file,"else")+
					count_single_keyword(content_of_file,"while")+
					count_single_keyword(content_of_file,"for");
			return number;
		}
		return 0;
	}
	
	private int count_single_keyword(String content_of_file,String word_to_find) 
	{ 

		  int count = 0;  
		  Matcher matcher = Pattern.compile(
				  "\\b" + word_to_find +"\\b", 
				  Pattern.CASE_INSENSITIVE).matcher(content_of_file);
		  while (matcher.find()) 
			  count++;
		  return count;
	}  
}