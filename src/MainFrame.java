import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;


public class MainFrame extends JFrame implements ActionListener  {
	private JMenuBar menuBar = new JMenuBar();
	
	private JMenu file_menu = new JMenu("File");
	private JMenu project_menu = new JMenu("Project");
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
	/////////////////////////////////
	private ArrayList<File> files;
	private JTabbedPane tab_bar = new JTabbedPane(JTabbedPane.TOP);
	private ArrayList<Tab> tab = new ArrayList<Tab>();

	
	
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
		super("TEXT EDITOR"); 			//Set Program's Name
		createMenuItem();
		menuBar.add(project_menu);		//MenuBar(TaskBar) > menu(File) > each menuButton(new,create,..)	
		menuBar.add(file_menu);
		
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
		
		save_project = new JMenuItem("Save Project (Save All)");
		save_project.addActionListener(this);
		project_menu.add(save_project);
		
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
		
		save_file = new JMenuItem("Save File (Save)");
		save_file.addActionListener(this);
		file_menu.add(save_file);
		
		close_file = new JMenuItem("Close File");
		close_file.addActionListener(this);
		file_menu.add(close_file);
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
			
		}
		else if(e.getSource() == close_file)
		{
			close_file_function();
		}
		//////////////////////////////////////////////////////
	}
	
	
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
            	return;
            
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
            
            
        }  
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
		
		if( new File( dir_path + "\\" + folderName ).exists() ) 
		{
			JOptionPane.showMessageDialog(null, "Project already exists", null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
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
	
	private void open_file_function() throws IOException 
	{
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
            System.out.println(path);
            if(single_file == null)
            	return;
        	//////////////////////////
            
            tab.add(new Tab(readFileFromPath(single_file.getPath()), single_file.getName(),single_file.getPath()));
        	
        	tab_bar.addTab(tab.get(tab.size()-1).tabName, tab.get(tab.size()-1).text_area_with_scroll);
        	System.out.println(tab.get(tab.size()-1).tabName);
            
        }  
	}
	private void create_file_function()
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
		
		//Check if directory already exists
		if( new File( dir_path + "\\" + folderName ).exists() ) 
		{
			JOptionPane.showMessageDialog(null, "Project already exists", null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//if not then create the desired directory
		new File(dir_path+ "\\" + folderName).mkdir();
		
		//
		if( !new File(dir_path + "\\" + folderName).exists() )
		{
			JOptionPane.showMessageDialog(null, "Cant't create project, possible illegal character(s)", null, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		dir_path += "\\" + folderName;	
       
	}
	private void save_file_function()
	{
		int index_selected_tab = tab_bar.getSelectedIndex();
		Tab current_selected_tab = tab.get(index_selected_tab);
		String content = current_selected_tab.content;
	    try 
	    {
	    	BufferedWriter writer = new BufferedWriter(new FileWriter(files.get(index_selected_tab).getPath()));
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
}
