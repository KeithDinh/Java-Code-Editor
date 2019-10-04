import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
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
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TabbedPaneUI;


/* STRUCTURE INTRODUCTION: 
 * 	Frame {Menu bar, Tab bar}
 * 		Menu bar {Project, File, Edit}
 * 		Tab bar  {Tab[]}
 *  		Tab  {textArea, file, file_content, file_name, file_path}
 */

//CTRL + SHIFT + Numpad /: collapse all

public class MainFrame extends JFrame implements ActionListener  
{	
	/* ********************************** CLASS MEMBERS ********************************** */
	private JMenuBar menuBar = new JMenuBar();
	//{
		private JMenu file_menu = new JMenu("File");
			//{
				private JMenuItem create_project;
				private JMenuItem open_project;
				private JMenuItem save_project;
				private JMenuItem close_project;
			//}
		private JMenu project_menu = new JMenu("Project");
			//{
				private JMenuItem create_file;
				private JMenuItem open_file;
				private JMenuItem save_file;
				private JMenuItem close_file;
			//}
		private JMenu edit_menu = new JMenu("Edit");
			//{
				private JMenuItem findReplaceMenuItem;
				protected FindReplaceDialog searchTool = new FindReplaceDialog(this);
			//}
		private JMenu build_menu = new JMenu("Build");
			//{
				private JMenuItem compile;
				private JMenuItem execute;
			//}
		private JMenu about_menu = new JMenu("About");
	//}
				
				
	private JTabbedPane tab_bar = new JTabbedPane(JTabbedPane.TOP);
	//{
		private ArrayList<Tab> tab = new ArrayList<Tab>();
		private JTextArea terminal_tab;
	//}
	
	Process process;	
	private String project_dir; //store current project path
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
    
    //take file path(string) as argument, return content of file (string) 
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
		super("Java Editor by C--"); 				//Set Program's Name
		setIconImage(new ImageIcon("icons/javaTextEditorIcon2.PNG").getImage());
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
		
		
		this.setSize(700,600);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() 
		{
			  @Override
			  public void windowClosing(WindowEvent we)
			  { 
				if(tab.size() > 0)
				{	
				    String option_button[] = {"Yes","No", "Cancel"};
				    int option = JOptionPane.showOptionDialog(null, 
				        "Save before closing", 	//content description
				        "Warning", 			 //top title of the pop-up window
				        JOptionPane.DEFAULT_OPTION, 	//option type(Yes,no,true,false,...)
				        JOptionPane.QUESTION_MESSAGE, 	//window type(warning, question,...)
				        null, 
				        option_button,		//buttons on the dialog
				        option_button[0]); //default selected button
				    if(option==0)
				    {
				      save_project_function();    
				    }
				    else if (option == 2)
				    	return;  
			  }
				System.exit(0);  
			}
		});
			
		this.setVisible(true); 
	
        getContentPane().add(tab_bar);
	}

	private void createMenuItem()
	{
		//************ Add menuButton to project menu ************//
		
		create_project = new JMenuItem("New Project");
		create_project.addActionListener(this);
		project_menu.add(create_project);
		
		open_project = new JMenuItem("Open Project");
		open_project.addActionListener(this);
		project_menu.add(open_project);
		project_menu.addSeparator();
		
		save_project = new JMenuItem("Save Project (Save All)");
		save_project.setEnabled(false);	
		save_project.addActionListener(this);
		project_menu.add(save_project);
		project_menu.addSeparator();
		
		close_project = new JMenuItem("Close Project");
		close_project.addActionListener(this);
		close_project.setEnabled(false);
		project_menu.add(close_project);
		
		//************ Add menuButton to file menu ************//
		
		create_file = new JMenuItem("New File");
		create_file.addActionListener(this);
		file_menu.add(create_file);
		file_menu.addSeparator();
		
		open_file = new JMenuItem("Open File");
		open_file.addActionListener(this);
		file_menu.add(open_file);
		file_menu.addSeparator();
		
		save_file = new JMenuItem("Save File");
		save_file.addActionListener(this);
		file_menu.add(save_file);
		file_menu.addSeparator();
		save_file.setEnabled(false); //initialize save_file menuItem in disable mode when no file to be saved
		
		
		close_file = new JMenuItem("Close File");
		close_file.addActionListener(this);
		close_file.setEnabled(false);
		file_menu.add(close_file);
		
		//************ Add menuButton to edit menu ************//
		
		//Buid edit_menu with cutCopyPasteAction()
		cutCopyPasteAction();
		project_menu.addSeparator();
		findReplaceMenuItem = new JMenuItem("Find/Replace");
		findReplaceMenuItem.setEnabled(false);//enable when exists a opened file. 
		edit_menu.add(findReplaceMenuItem);
		findReplaceMenuItem.addActionListener(this);
		
		//************ Add menuButton to build menu ************//
		
		compile = new JMenuItem("Compile");
		compile.addActionListener(this);
		compile.setEnabled(false);
		build_menu.add(compile);
		build_menu.addSeparator();
		
		execute = new JMenuItem("Execute");
		execute.addActionListener(this);
		execute.setEnabled(false);
		build_menu.add(execute);
		build_menu.addSeparator();
		
	}
	
	private void enableShortCutKeys(boolean enableMode) 
	{
		if(enableMode==true) 
		{	//short cut keys for menus
			project_menu.setMnemonic('P');
			file_menu.setMnemonic('F');
			edit_menu.setMnemonic('E');
			//short-cut keys for Project Menu Item
			create_project.setAccelerator(KeyStroke.getKeyStroke('N',Event.CTRL_MASK|Event.SHIFT_MASK));
			save_project.setAccelerator(KeyStroke.getKeyStroke('S',Event.CTRL_MASK|Event.SHIFT_MASK));	//add short cut ctrl+shift+S for saving a project
			close_project.setAccelerator(KeyStroke.getKeyStroke('W',Event.CTRL_MASK|Event.SHIFT_MASK));//ctrl+shift+w for closing the current project
			//We have to cast KeyEvent.VK_ to a char. If not, it will show a warning as below
			open_project.setAccelerator(KeyStroke.getKeyStroke('O',Event.CTRL_MASK|Event.SHIFT_MASK));
			
			//short cut keys for menuItems in File Menu
			save_file.setAccelerator(KeyStroke.getKeyStroke('S',Event.CTRL_MASK));	//ctrl+S for saving a file
			create_file.setAccelerator(KeyStroke.getKeyStroke('N',Event.CTRL_MASK));//ctrl+N for creating a new file
			close_file.setAccelerator(KeyStroke.getKeyStroke('W',Event.CTRL_MASK));//ctril+W for closing current file
			open_file.setAccelerator(KeyStroke.getKeyStroke('O',Event.CTRL_MASK));//ctrl+O for opening a file
			
			//short cut keys for find/replace menuItems in Edit Menu
			findReplaceMenuItem.setAccelerator(KeyStroke.getKeyStroke('F',Event.CTRL_MASK));
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
			close_file_function();
		}
		//******************EDIT******************//
		else if(e.getSource()==findReplaceMenuItem) {
			searchTool.searchThisArea(getCurrentTab().getRSTA());
		}
		//******************BUILD******************//
		else if(e.getSource() == compile)
		{
			try {
				compile_function();
			} catch (IOException e1) {
				System.out.println("Compile Error");
			}
		}
		else if(e.getSource() == execute)
		{
			try {
				execute_function();
			} catch (IOException e1) {
				System.out.println("Execute Error");
			}
		}
	}
	
	
	//***************PROJECT FUNCTIONS*******************//
	

	private void create_project_function()
	{
		System.out.println("***CREATE PROJECT***");
		
		if(project_dir != null)
		{
			Object[] options = { "OK", "CANCEL" };
			int result = JOptionPane.showOptionDialog(null, "Close current project?", "Warning",
			        JOptionPane.DEFAULT_OPTION, 
			        JOptionPane.WARNING_MESSAGE,
			        null, options, options[0]);
			if(result==JOptionPane.YES_OPTION)
	        {
				close_project_function();		//ask user if they want to save and close
	        }
			else
				return;
		}
		
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
				
				//create folder
				new File(dir_path+ "\\" + folderName).mkdir();
				new File(dir_path+ "\\" + folderName + "\\lib").mkdir();
				new File(dir_path+ "\\" + folderName + "\\src").mkdir();
				
				if(new File(dir_path + "\\" + folderName).exists()) //created but not exist? -> invalid
				{
					dir_path += "\\" + folderName;
					project_dir =dir_path;
					create_Main_function();
				}
				else 
				{
				JOptionPane.showMessageDialog(null, "Cant't create project, possible illegal character(s)", null, JOptionPane.ERROR_MESSAGE);
				return;
				}
			}
		}
		compile.setEnabled(true);
        save_project.setEnabled(true);
        close_project.setEnabled(true);
        save_file.setEnabled(true);
        close_file.setEnabled(true);
        findReplaceMenuItem.setEnabled(true);
		System.out.println("***END CREATE PROJECT***");

		return;
	}
	
	private void open_project_function() throws IOException 
	{
		System.out.println("***OPENING PROJECT***");
		if(project_dir != null)
		{
			Object[] options = { "OK", "CANCEL" };
			int result = JOptionPane.showOptionDialog(null, "Close current project?", "Warning",
			        JOptionPane.DEFAULT_OPTION, 
			        JOptionPane.WARNING_MESSAGE,
			        null, options, options[0]);
			if(result==JOptionPane.YES_OPTION)
	        {
				close_project_function();
	        }
			else
				return;
		}
		JFileChooser chooser = new JFileChooser(); 						//this class is to open file/directory
		
		if(project_dir != null)
			chooser.setCurrentDirectory(new File(project_dir)); 		//set current dir as window popup	
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
            
            
            if(files == null)
            	return ;
            
            //Each file will be presented on a tab
            
            //Iterate through files, get contents, open and write on the tab
            for(int i=0; i<files.size();i++)
            {
            	
            	//create tab with string (readFileFromPath return the contents in string)
            	tab.add(new Tab
	            			(
		            			readFileFromPath(files.get(i).getPath()), //content of file
		            			files.get(i).getName(),					  //name of file
		            			files.get(i).getPath(),					  //path of file
		            			files.get(i)							  //file itself
	            			)
            			);
            	
            	tab_bar.addTab(
            			tab.get(i).tabName, 							  //Name of Tab
            			tab.get(i).container);				  //Add scrollable version of Tab
            }
            
            compile.setEnabled(true);
            save_project.setEnabled(true);
            close_project.setEnabled(true);
            save_file.setEnabled(true);
            close_file.setEnabled(true);
            findReplaceMenuItem.setEnabled(true);
            
            project_dir = path;
            if(project_dir.endsWith("\\src"))
            {
            	project_dir=project_dir.substring(0, project_dir.length()-3);
            }
        }
	    System.out.println("***END OPENING PROJECT***");
        return;
	}
  
	private void save_project_function()
	{
		for(int i=0; i<tab.size();i++)
        {
			String content = tab.get(i).get_updated_content();
		    try 
		    {
		    	BufferedWriter writer = new BufferedWriter(new FileWriter(tab.get(i).path));
				writer.write(content);
				writer.close();
				
			} 
		    catch (IOException e) 
		    {
				JOptionPane.showConfirmDialog(null, e.getMessage(),"Error Writing File",
						JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
			}
		    
		    
        }
		System.out.println("***END SAVE PROJECT***");

	}
	
	private void close_project_function()
	{
		Object[] options = { "OK", "CANCEL" };
		int result = JOptionPane.showOptionDialog(null, "Save before closing?", "Warning",
		        JOptionPane.DEFAULT_OPTION, 
		        JOptionPane.WARNING_MESSAGE,
		        null, options, options[0]);
		if(result==JOptionPane.YES_OPTION)
        {
        	save_project_function();
        }
		tab_bar.removeAll();
		save_project.setEnabled(false);
		close_project.setEnabled(false);
		tab.clear();
		project_dir = null;
		System.out.println("***END CLOSE PROJECT***");

	}
	
	//****************FILE FUNCTIONS******************//
	

	private void create_Main_function()
	{
		System.out.println("***NEW FILE***");
		String fileName = "Main.java";
		
		FileWriter file = null;		
		System.out.println(project_dir);
		String filePath = project_dir + "\\src\\" + fileName;
		
		//-5 = remove ".java" to get the name only
		String contents = "public class Main\n{\n\tpublic static void main(String[] args)\n\t{\n\t}\n}"; 
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
		
		//files.add(new File(filePath));
		tab.add(new Tab(
				readFileFromPath(filePath), 
				fileName, 
				filePath, 
				new File(filePath)));
    	
    	tab_bar.addTab(
    			tab.get(tab.size()-1).tabName, 
    			tab.get(tab.size()-1).container);
    	
    	save_file.setEnabled(true);
    	close_file.setEnabled(true);
    	save_project.setEnabled(true);
    	close_project.setEnabled(true);
		System.out.println("***END NEW FILE***");

	}
	
	private void create_file_function()
	{
		System.out.println("***NEW FILE***");
		
		if(project_dir == null)
		{
			Object[] options = { "OK", "CANCEL" };
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
		
		String name_to_check = fileName.substring( 0, fileName.length() - 5);   
		if( !Character.isLetter( name_to_check.charAt( 0 ) ) || !name_to_check.matches("[a-zA-Z0-9]*" ) )
		{
			JOptionPane.showMessageDialog(null, "Illegal character(s) in file name\nFile name can only contain letters and numbers and must start with a letter", null, JOptionPane.ERROR_MESSAGE);
			return;
		}

		FileWriter file = null;
		String filePath = project_dir + "\\src\\" + fileName;
		
		//-5 = remove ".java" to get the name only
		String contents = "public class " + fileName.substring(0, fileName.length() - 5) + "\n{\n\n}"; 
		
		System.out.println(filePath);

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
		
		//files.add(new File(filePath));
		tab.add(new Tab(
				readFileFromPath(filePath), 
				fileName, 
				filePath, 
				new File(filePath)));
    	
    	tab_bar.addTab(
    			tab.get(tab.size()-1).tabName, 
    			tab.get(tab.size()-1).container);
    	
    	save_file.setEnabled(true);
    	close_file.setEnabled(true);
		System.out.println("***END NEW FILE***");

	}
	
	private void open_file_function() throws IOException 
	{
		System.out.println("***OPENING FILE***");
		JFileChooser chooser = new JFileChooser(); 						//this class is to open file/directory
		if(project_dir != null)
			chooser.setCurrentDirectory(new File(project_dir)); 		//set current dir as window popup	
		else
			chooser.setCurrentDirectory(new File("."));
		chooser.setFileFilter(new FileNameExtensionFilter("*.java", "java"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); 		//will save current dir/selected dir
        int r = chooser.showOpenDialog(this); 
        
        if (r == JFileChooser.APPROVE_OPTION) 
        {
            String path = chooser.getSelectedFile().getPath();
                       
           //create file with filter, then store all valid files in it
            File single_file = new File(path);
            
            if(single_file == null)
            	return;
        	//////////////////////////
            
            tab.add(new Tab(
            		readFileFromPath(single_file.getPath()), 
            		single_file.getName(),
            		single_file.getPath(),
            		single_file));
        	
        	tab_bar.addTab(
        			tab.get(tab.size()-1).tabName, 
        			tab.get(tab.size()-1).container);
        	
        	
			save_file.setEnabled(true);
			close_file.setEnabled(true);
			findReplaceMenuItem.setEnabled(true);
			save_project.setEnabled(true);
			close_project.setEnabled(true);
			
            
        }  
        //if file is successfully created, change save_file menuItem to enable
        

        System.out.println("***END OPENING FILE***");
	}
		
	private void save_file_function()
	{
		int index_selected_tab = tab_bar.getSelectedIndex();
		System.out.println(index_selected_tab);
		Tab current_selected_tab = tab.get(index_selected_tab);
		
		/////////get current contents on the textArea///////////////
		String content = current_selected_tab.get_updated_content();

	    try 
	    {
	    	BufferedWriter writer = new BufferedWriter(new FileWriter(current_selected_tab.path));
			writer.write(content);
			writer.close();
		} 
	    catch (IOException e) 
	    {
			JOptionPane.showConfirmDialog(null, e.getMessage(),"Error Writing File",JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
		}
	}
		
	private void close_file_function()
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
		int index_selected_tab = tab_bar.getSelectedIndex();
		tab.remove(index_selected_tab);
		tab_bar.remove(index_selected_tab);
		if(tab.size()==0)
		{
			save_project.setEnabled(false);
			close_project.setEnabled(false);
			save_file.setEnabled(false);
			close_file.setEnabled(false);
		}
		System.out.println("***END CLOSE FILE***");

	}

	//****************EDIT FUNCTIONS*******************//
	public void cutCopyPasteAction() {
		Action cutAction = new DefaultEditorKit.CutAction();
		Action copyAction = new DefaultEditorKit.CopyAction();
		Action pasteAction = new DefaultEditorKit.PasteAction();
		
		cutAction.putValue(Action.NAME,"Cut");
		cutAction.putValue(Action.SMALL_ICON, new ImageIcon("icons/cut.gif"));
		cutAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('X',Event.CTRL_MASK));
		edit_menu.add(cutAction);
		edit_menu.addSeparator();
		
		copyAction.putValue(Action.NAME,"Copy");
		copyAction.putValue(Action.SMALL_ICON, new ImageIcon("icons/copy.gif"));
		copyAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('C',Event.CTRL_MASK));
		edit_menu.add(copyAction);
		edit_menu.addSeparator();
		
		pasteAction.putValue(Action.NAME,"Paste");
		pasteAction.putValue(Action.SMALL_ICON, new ImageIcon("icons/paste.gif"));
		pasteAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('V',Event.CTRL_MASK));
		edit_menu.add(pasteAction);
		edit_menu.addSeparator();
	}
	
	//****************BUILD FUNCTIONS*******************//
	
	public void compile_function() throws IOException
	{
		String file_path;

		if(new File(project_dir+"src").exists())
			file_path= project_dir+"\\src\\";
		else {
			file_path= project_dir+"\\";
		}
		
		String compile_output="";
		 //combine all arguments with space, that's it
		ProcessBuilder processBuilder = new ProcessBuilder("javac","-cp", project_dir+"\\lib\\", file_path+"\\Main.java"); 
		process = processBuilder.start();
	    if( process.getErrorStream().read() != -1 )
        {
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            compile_output += "************* Compilation Errors *************\n";
            String line;
            while((line = error.readLine()) != null )
            {
                compile_output += line + "\n";
            }
            error.close();
        }
	    else {
	    	compile_output += "************* Compilation Success *************\n";
	    }
	    
	    terminal_tab = new JTextArea();
	    terminal_tab.setText(compile_output);
	    tab_bar.add("Terminal Output", terminal_tab);
	    
	    if( process.exitValue() == 0 ) //if compile error -> exitValue() != 0
	    {
	    	execute.setEnabled(true);
	    }
	    
	    terminal_tab.setEditable(false);
		
	}
	
	public void execute_function() throws IOException {
		String file_path;
		
		if(new File(project_dir+"\\src").exists())
			file_path= project_dir+"\\src\\";
		else {
			file_path= project_dir+"\\";
		}
		System.out.println(file_path);
		process = new ProcessBuilder("java","-cp",file_path, "Main").start();
		if( process.getErrorStream().read() != -1 ){
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            terminal_tab.append( "************* Execution Errors *************\n");
            String line;
            while((line = error.readLine()) != null )
            {
            	terminal_tab.append(line + "\n");
            }
            error.close();
        }
        else{
        	BufferedReader error = new BufferedReader(new InputStreamReader(process.getInputStream()));
        	terminal_tab.append( "************* Execution Success *************\n");
        	String line;
            while((line = error.readLine()) != null )
            {
            	terminal_tab.append(line + "\n");
            }
            error.close();
        }
		
		
	}
}









