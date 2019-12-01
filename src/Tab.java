import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/*Tab will contain file's information and text area*/

public class Tab 
{
	protected String tabName;
	protected String fileName;
	protected String content;
	protected String path;											
	protected File file;
	protected int count;
	private int keyWordCount;
	private int oldCount;
	protected boolean modified;
	protected boolean projectFile; 
	
	protected JPanel container = new JPanel();		
	protected RTextScrollPane scrollPane;				
	protected RSyntaxTextArea textArea = new RSyntaxTextArea(); 	
	protected JLabel keyWordLabel;
	
	public Tab(String content, String tabName, String path, File file, boolean projectFile )
	{
		this.path = path;
		this.tabName = tabName;
		fileName = tabName;
		this.content = content;
		this.file = file;
		this.projectFile = projectFile;
		modified = false;
		count = 0;
		
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
	    textArea.revalidate();
	    scrollPane = new RTextScrollPane(textArea);
	    
	    container.setLayout(new BorderLayout());
	    
	    container.add(scrollPane, BorderLayout.CENTER);
	    
	    countKeywords(content);
	    keyWordLabel = new JLabel( "Keywords: " + keyWordCount );
	    container.add(keyWordLabel, BorderLayout.SOUTH );
	    
	    /*the function belows will continuously count the number of keywords
	     * everytime a word gets removed/added/changed
	     */
	    textArea.getDocument().addDocumentListener
	    (new DocumentListener() 
		    {
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateKeywordCount(getUpdatedContent());
					modified = true;
					MainFrame.save_file.setEnabled( true );
					if( projectFile )
						MainFrame.save_project.setEnabled( true );
					MainFrame.save_all.setEnabled( true );
				}
	
				@Override
				public void removeUpdate(DocumentEvent e) {
					updateKeywordCount(getUpdatedContent());
					modified = true;
					MainFrame.save_file.setEnabled( true );
					if( projectFile )
						MainFrame.save_project.setEnabled( true );
					MainFrame.save_all.setEnabled( true );
				}
	
				@Override
				public void changedUpdate(DocumentEvent e) {
				}	
		    }
	    );
	    textArea.setCaretPosition(0);
	}
	
	public String getUpdatedContent() {
		return textArea.getText();
	}
	
	public RSyntaxTextArea getRSTA() {
		return textArea;
	}
	
	private void updateKeywordCount(String fileContents) 
	{	
		countKeywords( fileContents );
		keyWordLabel.setText("Keywords: " + keyWordCount );
    }
	
	private void countKeywords(String fileContents) 
	{		
		
		if( fileContents.length() > 0 )
		{ 
			oldCount = keyWordCount;
			keyWordCount = 0;
			try 
			{
				new VoidVisitorAdapter<Object>() {
				    @Override
				    public void visit(IfStmt n, Object arg) {
				    	super.visit(n, arg);
				    	if( n.hasElseBranch() ) 
				    	{
				    		keyWordCount++;
				    	}
				        keyWordCount++;
				    }
				    public void visit(ForStmt n, Object arg) {
				        super.visit(n, arg);
				        keyWordCount++;
				    }
				    public void visit(WhileStmt n, Object arg) {
				        super.visit(n, arg);
				        keyWordCount++;
				    }
				    public void visit(ForEachStmt n, Object arg) {
				        super.visit(n, arg);
				        keyWordCount++;
				    }
				    public void visit(DoStmt n, Object arg) {
				        super.visit(n, arg);
				        keyWordCount++;
				    }
				}.visit(StaticJavaParser.parse(fileContents), null);
			}
			catch( Exception e ) 
			{
				keyWordCount = oldCount;
			}
		}
		else
			keyWordCount = 0;
	} 
}