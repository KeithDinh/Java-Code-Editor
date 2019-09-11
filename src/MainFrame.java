import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


public class MainFrame extends JFrame implements ActionListener  {
	private JMenuBar menuBar = new JMenuBar();
	private JMenu file_menu = new JMenu("File");
	private JMenuItem create_project;
	private JMenuItem open_project;
	private JMenuItem save_project;
	private JMenuItem close_project;
	
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
		super("TEXT EDITOR"); 		//Set Program's Name
		createMenuItem();
		menuBar.add(file_menu);		//menubar(taskbar) > menu(File) > each menuButton(new,create,..)	
				
		setJMenuBar(menuBar); 		//Add the menu bar to the frame
		pack(); 	//no idea what this is but without it, menu bar won't display on the frame
		
		
		this.setSize(500,300);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //close the GUI also terminate program
		this.setVisible(true); 

	}
	private void createMenuItem()
	{
		//////////////////////////Add menuButton to file menu/////////////////////////
		create_project = new JMenuItem("Create Project");
		create_project.addActionListener(this);
		file_menu.add(create_project);
		
		open_project = new JMenuItem("Open Project");
		open_project.addActionListener(this);
		file_menu.add(open_project);
		
		save_project = new JMenuItem("Save Project");
		save_project.addActionListener(this);
		file_menu.add(save_project);
		
		close_project = new JMenuItem("Close Project");
		close_project.addActionListener(this);
		file_menu.add(close_project);
		///////////////////////////////////////////////////////////////////////////
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == create_project)
		{
			JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            int r = chooser.showOpenDialog(this); 		//show the browse box
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);	
            if (r == JFileChooser.APPROVE_OPTION) 	//if Open button is hit
            {
            	System.out.println(chooser.getCurrentDirectory().getPath());
            }
		}
		else if(e.getSource() == open_project)
		{
	            open_project_function();
		}
		else if(e.getSource() == save_project)
		{
			
		}
		else if(e.getSource() == close_project)
		{
			
		}
	}
	private void open_project_function() {
		JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int r = chooser.showOpenDialog(this);
        
        if (r == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getPath() + "\\src";
            
          //create array of file with filter, then store all valid files in it
            File[] files = new File(path).listFiles(javaFilter);	
            
           //print to test
            for (File file : files)
            {
                System.out.println(file.getName() + " : " + file.getPath());
            }
            System.out.println("Directory: " + path);
        }  
	}
}
