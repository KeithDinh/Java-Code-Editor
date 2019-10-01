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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import javax.swing.UnsupportedLookAndFeelException;


public class MainFrame extends JFrame implements ActionListener  {
	private JMenuBar menuBar = new JMenuBar();
	
	private JMenu file_menu = new JMenu("File");
	private JMenu project_menu = new JMenu("Project");
	private JMenu edit_menu = new JMenu("Edit");
	/////////////////////////////////
	private JMenuItem create_project;
	private JMenuItem open_project;
	private JMenuItem save_project;
	private JMenuItem close_project;
	/////////////////////////////////
	private JMenuItem create_file;
	private JMenuItem open_file;
	private JMenuItem save_file;
	private JMenuItem close_file;
	/////////////////////////////
	private JMenuItem findReplaceMenuItem;

	/////////////////////////////////

	private String project_dir;

	private ArrayList<File> files;
	private JTabbedPane tab_bar = new JTabbedPane(JTabbedPane.TOP);
	private ArrayList<Tab> tab = new ArrayList<Tab>();
	protected FindReplaceDialog searchTool = new FindReplaceDialog(this);
	
	
	////////////////////ONLY .java is acceptable//////////////////////
	private FilenameFilter javaFilter = new FilenameFilter()
    {
        @Override
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".java");
        }
    };
    //////////////////////////////////////////////////////////////////
    
	public MainFrame()
	{
		super("TEXT EDITOR"); 
		setUIStyle();//Set Program's Name
		createMenuItem();
		
		enableShortCutKeys(true);		//add shortcut keys 
		
		menuBar.add(project_menu);		//MenuBar(TaskBar) > menu(File) > each menuButton(new,create,..)	
		menuBar.add(file_menu);
		menuBar.add(edit_menu);
		
		setJMenuBar(menuBar); 			//Add the menu bar to the frame
		pack(); 						//no idea what this is but without it, menu bar won't display on the frame
		
		
		this.setSize(700,600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);				 //close the GUI also terminate program
		this.setVisible(true); 
	
        getContentPane().add(tab_bar);
	}
	
	private void createMenuItem()
	{
		//////////////////////Add menuButton to project menu////////////////////
		
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
		project_menu.add(close_project);
		
		///////////////////////Add menuButton to file menu////////////////////
		
		create_file = new JMenuItem("New File");
		create_file.addActionListener(this);
		file_menu.add(create_file);
		
		open_file = new JMenuItem("Open File");
		open_file.addActionListener(this);
		file_menu.add(open_file);
		project_menu.addSeparator();
		
		save_file = new JMenuItem("Save File (Save)");
		save_file.setEnabled(false);//initialize save_file menuItem in disable mode when no file to be saved
		save_file.addActionListener(this);
		file_menu.add(save_file);
		project_menu.addSeparator();
		
		close_file = new JMenuItem("Close File");
		close_file.addActionListener(this);
		file_menu.add(close_file);
		

		//Buid edit_menu with cutCopyPasteAction()
		cutCopyPasteAction();
		findReplaceMenuItem = new JMenuItem("Find/Replace");
		findReplaceMenuItem.setEnabled(false);//enable when exists a opened file. 
		edit_menu.add(findReplaceMenuItem);
		findReplaceMenuItem.addActionListener(this);
		

		////////////////////////////////////////////////////////////////////
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		////////////////////PROJECT////////////////////////////
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
		////////////////////FILE////////////////////////////
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
		else if(e.getSource()==findReplaceMenuItem) {
			searchTool.searchThisArea(getCurrentTab().getRSTA());
		}
	}
	
	////////////////////////PROJECT FUNCTION///////////////////////////////////
	private void open_project_function() throws IOException 
	{
		JFileChooser chooser = new JFileChooser(); 						//this class is to open file/directory
        chooser.setCurrentDirectory(new File(".")); 					//set current dir as window popup
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 	//will save current dir/selected dir
        int r = chooser.showOpenDialog(this); 
        
        if (r == JFileChooser.APPROVE_OPTION) {
        	
        	//get path of directory
            String path = chooser.getSelectedFile().getPath() + "\\src";
            
            //new File(path).listFiles(javaFilter) return array of files with filter
            //aslist will convert the array into ArrayList type
            files = new ArrayList<File>(Arrays.asList(new File(path).listFiles(javaFilter)));
            if(files == null)
            	return ;
            
            //Each file will be presented on a tab
            
            //Iterate through files, get contents, open and write on the tab
            for(int i=0; i<files.size();i++)
            {
            	//check path on output screen
            	System.out.println(files.get(i).getPath());
            	//create tab with string (readFileFromPath return the contents in string)
            	tab.add(new Tab(readFileFromPath(files.get(i).getPath()), files.get(i).getName(),files.get(i).getPath()));
            	
            	//make tab scrollable
            	tab_bar.addTab(tab.get(i).tabName, tab.get(i).text_area_with_scroll);
            	System.out.println(tab.get(i).tabName);
            }
            
            project_dir = path;
        }
        save_project.setEnabled(true);
        findReplaceMenuItem.setEnabled(true);
        return ;
	}
  
	private void create_project_function()
	{
		
		int response = JOptionPane.showConfirmDialog(null, "Choose a directory, this is where the project folder will be saved", null, JOptionPane.OK_CANCEL_OPTION);
		if( response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION)
			return;
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle( "Select Project" );
        chooser.setCurrentDirectory(new File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
       
		if( chooser.showOpenDialog( this ) != JFileChooser.APPROVE_OPTION ) 
			return;	
		
		String dir_path = chooser.getSelectedFile().getPath();
		
		// Name the Project Folder
		
		String folderName = JOptionPane.showInputDialog(null, "Choose a project name");
		if( folderName == null )
			return;
		
		////////check if folder already exists///////////
		if( new File( dir_path + "\\" + folderName ).exists() ) 
		{
			JOptionPane.showMessageDialog(null, "Project already exists", null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		////////////////////////////////////////////////
		
		new File(dir_path+ "\\" + folderName).mkdir();
		
		if( !new File(dir_path + "\\" + folderName).exists() )
		{
			JOptionPane.showMessageDialog(null, "Cant't create project, possible illegal character(s)", null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		dir_path += "\\" + folderName;	
       
	}
	private void save_project_function()
	{
		for(int i=0; i<files.size();i++)
        {
			String content = tab.get(i).textArea.getText();
		    try 
		    {
		    	BufferedWriter writer = new BufferedWriter(new FileWriter(files.get(i).getPath()));
				writer.write(content);
				writer.close();
				
			} 
		    catch (IOException e) 
		    {
				JOptionPane.showConfirmDialog(null, e.getMessage(),"Error Writing File",
						JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
			}
		    
		    
        }
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
	}
	
	////////////////////////FILE FUNCTION///////////////////////////////////

	private void open_file_function() throws IOException 
	{
		System.out.println("///////OPENING FILE////////////");
		JFileChooser chooser = new JFileChooser(); 						//this class is to open file/directory
        chooser.setCurrentDirectory(new File(".")); 					//set current dir as window popup
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); 		//will save current dir/selected dir
        int r = chooser.showOpenDialog(this); 
        
        if (r == JFileChooser.APPROVE_OPTION) 
        {
            String path = chooser.getSelectedFile().getPath();
            System.out.print(path);
            
           //create array of file with filter, then store all valid files in it
            File single_file = new File(path);
            
            //files.add(single_file); //add file to the files array
            
            System.out.println(path);
            if(single_file == null)
            	return;
        	//////////////////////////
            
            tab.add(new Tab(readFileFromPath(single_file.getPath()), single_file.getName(),single_file.getPath()));
        	
        	tab_bar.addTab(tab.get(tab.size()-1).tabName, tab.get(tab.size()-1).text_area_with_scroll);
        	
        	System.out.println(tab.get(tab.size()-1).tabName);
            
        }  
        //if file is successfully created, change save_file menuItem to enable
        if(!save_file.isEnabled()) {
			save_file.setEnabled(true);
			findReplaceMenuItem.setEnabled(true);
		}

        System.out.println("/////// END OPENING FILE////////////");
	}
	private void create_file_function()
	{
		System.out.println("///////////////NEW FILE///////////////////////");
		String fileName = JOptionPane.showInputDialog(null, "Enter the name of the new .java file", "Add File", JOptionPane.PLAIN_MESSAGE);
		if( fileName == null ) // this means that user clicked cancel or nothing
			return;
		if( !fileName.endsWith( ".java" ) )
		{
			fileName = fileName + ".java";
		}
		System.out.println(fileName);
		
		String temp = fileName.substring( 0, fileName.length() - 5);   
		if( !Character.isLetter( temp.charAt( 0 ) ) || !temp.matches("[a-zA-Z0-9]*" ) )
		{
			JOptionPane.showMessageDialog(null, "Illegal character(s) in file name\nFile name can only contain letters and numbers and must start with a letter", null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		

//		for( int i = 0; i < files.size(); i++) 
//		{
//			if(fileName.equals(files.get(i).getName())) 
//			{
//				JOptionPane.showMessageDialog(null, "File already exists", null, JOptionPane.ERROR_MESSAGE);
//				return;
//			}
//		}
		

		FileWriter file = null;
		if(project_dir == null)
		{
			int response = JOptionPane.showConfirmDialog(null, "Choose a directory, this is where the project folder will be saved", null, JOptionPane.OK_CANCEL_OPTION);
			if( response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION)
				return;
			
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle( "Select Project" );
	        chooser.setCurrentDirectory(new File("."));
	        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        
	       
			if( chooser.showOpenDialog( this ) != JFileChooser.APPROVE_OPTION ) 
				return;	
			
			project_dir = chooser.getSelectedFile().getPath();
		}
		

		String filePath = project_dir + "\\" + fileName;
		
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
		tab.add(new Tab(readFileFromPath(filePath), fileName, filePath));
    	
    	tab_bar.addTab(tab.get(tab.size()-1).tabName, tab.get(tab.size()-1).text_area_with_scroll);
		System.out.println("///////////////END NEW FILE///////////////////////");

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
		int index_selected_tab = tab_bar.getSelectedIndex();
		Tab current_selected_tab = tab.get(index_selected_tab);
		tab_bar.remove(index_selected_tab);
	}
	private String readFileFromPath(String filePath) //put all content of file to a string
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
	
	private void enableShortCutKeys(boolean enableMode) {
		if(enableMode==true) {
		project_menu.setMnemonic('P');
		file_menu.setMnemonic('F');
		edit_menu.setMnemonic('E');
		create_project.setAccelerator(KeyStroke.getKeyStroke('N',Event.CTRL_MASK));
		save_project.setAccelerator(KeyStroke.getKeyStroke('S',Event.CTRL_MASK));
	//We have to cast KeyEvent.VK_ to a char. If not, it will show a warning as below
		open_project.setAccelerator(KeyStroke.getKeyStroke('O',Event.CTRL_MASK));
		}
		findReplaceMenuItem.setAccelerator(KeyStroke.getKeyStroke('F',Event.CTRL_MASK));
	}


	public void cutCopyPasteAction() {
		Action cutAction = new DefaultEditorKit.CutAction();
		Action copyAction = new DefaultEditorKit.CopyAction();
		Action pasteAction = new DefaultEditorKit.PasteAction();
		
		cutAction.putValue(Action.NAME,"Cut");
		cutAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('X',Event.CTRL_MASK));
		edit_menu.add(cutAction);
		
		copyAction.putValue(Action.NAME,"Copy");
		copyAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('C',Event.CTRL_MASK));
		edit_menu.add(copyAction);
		
		pasteAction.putValue(Action.NAME,"Paste");
		pasteAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('V',Event.CTRL_MASK));
		edit_menu.add(pasteAction);
		
	}
	
	/**Description: This is a private function that create a GUI for 
	 * Find/Replace Menu Item when this Menu Item is clicked. 
	 * 
	 */
	private void setUIStyle() {
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        	System.out.println("NO SUPPORT FOR UI");
	}
		UIManager.put("MenuBar.background", Color.lightGray);
		UIManager.put("MenuItem.opaque",true);
		//UIManager.put("Menu.background", Color.GREEN);
		//UIManager.put("MenuItem.background", Color.lightGray);
		
	}
	
	private Tab getCurrentTab() {
		int index_selected_tab = tab_bar.getSelectedIndex();
		Tab current_selected_tab = tab.get(index_selected_tab);
		return current_selected_tab;
	}
	
	
	
}

