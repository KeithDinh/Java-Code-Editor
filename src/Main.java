import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;
import javax.swing.event.*;
import java.util.function.*;
import javax.swing.border.*;
//Adding this comment to see if i can push to the repository.ZB
// For coloring change JTextArea to JTextPane, we will have to update the way that the line numbers work

// TODO:
// 1) Test for possible bugs
// 2) Keyword count real-time and for loaded files?
// 3) Syntax Coloring real-time and loaded files?
// 4) clean up dead code and organize code to be more readable. There is A LOT of code that is superfluous that can be simplified 
// 5) create Execute function / menu item , if there is a compilation error, run the last successful build ( class files ), or should we build first? 


public class Main extends JFrame implements ActionListener, WindowListener, DocumentListener
{
	private final static int width = 640; // 640 
	private final static int height = 480; // 480 
	private final static String fontName = "Courier New"; // will this work on linux?
	private final static int fontSize = 14;
	//Declare controls used
	private JTextArea ta;
	private LineNumberingTextArea lnt;
	private JScrollPane sp;
	private JMenu view;
	private JPanel jpanel; 
	private JLabel statusLabel1;
	private JLabel statusLabel2;
	private JLabel statusLabel3;
	
	// Output Window
	private JTextArea outputText;
	private JScrollPane scrollPane;
	private JDialog dialog;

	private JMenuItem miFileSave;
	private JMenuItem miFileSaveAll;
	private JMenuItem miAddFile;
	private JMenuItem miRemoveFile;
	private JMenuItem miBuildCompile;
	private JMenuItem miBuildCompileAll;
	private JMenuItem miCloseProject;
	private JMenuItem miExecute;
	private ArrayList<JMenuItem> viewFiles;
	private int currentViewIndex;

	private String projectDirectory;
	private ArrayList<SourceFile> sourceFiles;
	ArrayList<String> cache;
	private int currentSourceFileIndex;//index of the current Source File is in ArrayList sourceFiles
	private boolean compilationError = false;
	private boolean switching = false;

	
	private class SourceFile
	{
		private String directory; // getDirectory returns the directory
		private String fileName; // getFileName returns the fileName
		private String path; // getPath returns path
		private boolean loaded; // isLoaded returns loaded
		private boolean modified; // isModified returns modified
		private String contents; // getContens returns contents
		private int KeyWordCount = 0; // number of keywords in the file
		private boolean keyWordExists = false;
		
		public SourceFile( String directory, String fileName, String path )
		{
			this.directory = directory;
			this.fileName = fileName;
			this.path = path;
			this.loaded = false;
			this.modified = false;
			this.contents = null;
		}
		public SourceFile( String directory, String fileName, String path, String contents )
		{
			this.directory = directory;
			this.fileName = fileName;
			this.path = path;
			this.loaded = true;
			this.modified = false;
			this.contents = contents;
		}
		public String getDirectory()
		{
			return directory;
		}
		public String getFileName()
		{
			return fileName;
		}
		public String getPath()
		{
			return path;
		}
		public boolean isLoaded() // not used yet
		{
			return loaded;
		}
		public boolean isModified()
		{
			return modified;
		}
		public void setModified( boolean flag )
		{
			modified = flag;
		}
		public String getContents()
		{
			return contents;
		}
		public void setContents( String s )
		{
			contents = s;
			loaded = true;
			modified = false;
		}
		public int getKeyWordCount() 
		{
			return KeyWordCount;
		}
		public void setKeyWordCount(int KeyWordCount ) 
		{
			this.KeyWordCount = KeyWordCount;
			keyWordExists = true;
		}
		public void KeyWordExists( boolean flag ) 
		{
			keyWordExists = flag;
		}
		public boolean KeyWordExists( ) 
		{
			return keyWordExists;
		}
		
	}//end SourceFile class
	
	
	/**Description: Switch the current source file to the source file fileName.
	 * This function will check whether if the current source file is modified 
	 * in order to save the current source file before switching to fileName source file.
	 * @param String fileName is name of the file is 
	 */
	private void switchFile( String fileName ) 
	{
		switching = true;
		boolean flag = miFileSaveAll.isEnabled();//check if Save All menuItem is enabled or not. 
		int oldSourceFileIndex = currentSourceFileIndex;
		boolean modified = sourceFiles.get( currentSourceFileIndex ).isModified(); //check modification mode 
		
		if( sourceFiles == null || sourceFiles.size() < 1 ) 
		{
			switching = false;
			return;
		}
		//temporarily store the currentSourceFile's content into cache at index currentSourceFileIndex
		cache.set( currentSourceFileIndex, ta.getText() );
		
		for( int i = 0; i < sourceFiles.size(); i++ ) 
		{
			//find the index i of fileName in the ArrayList SourceFiles
			if( sourceFiles.get( i ).getFileName().equals( fileName ) )
			{	//pull the fileName's content from cache to main text area ta
				if( cache.get( i ) == null )
					ta.setText( " " );
				else 
					ta.setText( cache.get( i ) );
				
				miFileSave.setText( "Save " + fileName );
				miBuildCompile.setText( "Compile " + fileName );
				currentSourceFileIndex = i;//set currentSourceFileIndex equal to fileName's index
				// keep original modified values
				miFileSaveAll.setEnabled( flag );
				sourceFiles.get( oldSourceFileIndex ).setModified( modified );
				miFileSave.setEnabled( sourceFiles.get( currentSourceFileIndex ).isModified() );
				
				switching = false;
				return; // change to return
			}
		}
	}
	private void showSourceFile( String fileName )
	{
		switching = true;
		System.out.println( fileName );
		System.out.println( sourceFiles.get( 0 ).getFileName() );
		if( sourceFiles == null || sourceFiles.size() < 1 )
			return;
		SourceFile sf;
		for( int i = 0; i < sourceFiles.size(); i++ )
		{
			sf = sourceFiles.get( i );
			if( sf.getFileName().equals( fileName ) )
			{
				if( !sf.loaded ) 
				{
					switching = false;
					return;
				}
				String contents = sf.getContents();
				if( contents == null || contents.equals( " " ) || contents.equals( "" ) ) 
				{
					sf.setContents( "public class Main \n{\n\tpublic static void main( String args[] )\n\t{\n\n\t}\n}" ); 
					ta.setText( "public class Main \n{\n\tpublic static void main( String args[] )\n\t{\n\n\t}\n}" );
				}
				else 
				{
					ta.setText( contents );
				}
				lnt.setEnabled( true );
				ta.setEnabled( true );
				sp.setRowHeaderView( lnt );
				miFileSave.setText( "Save " + fileName );
				if( sf.isModified() )
				{
					miFileSave.setEnabled( true );
					miFileSaveAll.setEnabled( true );
				}
				else
				{
					miFileSave.setEnabled( false );
					miFileSaveAll.setEnabled( false );
				}
				miBuildCompile.setText( "Compile " + fileName );
				miBuildCompile.setEnabled( true );
				miBuildCompileAll.setEnabled( true );
				miAddFile.setEnabled( true );
				if( sourceFiles.size() > 1 )
					miRemoveFile.setEnabled( true );
				view.setEnabled( true );
				currentSourceFileIndex = i;
				switching = false;
				return;
			}
		}
	}
	public void setFileSaveAllEnabled() 
	{
		if( sourceFiles == null )
		{
			miFileSaveAll.setEnabled( false );
			return;	
		}
		for( int i = 0; i < sourceFiles.size(); i++ )
		{
			if( sourceFiles.get( i ).isModified() )
			{
				miFileSaveAll.setEnabled( true );
				return;
			}
		}
		miFileSaveAll.setEnabled( false );
	}

	public Main()
	{
		cache = new ArrayList<String>();
		
		currentSourceFileIndex = -1;
		setTitle( "Code Editor" );
		setSize( width, height );
		setLocationRelativeTo( null );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	
		// Bottom Status Panel
		jpanel = new JPanel();
		jpanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		jpanel.setPreferredSize(new Dimension(width, 20));
		jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.X_AXIS));
		add(jpanel, BorderLayout.SOUTH);
		
		statusLabel1 = new JLabel("Keywords: 0");
		statusLabel1.setHorizontalAlignment(SwingConstants.LEFT);
		jpanel.add( statusLabel1 );
		
		statusLabel2 = new JLabel(" ");
		statusLabel2.setHorizontalAlignment(SwingConstants.LEFT);
		jpanel.add( statusLabel2 );
		
		statusLabel3 = new JLabel(" ");
		statusLabel3.setHorizontalAlignment(SwingConstants.LEFT);
		jpanel.add( statusLabel3 );
		
		JMenuBar mb = new JMenuBar();
		JMenu m;
		JMenuItem mi;

		// File

		m = new JMenu( "File" );
		m.setMnemonic( KeyEvent.VK_F );

		// File > New Project

		mi = new JMenuItem( "New Project..." );
		mi.setMnemonic( KeyEvent.VK_N );
		mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK ) );
		mi.addActionListener( this );
		m.add( mi );

		// File > Open Project

		mi = new JMenuItem( "Open Project..." );
		mi.setMnemonic( KeyEvent.VK_O );
		mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK ) );
		mi.addActionListener( this );
		m.add( mi );
		
		// File > Close Project
		//build the "Close Project" menu item 
		miCloseProject = new JMenuItem( "Close Project" );
		miCloseProject.addActionListener( this );
		miCloseProject.setEnabled( false );//set enable mode  to false when no  project is opened.
		m.add( miCloseProject );//add Close Project menu item to File JMenu
		
		// File > New File
		
		m.addSeparator();
		miAddFile = new JMenuItem( "New File" );
		miAddFile.setEnabled( false );
		miAddFile.addActionListener( this );
		m.add( miAddFile );
		
		// File > Remove File
		miRemoveFile = new JMenuItem( "Remove File" );
		miRemoveFile.setEnabled( false );
		miRemoveFile.addActionListener( this );
		m.add( miRemoveFile );

		// File > Save

		m.addSeparator();
		miFileSave = new JMenuItem( "Save" );
		miFileSave.setMnemonic( KeyEvent.VK_S );
		miFileSave.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK ) );
		miFileSave.setEnabled( false );
		miFileSave.addActionListener( this );
		m.add( miFileSave );

		// File > Save All

		miFileSaveAll = new JMenuItem( "Save All" );
		miFileSaveAll.setMnemonic( KeyEvent.VK_A );
		miFileSaveAll.setDisplayedMnemonicIndex( 5 );
		miFileSaveAll.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK ) );
		miFileSaveAll.setEnabled( false );
		miFileSaveAll.addActionListener( this );
		m.add( miFileSaveAll );
		
		// File > Exit

		mi = new JMenuItem( "Exit" );
		mi.setMnemonic( KeyEvent.VK_X );
		mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK ) );
		mi.addActionListener( this );
		m.add( mi );

		mb.add( m ); 

		// Edit
		m = new JMenu( "Edit" );
		m.setMnemonic( KeyEvent.VK_E );

		mi = new JMenuItem( "Cut" );
		mi.setMnemonic( KeyEvent.VK_CUT );
		mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK ) );
		mi.setEnabled( false );
		mi.addActionListener( this );
		m.add( mi );

		mi = new JMenuItem( "Copy" );
		mi.setMnemonic( KeyEvent.VK_COPY );
		mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK ) );
		mi.setEnabled( false );
		mi.addActionListener( this );
		m.add( mi );

		mi = new JMenuItem( "Paste" );
		mi.setMnemonic( KeyEvent.VK_PASTE );
		mi.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK ) );
		mi.setEnabled( false );
		mi.addActionListener( this );
		m.add( mi );

		mb.add( m );

		// View

		view = new JMenu( "View" );
		view.setMnemonic( KeyEvent.VK_V );
		view.setEnabled( false );
		mb.add ( view );

		// Build

		m = new JMenu( "Build" );
		m.setMnemonic( KeyEvent.VK_B );

		miBuildCompile = new JMenuItem( "Compile" );
		miBuildCompile.setMnemonic( KeyEvent.VK_C );
		miBuildCompile.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK ) );
		miBuildCompile.setEnabled( false );
		miBuildCompile.addActionListener( this );
		m.add( miBuildCompile );

		miBuildCompileAll = new JMenuItem( "Compile All" );
		miBuildCompileAll.setMnemonic( KeyEvent.VK_A );
		miBuildCompileAll.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK ) );
		miBuildCompileAll.setEnabled( false );
		miBuildCompileAll.addActionListener( this );
		m.add( miBuildCompileAll );

		miExecute = new JMenuItem( "Execute" );
		miExecute.setMnemonic( KeyEvent.VK_X );
		miExecute.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F5, 0 ) );
		miExecute.setEnabled( false ); // need to add menu item private variable  
		miExecute.addActionListener( this );
		m.add( miExecute );

		mb.add( m );

		setJMenuBar( mb );

		// Text Area for code
		ta = new JTextArea();
		ta.setVisible( false );
		ta.setEnabled( false );
		ta.setFont( new Font( fontName, Font.PLAIN, fontSize ) );
		ta.getDocument().addDocumentListener( this );
		sp = new JScrollPane( ta );
		getContentPane().add( sp );
		
		// Line numbers for the Text Area
		lnt = new LineNumberingTextArea( ta );
		lnt.setEnabled( false );
		
		// Console
		
		dialog = new JDialog();
		scrollPane = new JScrollPane();
		outputText = new JTextArea();
		
		outputText.setEditable( false );
		outputText.setFont( new Font("Courier New", Font.PLAIN, 14));
		outputText.setForeground( Color.white );
		outputText.setBackground( Color.black );
		
		dialog.addWindowListener(new WindowAdapter()
	    {
	      public void windowClosed(WindowEvent e)
	      {
	      }
	      public void windowClosing(WindowEvent e)
	      {
	    	  outputText.setText( "" );
	      }
	    });
		
		// WindowListener for JFrame
		addWindowListener( this );
		

	}

	// File > New Project...

	private void fileNewProject() // remember to set projectDirectory and status bar
	{
		int result = JOptionPane.showConfirmDialog(null, "Choose a directory, this is where the project folder will be saved", null, JOptionPane.OK_CANCEL_OPTION);
		if( result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
			return;
	
		JFileChooser c = new JFileChooser();

		c.setDialogTitle( "Select A Workspace" );
		c.setCurrentDirectory( new java.io.File( "." ) );
		c.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		c.setAcceptAllFileFilterUsed( false );
		c.setMultiSelectionEnabled( false );

		if( c.showOpenDialog( this ) != JFileChooser.APPROVE_OPTION ) 
			return;	
		
		String d = c.getSelectedFile().getPath();
		
		// Name the Project Folder
		
		String folderName = JOptionPane.showInputDialog(null, "Choose a project name");
		if( folderName == null )
			return;
		
		if( new File( d + "\\" + folderName ).exists() ) 
		{
			JOptionPane.showMessageDialog(null, "Folder already exists", null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		new File(d + "\\" + folderName).mkdir();
		
		if( !new File(d + "\\" + folderName).exists() )
		{
			JOptionPane.showMessageDialog(null, "Cant't create folder, possible illegal character(s)", null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		d = d + "\\" + folderName;
		ArrayList<SourceFile> list = new ArrayList<SourceFile>();
		
		// create Main.java first
		FileWriter file = null;
		String filePath = d + "\\Main.java";
		String contents = "public class Main\n{\tpublic static void main( String Args[] )\n\t{\n\n\t}\n}";
		
		try
		{
			file = new FileWriter( new File( filePath ) );
			file.write( contents );
			list.add( new SourceFile( d, "Main.java", filePath, contents ) );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, "Unable to create file", null, JOptionPane.WARNING_MESSAGE);
			return;
		}
		finally
		{
			try { if( file != null ) file.close(); } catch( Exception ee ) { }
		}
		
		// if file was successfully created then write then reset everything if necessary,  create sourceFiles, viewFiles, etc.......
		if( sourceFiles != null)
			CloseProject(); 
		
		sourceFiles = list;
		projectDirectory = d;
		
		cache.add( contents );
		viewFiles = new ArrayList<JMenuItem>();
		currentViewIndex = 0;
		for( int i = 0; i < list.size(); i++ )
		{
			viewFiles.add(new JMenuItem( list.get( i ).getFileName() )); 
			if( list.get( i ).getFileName().equals( "Main.java" ) )
				currentViewIndex = i;
			viewFiles.get( i ).addActionListener( this );
			view.add( viewFiles.get( i ) );
		}
		showSourceFile("Main.java");
		
		// Highlight Main.java
		viewFiles.get( currentViewIndex ).setForeground(new Color(191, 191, 191));
		
		// Updating the Status Bar
		if( !sourceFiles.get( currentSourceFileIndex ).KeyWordExists() )
			statusLabel1.setText("KeyWords: 0");
		else
			statusLabel1.setText("Keywords: " + sourceFiles.get( currentSourceFileIndex ).getKeyWordCount());
		
		statusLabel2.setText( "   " + projectDirectory );
		statusLabel3.setText("   Main.java");
		
		// create status labels
		
		miCloseProject.setEnabled( true );
		view.setEnabled( true );
		ta.setVisible( true );
		lnt.setVisible( true );
		return;
	}	
	
	// File > Open Project...

	private void fileOpenProject()
	{	
		switching = true;
		JFileChooser c = new JFileChooser();

		c.setDialogTitle( "Select Project Directory" );
		c.setCurrentDirectory( new java.io.File( "." ) );
		c.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		c.setAcceptAllFileFilterUsed( false );
		c.setMultiSelectionEnabled( false );

		if( c.showOpenDialog( this ) != JFileChooser.APPROVE_OPTION ) 
		{
			switching = false;
			return;	
		}

		String d = c.getSelectedFile().getPath();
		
		// if the user is not in the src file, check if a src file exists and go the that directory, else, continue
		// this way if the user is opening a project from eclipse where a src folder exists
		// they do not have to click on the src folder, only the project directory
		if( !d.endsWith( "src" ) ) 
		{
			File f = new File(d + "\\src");
			if( f.exists() && f.isDirectory() ) 
			{
				d = f.getPath();
			}
		}
	
		if( sourceFiles != null ) 
		{
			if( d.equals( projectDirectory ) ) 
			{
				JOptionPane.showMessageDialog(null, "Project is already open" );
				switching = false;
				return;
			}
			if( miFileSaveAll.isEnabled() || miFileSave.isEnabled() ) 
			{
				int result = JOptionPane.showConfirmDialog(null, "You have unsaved work, do you want to save before exiting?", null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(result == JOptionPane.YES_OPTION) 
					fileSaveAll(); 
				else if(result == JOptionPane.CANCEL_OPTION) 
				{
					switching = false;
					return;
				}
			}
			cache.clear();
		}
		
		ArrayList<SourceFile> list = new ArrayList<SourceFile>();
		File[] files = new File( d ).listFiles();
		if( files == null )
		{
			JOptionPane.showMessageDialog(null, "Could not find Main.java", "Error", JOptionPane.ERROR_MESSAGE);
			switching = false;
			return;
		}
		boolean foundMain = false;
		JMenuItem mi;
		try
		{
			for( int i = 0; i < files.length; i++ )
			{
				if( files[i].isFile() && files[i].getName().endsWith( ".java" ) )
				{
					if( files[i].getName().equals( "Main.java" ) )
					{
						list.add( new SourceFile( d, files[i].getName(), files[i].getPath(), new String( Files.readAllBytes( Paths.get( files[i].getPath() ) ), StandardCharsets.UTF_8 ) ) ); 
						cache.add( new String( Files.readAllBytes( Paths.get( files[i].getPath() ) ) ) );
						foundMain = true;
					}
					else
					{
						list.add( new SourceFile( d, files[i].getName(), files[i].getPath(), new String( Files.readAllBytes( Paths.get( files[i].getPath() ) ), StandardCharsets.UTF_8 ) ) );
						cache.add( new String( Files.readAllBytes( Paths.get( files[i].getPath() ) ) ) );
					}
					
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			switching = false;
			return;
		}
		if( !foundMain )
		{
			JOptionPane.showMessageDialog(null, "Could not find Main.java", "Error", JOptionPane.ERROR_MESSAGE);
			switching = false;
			return;
		}

		projectDirectory = d;
		sourceFiles = list;
		
		
		view.removeAll();
		viewFiles = new ArrayList<JMenuItem>();
		currentViewIndex = 0;
		for( int i = 0; i < list.size(); i++ )
		{
			viewFiles.add(new JMenuItem( list.get( i ).getFileName() )); 
			if( list.get( i ).getFileName().equals( "Main.java" ) )
				currentViewIndex = i;
			viewFiles.get( i ).addActionListener( this );
			view.add( viewFiles.get( i ) );
		}
		showSourceFile( "Main.java" );
		
		// highlight current file 
		viewFiles.get( currentViewIndex ).setForeground(new Color(191, 191, 191));
		
		// Updating the Status Bar
		if( !sourceFiles.get( currentSourceFileIndex ).KeyWordExists() )
			statusLabel1.setText("KeyWords: 0");
		else
			statusLabel1.setText("Keywords: " + sourceFiles.get( currentSourceFileIndex ).getKeyWordCount());
		
		statusLabel2.setText( "   " + projectDirectory );
		statusLabel3.setText("   Main.java");
		
		switching = false;
		miCloseProject.setEnabled( true );
		ta.setVisible( true );
		lnt.setVisible( true );

		return;
		
	}
	
	// File > New File
	
	private void AddFile() 
	{
		String fileName = JOptionPane.showInputDialog(null, "Enter the name of the new .java file", "Add File", JOptionPane.PLAIN_MESSAGE);
		if( fileName == null ) // this means that user clicked cancel or nothing
			return;
		if( fileName.equals( "Main.java" ) || fileName.equals( "main.java" ) ) 
		{
				JOptionPane.showMessageDialog(null, "Cannot name file " + fileName, null, JOptionPane.ERROR_MESSAGE);
				return;
		}
		if( !fileName.endsWith( ".java" ) )
		{
			fileName = fileName + ".java";
		}
		// java class can only contains letters and numbers, must start with a letter, no spaces 
		String temp = fileName.substring( 0, fileName.length() - 5);   
		if( !Character.isLetter( temp.charAt( 0 ) ) || !temp.matches("[a-zA-Z0-9]*" ) )
		{
					JOptionPane.showMessageDialog(null, "Illegal character(s) in file name\nFile name can only contain letters and numbers and must start with a letter", null, JOptionPane.ERROR_MESSAGE);
					return;
		}
		for( int i = 0; i < sourceFiles.size(); i++) 
		{
			if(fileName.equals( sourceFiles.get( i ).getFileName() ) ) 
			{
				JOptionPane.showMessageDialog(null, "File already exists", null, JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		FileWriter file = null;
		String filePath = projectDirectory + "\\" + fileName;
		int endIndex = fileName.length() - 5;
		String contents = "public class " + fileName.substring(0, endIndex) + "\n{\n\n}";
		try
		{
			file = new FileWriter( new File( filePath ) );
			file.write( contents );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, "Unable to create file", null, JOptionPane.WARNING_MESSAGE);
			return;
		}
		finally
		{
			try { if( file != null ) file.close(); } catch( Exception ee ) { }
		}
		
		// update sourceFiles, viewFiles, and cache 
		sourceFiles.add( new SourceFile( projectDirectory, fileName, filePath, contents ) ); // add to the end of the sourceFile
		cache.add( contents );
		viewFiles.add( new JMenuItem( fileName ) );
		view.add( viewFiles.get( viewFiles.size() - 1 ) ); 
		switchViewFileMenuItem( fileName );
		viewFiles.get( currentViewIndex ).addActionListener( this );
		miRemoveFile.setEnabled( true ); // if you add a file there will always be at least 2 files, so set Remove File to enabled
	}
	
	// File > Remove File
	
	private void RemoveFile() // Error, throws error out of bounds
	{
		// cannot remove Main.java, cannot add duplicate files, update sourceFiles, viewFiles, cache 
		String fileNames[] = new String[sourceFiles.size() - 1];
		int count = 0;
		for( int i = 0; i < sourceFiles.size(); i++) 
		{
			if( !sourceFiles.get( i ).getFileName().equals( "Main.java" ) &&  !sourceFiles.get( i ).getFileName().equals( "main.java" ) ) 
			{
				fileNames[count] = sourceFiles.get( i ).getFileName();
				count++;
			}
				
		}
		String fileName = ( String )JOptionPane.showInputDialog(null, "Choose a file to remove", "Remove File", JOptionPane.PLAIN_MESSAGE, null, fileNames, fileNames[count - 1]);
		if( fileName == null ) // this means that user clicked cancel 
			return;
		if(sourceFiles.size() < 2)
			miRemoveFile.setEnabled( false );
		
		String filePath = null;
		
		// switch to Main.java if the file being deleted is currently being viewed
		if( sourceFiles.get( currentSourceFileIndex ).getFileName().equals( fileName ) )
			switchViewFileMenuItem( "Main.java" );
		
		// shifts all the indices to the left 
		for( int i = 0; i < sourceFiles.size(); i++) 
		{
			if( sourceFiles.get( i ).getFileName().equals( fileName ) && cache.get( i ).equals( sourceFiles.get( i ).getContents() ) && viewFiles.get( i ).getText().equals( sourceFiles.get( i ).getFileName() ) ) 
				{
					filePath = sourceFiles.get( i ).getPath();
					cache.remove( i );
					viewFiles.remove( i );
					view.remove( i );
					sourceFiles.remove( i );
					if( i < currentViewIndex ) 
					{
						currentViewIndex--;
						currentSourceFileIndex--;
					}
					break;
				
				}
		} 
		
		// Delete the file
		
		try
        { 
            Files.deleteIfExists(Paths.get( filePath ) ); 
        } 
        catch(NoSuchFileException e) 
        { 
            JOptionPane.showMessageDialog(null, "No such file/directory exists", null, JOptionPane.ERROR_MESSAGE); 
        } 
        catch(DirectoryNotEmptyException e) 
        { 
        	JOptionPane.showMessageDialog(null, "Directory is not empty", null, JOptionPane.ERROR_MESSAGE);
        } 
        catch(IOException e) 
        { 
        	JOptionPane.showMessageDialog(null, "Invalid permissions", null, JOptionPane.ERROR_MESSAGE);
        } 
        finally 
        {
            System.out.println("Deletion successful.");
        }   
		
		if( sourceFiles.size() < 2 ) 
			miRemoveFile.setEnabled( false );
		
	}
	
	// File > Save
	
	private void fileSave()
	{
		if( currentSourceFileIndex < 0 )  // this should never happen
		{
			miFileSave.setEnabled( false );
			miFileSaveAll.setEnabled( false );
			return;
		}
		
		FileWriter file = null;
		try
		{
			file = new FileWriter( new File( sourceFiles.get( currentSourceFileIndex ).getPath() ) );
			file.write( ta.getText() );
			miFileSave.setEnabled( false );
			sourceFiles.get( currentSourceFileIndex ).setModified( false );
			// DEBUG
			for( int i = 0; i < sourceFiles.size(); i++ ) 
			{
				if(sourceFiles.get( i ).isModified()) 
				{
					System.out.println(sourceFiles.get( i ).getFileName() + " is modified" );
				}
				else 
					System.out.println(sourceFiles.get( i ).getFileName() + " is not modified" );
			}
			setFileSaveAllEnabled();
			return;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, "Unable to save file", null, JOptionPane.WARNING_MESSAGE);
			return;
		}
		finally
		{
			try { if( file != null ) file.close(); } catch( Exception ee ) { }
		}
	}
	private void fileSaveAll()
	{
		if( currentSourceFileIndex < 0 )  // this should never happen
		{
			miFileSave.setEnabled( false );
			miFileSaveAll.setEnabled( false );
			return;
		}
		
		FileWriter file = null;
		
		// set the cache of the currentSourceFileIndex or the current file being viewed
		cache.set( currentSourceFileIndex, ta.getText() );
		
		for(int i = 0; i < sourceFiles.size(); i++) 
		{
			try
			{
				//sourceFiles.get( i ).setContents( cache.get( i ));
				file = new FileWriter( new File( sourceFiles.get( i ).getPath() ) );
				file.write( cache.get( i ) ); // sourceFiles.get( i ).getContents()
				sourceFiles.get( i ).setModified( false );
			}
			catch( Exception e )
			{
				e.printStackTrace();
				JOptionPane.showConfirmDialog(null, "Unable to save files", null, JOptionPane.WARNING_MESSAGE);
			}
			finally
			{
				try { if( file != null ) file.close(); } catch( Exception ee ) { }
			}
		}
		miFileSave.setEnabled( false );
		miFileSaveAll.setEnabled( false );
		return;
	}
	private void buildCompile() 
	{
		SourceFile sf = sourceFiles.get( currentSourceFileIndex );
		
		compilationError = false;
		
		dialog.setVisible( true );
		dialog.getContentPane().add(scrollPane);
		scrollPane.setViewportView( outputText );
		dialog.setTitle( "Console" );
		dialog.setSize(700, 300);
		
		outputText.append("Compiling " + sf.getDirectory() + File.separator + sf.getFileName() + "... ");
		System.out.print( "Compiling " + sf.getDirectory() + File.separator + sf.getFileName() + "... " );
		System.out.flush();
		
		// According to the instructions, we can assume that there is a file called Main.java
		// and we can assume that there is public static main method
		
		Compile.CompilationResult r = new Compile( sf.getDirectory(), sf.getFileName() ).compile();
		if( r.success )
		{
			outputText.append( "done" );
			System.out.println( "done" );
		}
		else
		{
			compilationError = true;
			outputText.append( "failed" );
			outputText.append( "\n" );
			System.out.println( "failed" );
			if( r.javacOutput != null && r.javacOutputComplete )
			{
				outputText.append( r.javacOutput.toString() );
				System.out.print( r.javacOutput.toString() );
			}
			else
			{
				outputText.append( r.errorMessage );
				System.out.println( r.errorMessage );
			}
		}
		outputText.append("\n" + "\n");
	}
	private void buildCompileAll()
	{
		compilationError = false;
		
		dialog.setVisible( true );
		dialog.getContentPane().add(scrollPane);
		dialog.setTitle( "Console" );
		dialog.setSize(700, 300);
		scrollPane.setViewportView( outputText );
		dialog.setModalityType(null);
		
		for(int i = 0; i < sourceFiles.size(); i++)
		{
			SourceFile sf = sourceFiles.get( i );
			outputText.append("Compiling " + sf.getDirectory() + File.separator + sf.getFileName() + "... ");
			System.out.print( "Compiling " + sf.getDirectory() + File.separator + sf.getFileName() + "... " );
			System.out.flush();
		
			// According to the instructions, we can assume that there is a file called Main.java
			// and we can assume that there is public static void main method
		
			Compile.CompilationResult r = new Compile( sf.getDirectory(), sf.getFileName() ).compile();
			if( r.success )
			{
				outputText.append( "done" );
				System.out.println( "done" );
			}
			else
			{
				compilationError = true;
				outputText.append( "failed\n" );
				System.out.println( "failed" );
				if( r.javacOutput != null && r.javacOutputComplete )
				{
					outputText.append( r.javacOutput.toString() );
					System.out.print( r.javacOutput.toString() );
				}
				else
				{
					outputText.append( r.errorMessage );
					System.out.println( r.errorMessage );
				}
			}
			outputText.append("\n");
		}
		outputText.append("\n");
	}
	
	// Execute 
	
	public void Execute( ) 
	{ 
		// 
	}
	
	// interface ActionListener

	public void actionPerformed( ActionEvent e )
	{
		System.out.println( e.getActionCommand() );
		if( e.getActionCommand().endsWith( ".java" ) && !e.getActionCommand().contains( " " ) ) 
		{
			switchViewFileMenuItem( e.getActionCommand().toString() );
		}
		else if( e.getActionCommand().equals( "Open Project..." ) )
		{
			fileOpenProject();
		}
		else if(e.getActionCommand().equals( "New Project..." ) )
		{
			fileNewProject();
		}
		else if( e.getActionCommand().startsWith( "Save" ) && !e.getActionCommand().equals( "Save All" ) )
		{
			fileSave();
		}
		else if( e.getActionCommand().startsWith( "Save All" ) )
		{
			fileSaveAll();
		}
		else if( e.getActionCommand().startsWith( "Compile" ) && !e.getActionCommand().equals( "Compile All" ) )
		{
			fileSave();
			buildCompile();
		}
		else if( e.getActionCommand().startsWith( "Compile All" ) )
		{
			fileSaveAll(); 
			buildCompileAll();
		}
		else if(e.getActionCommand().startsWith( "Execute" ) )
		{
			if(compilationError) 
			{
				int result = JOptionPane.showConfirmDialog(null, "There were build errors, would you like to run your last successful build?", null, JOptionPane.YES_NO_OPTION);
				if( result == JOptionPane.NO_OPTION ) 
				{
					return;
				}
			}
			Execute();
		}
		else if( e.getActionCommand().startsWith( "New File" ) ) 
		{
			AddFile();
		}
		else if( e.getActionCommand().startsWith( "Remove File" ) ) 
		{
			RemoveFile();
		}
		else if( e.getActionCommand().startsWith( "Exit" ) ) 
		{	
			ExitMessage();
		}
		else if( e.getActionCommand().startsWith( "Close Project" ) ) 
		{
			CloseProject();
			ta.setVisible( false );
			lnt.setVisible( false );
		}
	}
	
	// File > Close Project
	
	/**Description: This function is to close the opened project. T
	 * This function will check if there is any unsaved source file,and prompt to user 
	 * to ask for further instruction in order to save or ignore the unsaved file.  
	 */
	public void CloseProject() 
	{
		if(miFileSaveAll.isEnabled() || miFileSave.isEnabled()) 
		{
			//ignore if windowClosing is clicked if there is unsaved file exists
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			//prompt to user and get option. 
			int result = JOptionPane.showConfirmDialog(null, 
					"You have unsaved work, do you want to save before closing?", 
					null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if(result == JOptionPane.YES_OPTION) 
			{
				fileSaveAll();
			}
			else if( result == JOptionPane.CANCEL_OPTION)
				return;
		}
		//deallocate memory and reset all the modes
		cache.clear();
		sourceFiles.clear();
		view.removeAll(); 
		viewFiles.clear(); 
		projectDirectory = "";
		miFileSave.setEnabled( false );
		miFileSaveAll.setEnabled( false );
		miBuildCompile.setEnabled( false );
		miBuildCompileAll.setEnabled( false );
		view.setEnabled( false );
		miExecute.setEnabled( false );
		miAddFile.setEnabled( false );
		miRemoveFile.setEnabled( false );
		miCloseProject.setEnabled( false );
		statusLabel2.setText(" ");
		statusLabel3.setText(" ");
	} 
	
	public void switchViewFileMenuItem( String actionCommand ) 
	{
		viewFiles.get( currentViewIndex ).setForeground(Color.black); 
		for(int i = 0; i < viewFiles.size(); i++) 
		{
			if( viewFiles.get( i ).getText().equals( actionCommand ) ) 
			{
				currentViewIndex = i;
				viewFiles.get( i ).setForeground(new Color(191, 191, 191));
				break;
			}
		}
		switchFile( actionCommand );
		if( !sourceFiles.get( currentSourceFileIndex ).KeyWordExists() )
			statusLabel1.setText("KeyWords: 0");
		else
			statusLabel1.setText("Keywords: " + sourceFiles.get( currentSourceFileIndex ).getKeyWordCount());
		
		statusLabel3.setText("   " + sourceFiles.get( currentSourceFileIndex).getFileName());
	}
	
	public void ExitMessage() 
	{
		if(miFileSaveAll.isEnabled() || miFileSave.isEnabled()) 
		{
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			int result = JOptionPane.showConfirmDialog(null, "You have unsaved work, do you want to save before exiting?", null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(result == JOptionPane.YES_OPTION) 
			{
				fileSaveAll(); 
				System.exit(0);
			}
			else if(result == JOptionPane.NO_OPTION) 
			{
				System.exit(0);
			}
		}
		else
			System.exit(0);
	} 
	// interface WindowListener methods
	public void windowActivated( WindowEvent e )
	{
	}
	public void windowClosed( WindowEvent e )
	{
	}
	public void windowClosing( WindowEvent e )
	{
		ExitMessage();
	}
	public void windowDeactivated( WindowEvent e )
	{
	}
	public void windowDeiconified( WindowEvent e )
	{
	}
	public void windowIconified( WindowEvent e )
	{
	}
	public void windowOpened( WindowEvent e )
	{
		Dimension d = getContentPane().getSize();
		sp.setSize( d.width, d.height );
		sp.setLocation( 0, 0 );
//		System.out.println( d.width );
//		System.out.println( d.height );

	}

	// DocumentListener methods

	public void changedUpdate( DocumentEvent e )	
	{
		if( currentSourceFileIndex >= 0 && !switching) 
			sourceFiles.get( currentSourceFileIndex ).setModified( true );
		
		lnt.updateLineNumbers();
		miFileSave.setEnabled( true );
		miFileSaveAll.setEnabled( true );
	}
	public void insertUpdate( DocumentEvent e )	
	{
		if( currentSourceFileIndex >= 0 && !switching) 
			sourceFiles.get( currentSourceFileIndex ).setModified( true );
		
		lnt.updateLineNumbers();
		miFileSave.setEnabled( true );
		miFileSaveAll.setEnabled( true );
	}
	public void removeUpdate( DocumentEvent e )	
	{
		if( currentSourceFileIndex >= 0 && !switching) 
			sourceFiles.get( currentSourceFileIndex ).setModified( true );
		
		lnt.updateLineNumbers();
		miFileSave.setEnabled( true );
		miFileSaveAll.setEnabled( true );
	}

	
	/**ENTRY TO PROGRAM
	 * @param args
	 */
	public static void main( String[] args ) // driver method		
	{
		new Main().setVisible( true );
	}
}
