import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TabbedPaneUI;


/* STRUCTURE INTRODUCTION: 
 * 	Frame {Menu bar, Tab bar}
 * 		Menu bar {Project, File, Edit}
 * 		Tab bar  {Tab[]}
 *  		Tab  {textArea, file, file_content, file_name, file_path}
 */

//CTRL + SHIFT + Numpad /: collapse all

@SuppressWarnings("unchecked")
class MainFrame extends JFrame implements ActionListener
{	
	/* ********************************** CLASS MEMBERS ********************************** */
	private JMenuBar menuBar = new JMenuBar();
	//{
		private JMenu file_menu = new JMenu("File");
			//{
				private JMenuItem create_project;
				private JMenuItem open_project;
				protected static JMenuItem save_project;
				protected static JMenuItem save_all;
				private JMenuItem close_project;
			//}
		private JMenu project_menu = new JMenu("Project");
			//{
				private JMenuItem create_file;
				private JMenuItem open_file;
				protected static JMenuItem save_file;
				private JMenuItem close_file;
				private JMenuItem remove_file;
			//}
		private JMenu edit_menu = new JMenu("Edit");
			//{
				private Action cutAction;
				private Action copyAction;
				private Action pasteAction;
				private JMenuItem findReplaceMenuItem;
				protected FindReplaceDialog searchTool = new FindReplaceDialog(this);
			//}
		private JMenu build_menu = new JMenu("Build");
			//{
				private JMenuItem compile;
				private JMenuItem compileAll;
				private JMenuItem execute;
			//}
		private JMenu about_menu = new JMenu("About");
	//}
		private JTextArea console_text_area = new JTextArea();
		private JSplitPane splitPane= new JSplitPane();
		private JPanel bottom_terminal_panel = new JPanel();
	private JTabbedPane tab_bar = new JTabbedPane(JTabbedPane.TOP);
	//{
		private ArrayList<Tab> tab = new ArrayList<Tab>();
		private JTextArea terminal_tab;
	//variables to write output to console
	int lastCaretPositionFromOuput=0;
	int endCaretPos=0;
	private String completion="";
	private File istrFile;
	BufferedWriter bw;
	//}
		
		
	private HashMap<String, Integer> duplicates = new HashMap<String, Integer>();
	private String project_dir; 			//store current project path
	private String src_dir;
	private String bin_dir;
	private String lib_dir;
	private String last_project_path;		//will save the recent closed project path
	Process process;						//for compile and execute

	/* ********************************************************** */
	
	/* ********************************** CLASS FUNCTIONS ********************************** */
	
	
	/**Description: This is a private function that create a GUI for 
	 * the mainframe. */

	//*************************MINOR FUNCTIONS*************************//

	//used for open_project_function() to read only ".java" files
	private FilenameFilter javaFilter = new FilenameFilter()		
    {
        @Override
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".java");
        }
    };
    
	private FilenameFilter classFilter = new FilenameFilter()		
    {
        @Override
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".class");
        }
    };
    
    
	/**This function take file path(string) as argument, return content of file (string) 
	 * @param filePath
	 * @return
	 */
	private String readFileFromPath(String filePath) 				
    {
		String content = "";
	    try
	    {
	        content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
	    }
	    catch (IOException e)
	    {
	        e.printStackTrace();
	    }
	    return content;
    }
	
	//return current/open selected tab
	private Tab getCurrentTab() {
		int index_selected_tab = tab_bar.getSelectedIndex();
		Tab current_selected_tab = tab.get(index_selected_tab);
		return current_selected_tab;
	}
    
	//*************************MAJOR FUNCTIONS*************************//

	public MainFrame()
	{
		super("Java Editor"); //Set Program's Name
		setIconImage( new ImageIcon("icons/javaTextEditorIcon2.PNG").getImage() );
		
		createMenuItem();
		enableShortCutKeys(true);			//add shortcut keys 
		
		//MenuBar(TaskBar) > menu(Project, File, Edit) > each menuButton(new,create,..)
		menuBar.add(project_menu);			
		menuBar.add(file_menu);
		menuBar.add(edit_menu);
		menuBar.add(build_menu);
		menuBar.add(about_menu);
		
		setJMenuBar(menuBar); 			//Add the menu bar to the frame
		
		pack(); 						//no idea what this is but without it, menu bar won't display on the frame
		
		this.setSize(800,600);
		
		//use this splitPane to split the contentPane into the main area (under tab_bar pane),
		//and the terminal area under(bottom_terminal_panel)
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(getHeight()*2/3);//devide with proprotion 2/3 1/3
		splitPane.setTopComponent(tab_bar);
		splitPane.setBottomComponent(bottom_terminal_panel);
		//this function create the terminal under the main area
		openTerminal();
		
	    tab_bar.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent e) { 
	        	if( tab_bar.getSelectedComponent() != null ) 
	        	{
	        		Tab currentTab = tab.get( tab_bar.getSelectedIndex() ); 
		            save_file.setEnabled( currentTab.modified );
		            compile.setText( "Compile " + currentTab.fileName );
	        	}  
	        	else 
	        	{
	        		compile.setText( "Compile" );
	        	}
	        }
	    });
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent we)
			{ 
				boolean project_modified = false;
				for(int i=0; i<tab.size();i++)
				{
					if(tab.get(i).modified)
					{
						project_modified = true;
						break;
					}
				}
				if(tab.size() > 0 && project_modified )
				{	
				  String[] option_button = {"Yes","No", "Cancel"};
				  int option = JOptionPane.showOptionDialog(null, 
						  "Save before closing", 	//content description
						  "Warning", 			 //top title of the pop-up window
						  JOptionPane.DEFAULT_OPTION, 	//option type(Yes,no,true,false,...)
						  JOptionPane.QUESTION_MESSAGE, 	//window type(warning, question,...)
						  null, 
						  option_button,		//buttons on the dialog
						  option_button[0]); //default selected button
				  	if(option==0)
				  		save_project_function();    
				  	else if (option == 2)
				  		return;  
				}
				System.exit(0);  
			}
		});
			
		this.setVisible(true); 
        getContentPane().add(splitPane);
        
        //Center the frame on the Screen
        Dimension screenSize= Toolkit.getDefaultToolkit().getScreenSize();//screenSize 
		setBounds((int)(0.5*(screenSize.width-getWidth())),
				(int)(0.5*(screenSize.height-getHeight())),getWidth(),getHeight());
	}

	private void createMenuItem()
	{
		//************ Add menuButton to project menu ************//
		
		create_project = new JMenuItem("New Project");
		create_project.addActionListener(this);
		create_project.setIcon( new ImageIcon("icons/new-project.PNG") );
		project_menu.add(create_project);

		open_project = new JMenuItem("Open Project");
		open_project.addActionListener(this);
		open_project.setIcon( new ImageIcon("icons/open-project.PNG") );
		project_menu.add(open_project);
		
		project_menu.addSeparator();
		
		save_project = new JMenuItem("Save Project");
		save_project.setEnabled(false);	
		save_project.addActionListener(this);
		save_project.setIcon( new ImageIcon("icons/save-project.PNG") );
		project_menu.add(save_project);
		
		save_all = new JMenuItem("Save All");
		save_all.setEnabled(false);	
		save_all.addActionListener(this);
		save_all.setIcon( new ImageIcon("icons/save-all.PNG") );
		project_menu.add(save_all);
		
		project_menu.addSeparator(); 
		
		close_project = new JMenuItem("Close Project");
		close_project.addActionListener(this);
		close_project.setEnabled(false);
		project_menu.add(close_project);

		
		//************ Add menuButton to file menu ************//
		
		open_file = new JMenuItem("Open File");
		open_file.addActionListener(this);
		file_menu.add(open_file);
		
		close_file = new JMenuItem("Close File");
		close_file.addActionListener(this);
		close_file.setEnabled(false);
		file_menu.add(close_file);
		
		file_menu.addSeparator(); 
		
		save_file = new JMenuItem("Save File");
		save_file.addActionListener(this);
		save_file.setIcon( new ImageIcon("icons/save.PNG") );
		file_menu.add(save_file);
		
		file_menu.addSeparator();
		
		create_file = new JMenuItem("New File");
		create_file.addActionListener(this);
		create_file.setIcon( new ImageIcon("icons/new-file.PNG") );
		file_menu.add(create_file);
		
		remove_file = new JMenuItem("Remove File");
		remove_file.addActionListener(this);
		remove_file.setIcon( new ImageIcon("icons/remove-file.PNG") );
		remove_file.setEnabled(false);
		file_menu.add(remove_file);
		
		save_file.setEnabled(false); //initialize save_file menuItem in disable mode when no file to be saved
		
		//************ Add menuButton to edit menu ************//
		
		//Buid edit_menu with cut_copy_paste_action()
		cut_copy_paste_action();
		findReplaceMenuItem = new JMenuItem("Find/Replace");
		findReplaceMenuItem.setIcon( new ImageIcon( "icons/find.PNG" ) );
		findReplaceMenuItem.setEnabled( false );
		edit_menu.add(findReplaceMenuItem);
		findReplaceMenuItem.addActionListener(this);
		
		//************ Add menuButton to build menu ************//
		
		compile = new JMenuItem("Compile");
		compile.addActionListener(this);
		compile.setIcon( new ImageIcon("icons/build.PNG") );
		compile.setEnabled(false);
		build_menu.add(compile);
		
		compileAll = new JMenuItem("Compile All");
		compileAll.addActionListener(this);
		compileAll.setIcon( new ImageIcon("icons/compileAll.PNG") );
		compileAll.setEnabled(false);
		build_menu.add(compileAll);
		
		execute = new JMenuItem("Execute");
		execute.addActionListener(this);
		execute.setIcon( new ImageIcon("icons/run.PNG") );
		execute.setEnabled(false);
		build_menu.add(execute);
		
	}
	
	private void enableShortCutKeys(boolean enableMode) 
	{
		if(enableMode)
		{	//short cut keys for menus
			project_menu.setMnemonic( KeyEvent.VK_P );
			file_menu.setMnemonic( KeyEvent.VK_F );
			edit_menu.setMnemonic( KeyEvent.VK_E );
			build_menu.setMnemonic( KeyEvent.VK_B );
			about_menu.setMnemonic( KeyEvent.VK_A );
			
			// short-cut keys for Project Menu Item
			create_project.setAccelerator(KeyStroke.getKeyStroke('N',Event.CTRL_MASK|Event.SHIFT_MASK));
			save_project.setAccelerator(KeyStroke.getKeyStroke('S',Event.CTRL_MASK|Event.SHIFT_MASK));	//add short cut ctrl+shift+S for saving a project
			close_project.setAccelerator(KeyStroke.getKeyStroke('W',Event.CTRL_MASK|Event.SHIFT_MASK));//ctrl+shift+w for closing the current project
			save_all.setAccelerator(KeyStroke.getKeyStroke('A',Event.CTRL_MASK|Event.SHIFT_MASK));
			//We have to cast KeyEvent.VK_ to a char. If not, it will show a warning as below
			open_project.setAccelerator(KeyStroke.getKeyStroke('O',Event.CTRL_MASK|Event.SHIFT_MASK));
			
			//short cut keys for menuItems in File Menu
			save_file.setAccelerator(KeyStroke.getKeyStroke('S',Event.CTRL_MASK));	//ctrl+S for saving a file
			create_file.setAccelerator(KeyStroke.getKeyStroke('N',Event.CTRL_MASK));//ctrl+N for creating a new file
			close_file.setAccelerator(KeyStroke.getKeyStroke('W',Event.CTRL_MASK));//ctril+W for closing current file
			open_file.setAccelerator(KeyStroke.getKeyStroke('O',Event.CTRL_MASK));//ctrl+O for opening a file
			
			//short cut keys for find/replace menuItems in Edit Menu
			findReplaceMenuItem.setAccelerator(KeyStroke.getKeyStroke('F',Event.CTRL_MASK));
			
			//short cut keys for compile and execute menuItems in Build Menu
			compile.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK ) );
			compileAll.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK ) );
			execute.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_F5, 0 ) );
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		//******************PROJECT******************//
		if(e.getSource() == create_project)
		{
			create_project_function();
		}
		else if(e.getSource() == open_project)
		{
	            try {
					open_project_function();
				} catch (IOException e1) {
					System.out.println("Error of open project");
				}
		}
		else if(e.getSource() == save_project) //Save all files
		{
			save_project_function();
		}
		else if(e.getSource() == save_all) //Save all files
		{
			save_all_function();
		}
		else if(e.getSource() == close_project)
		{
			close_project_function();
		}
		//******************FILE******************//
		else if(e.getSource() == create_file)
		{
			create_file_function();
		}
		else if(e.getSource() == open_file)
		{
			try {
				open_file_function();
			} catch (IOException e1) {
				System.out.println("Error of open file");
			}
		}
		else if(e.getSource() == save_file)
		{
			save_file_function();
		}
		else if(e.getSource() == close_file)
		{
			close_file_function( getCurrentTab() );
		}
		else if(e.getSource() == remove_file)
		{
			remove_file_function();
		}
		//******************EDIT******************//
		else if(e.getSource()==findReplaceMenuItem) {
			searchTool.searchThisArea(getCurrentTab().getRSTA());
		}
		//******************BUILD******************//
		else if(e.getSource() == compile)
		{
			try {
				compile_function( null );
			} catch (IOException e1) {
				System.out.println("Compile Error");
			}
		}
		else if(e.getSource() == compileAll)
		{
			try {
				compile_all();
			} catch (IOException e1) {
				System.out.println("Compile Error");
			}
		}
		else if(e.getSource() == execute)
		{
			try {
				execute_function();
			} catch ( IOException e1 ) {
				System.out.println("Execute Error");
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private void add_close_tab_button( String title, String newTitle )
	{	
		int index = tab_bar.indexOfTab( title );
		
		JPanel Tab_with_close = new JPanel(new BorderLayout());
		ImageIcon disableCloseIcon= new ImageIcon("icons/close.PNG");
		ImageIcon enableCloseIcon= new ImageIcon("icons/close-.PNG");
		JLabel tab_title = new JLabel( (newTitle == null) ? title : newTitle ); //name of tab
		JButton close_button = new JButton(disableCloseIcon);	//x button
		//set a clear border for close button
		close_button.setBorder(BorderFactory.createEmptyBorder(0,4,0,0)); 
		close_button.setContentAreaFilled(false);
		close_button.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent e) {
				close_button.setIcon(enableCloseIcon);
			}
			public void mouseExited(MouseEvent e) {
				close_button.setIcon(disableCloseIcon);
			}
		});
		
		Tab_with_close.setOpaque( false );
		Tab_with_close.add(tab_title,BorderLayout.WEST);
		Tab_with_close.add(close_button,BorderLayout.EAST);
		
		tab_bar.setTabComponentAt(index, Tab_with_close);	//set old tab with new feature(x button)

		close_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int index_selected_tab = tab_bar.indexOfTab( (newTitle == null) ? title : newTitle );
				
				if(tab.get(index_selected_tab).modified)
				{
					Object[] options = { "Yes", "No", "Cancel" };
					int result = JOptionPane.showOptionDialog(null, "Save before closing?", "Warning",
					        JOptionPane.DEFAULT_OPTION, 
					        JOptionPane.WARNING_MESSAGE,
					        null, options, options[0]);
					if(result==0)
			        {
			        	save_file_function();
			        }
					else if(result == 2)
					{
						return;
					}
				}
				close_file_function( tab.get(index_selected_tab) );
			}
			
		});
	}
	//***************PROJECT FUNCTIONS*******************//
	

	private void create_project_function()
	{
		//System.out.println("***CREATE PROJECT***");
		//check if there is an active project.
		if(!close_current_active_project())
			return;
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle( "Location to create project" );
		chooser.setApproveButtonText("Select path");
		
		//open dialog on last path if it is valid
		if(project_dir != null)
			chooser.setCurrentDirectory(new File(project_dir)); 		//set current dir as window popup	
		else
			chooser.setCurrentDirectory(new File("."));
		
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        	       
		if( chooser.showOpenDialog( this ) == JFileChooser.APPROVE_OPTION ) 
		{
			String dir_path = chooser.getSelectedFile().getPath();
			
			// Name the Project Folder
			String folderName = JOptionPane.showInputDialog(null, "Choose a project name");
			if( folderName != null )
			{
				//check if folder already exists
				if(new File( dir_path + "\\" + folderName ).exists()) 
				{
					JOptionPane.showMessageDialog(null, "Project already exists", null, JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				//create folders
				String projectPath = dir_path+ "\\" + folderName;
				String libPath = dir_path+ "\\" + folderName + "\\lib";
				String srcPath = dir_path+ "\\" + folderName + "\\src";
				String binPath = dir_path+ "\\" + folderName + "\\bin";
				
				new File(projectPath).mkdir();
				new File(libPath).mkdir();
				new File(srcPath).mkdir();
				new File(binPath).mkdir();
				
				if(new File(dir_path + "\\" + folderName).exists()) //created but not exist? -> invalid
				{
					project_dir = projectPath;
					src_dir= srcPath;
					lib_dir = libPath;
					bin_dir = binPath;
					
					//if Main.java file is created successfully, enable active mode for project.
					if(create_Main_function())
						active_project_status(true);
				}
				else 
				{
				JOptionPane.showMessageDialog(null, "Cant't create project, possible illegal character(s)", null, JOptionPane.ERROR_MESSAGE);
				return;
				}
			}
		}
		
		//System.out.println("***END CREATE PROJECT***");

		return;
	}
	
	private void open_project_function() throws IOException 
	{
		//System.out.println("***OPENING PROJECT***");
		//check if there is already an active project
		if(!close_current_active_project())
			return;
		
		JFileChooser chooser = new JFileChooser(); 						//this class is to open file/directory
		
		if(project_dir != null)
			chooser.setCurrentDirectory(new File(project_dir)); 		//set current dir as window popup	
		else if(last_project_path != null)
			chooser.setCurrentDirectory(new File(last_project_path));
		else 
			chooser.setCurrentDirectory(new File(".")); 
		
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 	//will save current dir/selected dir
        int r = chooser.showOpenDialog(this); 
        
        if (r == JFileChooser.APPROVE_OPTION) 
        {
        	//get path of directory
            String path = chooser.getSelectedFile().getPath();
            
            //if (the selected path is NOT src folder) AND (src folder exist) -> add src to path
            //else -> there is no src folder -> no need to add src
            if(!path.contains("src") && new File(path+"\\src").isDirectory())
            	path += "\\src";
            
            //new File(path).listFiles(javaFilter) return array of JAVA files 
            //aslist will convert the array into ArrayList type
            ArrayList<File> files = new ArrayList<File>(Arrays.asList(new File(path).listFiles(javaFilter)));
            
            if(files.isEmpty()) 
            {
            	JOptionPane.showMessageDialog(null, "Cannot Open Project", "Error", JOptionPane.ERROR_MESSAGE);
            	return;	
            }
            
            //Each file will be presented on a tab 
            //Iterate through files, get contents, open and write on the tab
            for(int i=0; i<files.size();i++)
            {
            	//create tab with string (readFileFromPath return the contents in string)
            	open_file_on_new_tab(files.get(i).getName(),files.get(i).getPath(), true);
            }
            
            //if open project successfully
            active_project_status(true);
            //restore the path to current project folder
            project_dir = path;
            if(project_dir.endsWith("\\src"))
            {
            	src_dir = project_dir;
            	project_dir=project_dir.substring(0, project_dir.length()-4);
            }
            else
            	src_dir = project_dir;
            
            // set bin_dir, and lib_dir, if they do not exist, create them
			String libPath = project_dir + "\\lib";
			String binPath = project_dir + "\\bin";
            if( !new File(libPath).exists() ) new File(libPath).mkdir();
            if( !new File(binPath).exists() ) new File(binPath).mkdir();
            
            // check that the folders have been created
            if( new File(libPath).exists() && new File(binPath).exists() ) 
            {
            	lib_dir = libPath;
            	bin_dir = binPath;
            }
        }
	    //System.out.println("***END OPENING PROJECT***");
        return;
	}
  
	
	/**
	 * This function will save the current project. 
	 */
	private void save_project_function()
	{
		for(int i=0; i<tab.size();i++)
        {
			if( tab.get( i ).projectFile ) 
			{
				String content = tab.get(i).get_updated_content();
			    try 
			    {
			    	BufferedWriter writer = new BufferedWriter(new FileWriter(tab.get(i).path));
					writer.write(content);
					writer.close();
					tab.get(i).modified = false;
				} 
			    catch (IOException e) 
			    {
					JOptionPane.showConfirmDialog(null, e.getMessage(),"Error Writing File",
							JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
				}   
			} 
        }
		setSaveProject();
		//System.out.println("***END SAVE PROJECT***");

	}

	/**
	 *    
	 */
	void setSaveEnabled() 
	{
		save_all.setEnabled( false );
		save_project.setEnabled( false );
		save_file.setEnabled( false );
	}
	
	/**
	 * This function will save every file  
	 */
	private void save_all_function() 
	{
		for(int i=0; i<tab.size();i++)
        {
			String content = tab.get(i).get_updated_content();
		    try 
		    {
		    	BufferedWriter writer = new BufferedWriter(new FileWriter(tab.get(i).path));
				writer.write(content);
				writer.close();
				tab.get(i).modified = false;
			} 
		    catch (IOException e) 
		    {
				JOptionPane.showConfirmDialog(null, e.getMessage(),"Error Writing File",
						JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
			}   
        }
		setSaveEnabled();
		//System.out.println("***END SAVE ALL***");
	}
	
	
	/**This function will close the current active project before creating new project
	 * or opening another project
	 * @return boolean 
	 */
	private boolean close_current_active_project() {
		if( close_project.isEnabled() )
		{
			Object[] options = { "OK", "Cancel" };
			int result = JOptionPane.showOptionDialog(null, "Close current project?", "Warning",
			        JOptionPane.DEFAULT_OPTION, 
			        JOptionPane.WARNING_MESSAGE,
			        null, options, options[0]);
			if(result==JOptionPane.YES_OPTION)
	        {
				return(close_project_function());
	        }
			else
				return false;
		}
		else return true;// if there is no active project
	}
	
	
	/**
	 * This function will close the current project.
	 * This function will pop-up an dialog to ask user for saving project options.
	 */
	private boolean close_project_function()
	{
		boolean project_modified = false;
		for(int i=0; i<tab.size();i++)
		{
			if(tab.get(i).modified)
			{
				project_modified = true;
				break;
			}
		}
		if(project_modified)
		{
			Object[] options = { "Yes","No", "Cancel" };
			int result = JOptionPane.showOptionDialog(null, "Save before closing?", "Warning",
			        JOptionPane.DEFAULT_OPTION, 
			        JOptionPane.WARNING_MESSAGE,
			        null, options, options[0]);
			if(result==JOptionPane.YES_OPTION)
	        {
	        	save_project_function();
	        	//return closed;
	        }
			else if(result == 2)
			{
				//System.out.println("***STOP CLOSE PROJECT***");
				return  false;
			}
		}
		tab_bar.removeAll();
		duplicates.clear();
		setSaveEnabled();
		active_project_status(false);
		execute.setEnabled( false );
		tab.clear();
		last_project_path = project_dir;
		project_dir = null;
		//System.out.println("***END CLOSE PROJECT***");
		return true;

	}
	
	
	
	/** This function will enable all menu items save_file, close_file, findReplaceMenuItem,
	 * save_project,close_project, compile,execute when a project is active. And this function
	 * will disable these menu items otherwise
	 * @param isActive
	 */
	protected void active_project_status(boolean isActive) {
		remove_file.setEnabled(isActive);
		close_file.setEnabled(isActive);
		close_project.setEnabled(isActive);
		compile.setEnabled(isActive); 
		compileAll.setEnabled(isActive);
		findReplaceMenuItem.setEnabled(isActive);
	}
	
	
	//****************FILE FUNCTIONS******************//
	

	/**
	 * This function will create an default Main.java file when a project is created.
	 */
	private boolean create_Main_function() 
	{
		//System.out.println("***NEW FILE***");
		String fileName = "Main.java";
		System.out.println(project_dir);
		String filePath = project_dir + "\\src\\" + fileName;
		
		//-5 = remove ".java" to get the name only
		//create default content for Main.java file
		String contents = "public class Main\n{\n\tpublic static void main(String[] args)\n\t{\n\t}\n}"; 
		write_content_to_filepath(contents,filePath);
		
		//files.add(new File(filePath));
    	open_file_on_new_tab(fileName, filePath, true );
		//System.out.println("***END NEW FILE***");
		return true;
	}
	
	
	/**
	 * This function will create a new file and display it on new Tab
	 */
	private void create_file_function()
	{
		//System.out.println("***NEW FILE***");
		
		if(project_dir == null)
		{
			Object[] options = { "OK", "Cancel" };
			int result = JOptionPane.showOptionDialog(null, 
					"Create project for java file?", 
					"Warning",
			        JOptionPane.DEFAULT_OPTION, 
			        JOptionPane.WARNING_MESSAGE,
			        null, options, options[0]);
			if(result==JOptionPane.YES_OPTION)
			{
				create_project_function();
				if(project_dir == null) 		//after created project, cannot be null
						return;
			}
			else return;
		}
		
		String fileName = JOptionPane.showInputDialog(null, "Enter the name of the new .java file", "Add File", JOptionPane.PLAIN_MESSAGE);
		if( fileName == null ) // this means that user clicked cancel or nothing
			return;
		if( !fileName.endsWith( ".java" ) )
		{
			fileName = fileName + ".java";
		}
		for(int i=0; i< tab.size();i++)
		{

			if(tab.get(i).tabName.equals(fileName))
			{
				JOptionPane.showMessageDialog(null, "Name already used", null, JOptionPane.ERROR_MESSAGE);
				return;
			}
				
		}
		String name_to_check = fileName.substring( 0, fileName.length() - 5);   
		if( !Character.isLetter( name_to_check.charAt( 0 ) ) || !name_to_check.matches("[a-zA-Z0-9]*" ) )
		{
			JOptionPane.showMessageDialog(null, "Illegal character(s) in file name\nFile name can only contain letters and numbers and must start with a letter", null, JOptionPane.ERROR_MESSAGE);
			return;
		}

		String filePath = project_dir + "\\src\\" + fileName;
		
		//-5 = remove ".java" to get the name only
		String contents = "public class " + fileName.substring(0, fileName.length() - 5) + "\n{\n\n}"; 
		
		System.out.println(filePath);
		
		//write the contents to the filePath
		if(write_content_to_filepath(contents,filePath))
			//open fileName on new Tab
			open_file_on_new_tab(fileName,filePath, true );
		
		//System.out.println("***END NEW FILE***");
	}
	
	
	
	/**
	 * @throws IOException 
	 */
	private void open_file_function() throws IOException 
	{
		//System.out.println("***OPENING FILE***");
		JFileChooser chooser = new JFileChooser(); 						//this class is to open file/directory
		if(project_dir != null)
			chooser.setCurrentDirectory(new File(project_dir)); 		//set current dir as window popup	
		else if(last_project_path != null)
			chooser.setCurrentDirectory(new File(last_project_path));
		else 
			chooser.setCurrentDirectory(new File("."));

		chooser.setFileFilter(new FileNameExtensionFilter("*.java", "java"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); 		//will save current dir/selected dir
        int r = chooser.showOpenDialog(this); 
        
        if (r == JFileChooser.APPROVE_OPTION) 
        {
            String path = chooser.getSelectedFile().getPath();
            String directory = chooser.getSelectedFile().getParent();
            
           //create file with filter, then store all valid files in it
            File single_file = new File(path);
            
            if(!single_file.exists()) 
            {
                JOptionPane.showMessageDialog(null, "Cannot Open File", "Error", JOptionPane.ERROR_MESSAGE);
            	return;
            }
        	//////////////////////////
            boolean projectFile;
            if( !close_project.isEnabled() ){
            	projectFile = false;
            }
            else if( project_dir != null ) 
            	projectFile = directory.contains(project_dir);
            else
            	projectFile = true;
            	
            open_file_on_new_tab(single_file.getName(),single_file.getPath(), projectFile );
        } 
        //System.out.println("***END OPENING FILE***");
	}
	

	/**
	 * 
	 */
	void setSaveProject() 
	{
		boolean saveProject = false;
		boolean saveAll = false;
		for( int i = 0; i < tab.size(); i++) 
		{
			if( tab.get( i ).modified ) 
			{
				if( tab.get( i ).projectFile )
					saveProject = true;
				else
					saveAll = true;
			}
		} 
		save_project.setEnabled( saveProject );
		save_all.setEnabled( (saveProject) ? saveProject : saveAll ); 
	}
	
	/**
	 * 
	 */
	private void save_file_function()
	{
		int index_selected_tab = tab_bar.getSelectedIndex();
		//System.out.println(index_selected_tab);
		Tab current_selected_tab = tab.get(index_selected_tab);
		
		/////////get current contents on the textArea///////////////
		String content = current_selected_tab.get_updated_content();

	    try 
	    {
	    	BufferedWriter writer = new BufferedWriter(new FileWriter(current_selected_tab.path));
			writer.write(content);
			writer.close();
			current_selected_tab.modified = false;
			save_file.setEnabled( false );
			setSaveProject();
		} 
	    catch (IOException e) 
	    {
			JOptionPane.showConfirmDialog(null, e.getMessage(),"Error Writing File",JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * 
	 */
	void setNewTabNumbers( Tab currentTab )
	{
		int count = duplicates.get( currentTab.fileName );
		String newName;
		String currentName;
		
		for(int i = currentTab.count; i < count; i++ ) 
		{
			currentName = currentTab.fileName + " (" + (i+2) + ")";
			int index = tab_bar.indexOfTab( currentName );
			if( i == 0) 
				newName = currentTab.fileName;
			else
				newName = currentTab.fileName + " (" + (i+1) + ")";
			tab.get(index).count--;
			tab.get(index).tabName = newName;
			add_close_tab_button( currentName, newName );
			tab_bar.setTitleAt(index, newName);
		} 
		count--;
		if( count == 0 )
			duplicates.remove( currentTab.fileName );
		else
			duplicates.put( currentTab.fileName, count-- );
	}
		
	/**
	 * 
	 */
	private void close_file_function( Tab currentTab )
	{
		if(currentTab.modified)
		{
			Object[] options = { "Yes", "No", "Cancel" };
			int result = JOptionPane.showOptionDialog(null, "Save before closing?", "Warning",
			        JOptionPane.DEFAULT_OPTION, 
			        JOptionPane.WARNING_MESSAGE,
			        null, options, options[0]);
			if(result==0)
	        {
	        	save_file_function();
	        }
			else if(result == 2)
			{
				return;
			}
		}
		int index = tab_bar.indexOfTab( currentTab.tabName );
		tab.remove(index);
		tab_bar.remove(index);
		boolean active = false;
		if(tab.size()==0) 
		{
			duplicates.clear();
			setSaveProject();
			active_project_status(false);
			execute.setEnabled( false );
		}
		else 
		{
			for( int i = 0; i < tab.size(); i++) 
			{
				if( tab.get( i ).projectFile )
					active = true;
			}
		}
		if( !active ) 
		{
			setSaveProject();
			active_project_status(false);
			execute.setEnabled( false );
		}
		if( duplicates.containsKey(currentTab.fileName) )
			setNewTabNumbers( currentTab );
		//System.out.println("***END CLOSE FILE***");
	}
	

	private void remove_file_function()
	{
		System.out.println(getCurrentTab().tabName);
		Object[] options = { "Yes", "No" };
		int result = JOptionPane.showOptionDialog(
				null, 
				(getCurrentTab().tabName.equals("Main.java")) ? "A project cannot compile without Main.java\nAre you sure you want to delete it?" : "Are you sure you want to delete " + getCurrentTab().tabName + "?", 
				"Warning",
		        JOptionPane.DEFAULT_OPTION, 
		        JOptionPane.WARNING_MESSAGE,
		        null, options, options[0]);
		if(result==0)
		{
			String file_path_to_remove = getCurrentTab().path;
			close_file_function( getCurrentTab() );
		
			new File(file_path_to_remove).delete();
			if(tab.size() == 0)
				remove_file.setEnabled(false);
	    }
	}
	
	
	
	/**This function will write a string  to a file with a filePath is given.
	 * @param String content
	 * @param String filePath
	 */
	protected boolean write_content_to_filepath(String content, String filePath) {
		FileWriter file = null;
		boolean success=false;
		try
		{
			file = new FileWriter( new File( filePath ) );
			file.write( content );
			success=true;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, "Unable to create file", null, JOptionPane.WARNING_MESSAGE);
			//return false;
		}
		finally
		{
			try { if( file != null ) file.close(); } catch( Exception ee ) { }
		}
		return success;
	}
	
	
	
	/**This function will open a given file on a new Tab in the tab panel of MainFrame
	 * @param fileName
	 * @param filePath
	 */
	protected void open_file_on_new_tab(String fileName, String filePath, boolean fileProject ) {
		
		// check if file already exists
		int count = 0;
		String tempName = null;
		for( int i = 0; i < tab.size(); i++) 
		{
			if( tab.get( i ).fileName.equals( fileName ) ) 
			{
				if( tab.get( i ).path.equals( filePath ) ) 
				{
					tempName = tab.get( i ).tabName;
					close_file_function( tab.get( i ) );
				}
				else 
				{
					count++;
				}
			}
		}
				
		
		tab.add( new Tab(
				readFileFromPath(filePath), 
				(tempName != null) ? tempName : (count == 0) ? fileName : fileName + " (" + (count + 1) + ")", 
				filePath, 
				new File(filePath),
				fileProject));
    	
		if( count > 0) 
		{
			tab.get( tab.size() - 1).fileName = fileName;
			tab.get( tab.size() - 1).count = count;
			duplicates.put(fileName, count);
		}
		
    	tab_bar.addTab(
    			tab.get(tab.size()-1).tabName,
    			tab.get(tab.size()-1).container);
    	
    	add_close_tab_button(tab.get(tab.size()-1).tabName, null);
    	
    	findReplaceMenuItem.setEnabled( true );
    	close_file.setEnabled(true);
	}

	
	
	
	//****************EDIT FUNCTIONS*******************//
	/**
	 * 
	 */
	public void cut_copy_paste_action() {
		cutAction = new DefaultEditorKit.CutAction(); 
		copyAction = new DefaultEditorKit.CopyAction();
		pasteAction = new DefaultEditorKit.PasteAction();
		
		cutAction.putValue(Action.NAME,"Cut");
		cutAction.putValue(Action.SMALL_ICON, new ImageIcon("icons/cut.gif"));
		cutAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('X',Event.CTRL_MASK));
		edit_menu.add(cutAction);
		
		copyAction.putValue(Action.NAME,"Copy");
		copyAction.putValue(Action.SMALL_ICON, new ImageIcon("icons/copy.gif"));
		copyAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('C',Event.CTRL_MASK));
		edit_menu.add(copyAction);
		
		pasteAction.putValue(Action.NAME,"Paste");
		pasteAction.putValue(Action.SMALL_ICON, new ImageIcon("icons/paste.gif"));
		pasteAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('V',Event.CTRL_MASK));
		edit_menu.add(pasteAction);
	}
	
	//****************BUILD FUNCTIONS*******************//
	/**
	 * @throws IOException
	/*public void compile_function() throws IOException
	 */

	private void moveFile( File classFile )
	{
		if( classFile.getName().equals("Main.class") )
			execute.setEnabled( true );
		File replace = new File(bin_dir + "\\" + classFile.getName() );
		if( replace.exists() )
			replace.delete();
		if(classFile.renameTo( new File(bin_dir + "\\" + classFile.getName() ) ) ) 
			classFile.delete();
	}
	private void moveClassFilestoBin() 
	{
		new ArrayList<File>(Arrays.asList(new File(src_dir).listFiles(classFilter))).forEach( classFile -> moveFile(classFile) );
	}
	
	public void compile_function( String s ) throws IOException // maybe change later so that we store the compiled classes in a folder called bin
	{
		save_project_function(); 
		StringBuilder compileOutput = new StringBuilder();
		String fileName = (s == null) ? getCurrentTab().fileName : s; 
		compileOutput.append("Compiling " + src_dir + "\\" + fileName );
		Compile.CompilationResult r = new Compile( src_dir, fileName ).compile();
		if( r.success ) 
		{
			compileOutput.append("... success \n");
		} 
		else 
		{	
			compileOutput.append("... failed \n");
			if( r.javacOutput != null && r.javacOutputComplete )
				compileOutput.append( r.javacOutput.toString() );
			else
				compileOutput.append( r.errorMessage );
		}
	    if( s == null ) 
	    {
	    	outputToTerminal( compileOutput.toString() );
	    	moveClassFilestoBin();
	    } 
	    else outputCompileAll( compileOutput.toString() );
	}
	
	public void compile_all() throws IOException 
	{
		clearTerminal();
		for( int i = 0; i < tab.size(); i++ ) // only compile project files, non project files will not compile (like visual studio) 
		{
			Tab tb = tab.get( i );
			if( tb.projectFile ) 
			{
				compile_function( tb.fileName );
			}
		}
	    moveClassFilestoBin();
	}

	/**
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void execute_function() throws IOException, InterruptedException {
		StringBuilder executionResult = new StringBuilder();
		executionResult.append("Executing " + bin_dir + "\\Main");
		Execute execute = new Execute( bin_dir, "Main.class");
		String result = execute.execute() ? "... done" : "... failed \n";
		if( execute.getErrorMessage() != null ) result += result + execute.getErrorMessage();
		executionResult.append( result );
		outputToTerminal( executionResult.toString() );
	}//end execute_function

	/**
	 * This function creates a terminal Pane in the bottom of the main Pane
	 */
	protected void openTerminal() {
	//with this layout, grid default is (1,1),when we add textArea to thisPanel, 
	//this panel will be filled out completely
	bottom_terminal_panel.setLayout(new GridLayout());
	
	console_text_area.setEditable(  false );
	console_text_area.setSize(600,50);
	console_text_area.setFont( new Font("Consolas", Font.PLAIN, 12 ) ); //set background color
	console_text_area.setText("");

	console();
	JTabbedPane terminal_tab_bar=new JTabbedPane();
	JScrollPane console_scroll_pane=new JScrollPane();
	console_scroll_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	console_scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	console_scroll_pane.setViewportView(console_text_area);
	ImageIcon console_icon= new ImageIcon("icons/console1.PNG");
	String tooptip="This is a terminal";//hovering text
	
	terminal_tab_bar.addTab("Console",console_icon,console_scroll_pane,tooptip);
	bottom_terminal_panel.add(terminal_tab_bar);
	bottom_terminal_panel.setOpaque(true);
	}
	
	
	/**This function will write the current project directory and the  output to the
	 * terminal.
	 * @param output
	 * @param project_dir
	 */
	private void outputToTerminal(String output ) {
		console_text_area.setText("");//clear console
		console_text_area.setText(output);
		console_text_area.setLineWrap(true);
	}
	
	private void outputCompileAll( String output  ) 
	{
		console_text_area.append(output);
	}
	private void clearTerminal() 
	{
		console_text_area.setText("");
	}


	//new function substitute for inputToTerminal function
	private	void console() {
		//System.console();
		int startCaretPos;
		//console_text_area.setBackground(new Color(0,0,35));
		// console_text_area.setForeground(Color.WHITE);
		//console_text_area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));


		console_text_area.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int key =e.getKeyCode();
				if(key==KeyEvent.VK_ENTER) {
					endCaretPos=console_text_area.getCaretPosition();

					try {
						//System.out.println("last "+lastCaretPositionFromOuput+5);
						completion=console_text_area.getText(lastCaretPositionFromOuput,
								endCaretPos-lastCaretPositionFromOuput);
							/*try {
								bw.write(completion);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}*/
						System.out.print(completion+lastCaretPositionFromOuput+" "+endCaretPos);
						//System.out.println("end "+endCaretPos+4);
					} catch (BadLocationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}); //end console()


		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {

				console_text_area.append(String.valueOf((char) b));
				console_text_area.setFocusable(true);

				//update the caret position when System.out tothe console
				//add a
				lastCaretPositionFromOuput=console_text_area.getCaretPosition();

			}
		}));


	} // end setOut()
} 