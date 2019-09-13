
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MainFrame extends JFrame implements ActionListener  {
	//declare controls used
	private JMenuBar menuBar = new JMenuBar();
	private JMenu file_menu = new JMenu("File");
	private JMenuItem create_project;
	private JMenuItem open_project;
	private JMenuItem save_project;
	private JMenuItem close_project;
	JTabbedPane javaClassTabPane= new JTabbedPane();
	JPanel[] javaClassPanels;
	JTextPane[] editorTextPanes;//=new JTextPane[10];
	JScrollPane[] editorScrollPanes;//=new JScrollPane[10];
	GridBagConstraints gridConstraints;
	private String path;//remember the current directory of the src folder.
						//I suggest to change "path" to "srcPath"
	
	////////////////////ONLY .java is acceptable//////////////////////
	private FilenameFilter javaFilter = new FilenameFilter()
    {
        //@Override
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".java");
        }
    };
    //////////////////////////////////////////////////////////////////
    
	public MainFrame()
	{
		
		super("TEXT EDITOR"); 		//Set Program's Name
		setResizable(true);
		this.setBackground(Color.white);
		//set layout manager for the main frame
		getContentPane().setLayout(new GridBagLayout());
		
		
		createMenuItem();
		menuBar.add(file_menu);		//menubar(taskbar) > menu(File) > each menuButton(new,create,..)	
		file_menu.setMnemonic('F');	
		setJMenuBar(menuBar); 		//Add the menu bar to the frame
		//packs the layout onto the frame and makes the content visible
		pack(); 	
		
		//position the MainFrame in the middle of the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((int)(0.5*(screenSize.width)-getWidth()),(int)
		(0.5*(screenSize.height-getHeight())),getWidth(),getHeight());
		
		this.setSize(500,300);
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //close the GUI also terminate program
		this.setVisible(true); 
		//I think  we should add this function, in case we have to do
		//save project, or ask user before closing. 
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				exitForm(e);
			}
		});
<<<<<<< HEAD
=======

>>>>>>> 8606e7bae5c80336f5a34e51f92ecec0f196c869
	}
	
	/**
	 * Build FileMenu
	 */
	private void createMenuItem()
	{
		//////////////////////////Add menuButton to file menu/////////////////////////
		create_project = new JMenuItem("Create Project");
		create_project.setAccelerator(KeyStroke.getKeyStroke('N',Event.CTRL_MASK));
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
	
	
	/**
	 *
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == create_project)
		{
			JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            int r = chooser.showOpenDialog(this); 		//show the browse box
            //chooser.addChoosableFileFilter(JFileChooser.Dire);
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
			save_project_function();
		}
		else if(e.getSource() == close_project)
		{
			close_project_function();
		}
	}
	
	/**
	 * This function implement actionPerformed when openMenuItem gets clicked
	 */
	private void open_project_function() {
		JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int r = chooser.showOpenDialog(this);
        
        if (r == JFileChooser.APPROVE_OPTION) {
            path = chooser.getSelectedFile().getPath() + "\\src";
            
          //create array of file with filter, then store all valid files in it
            File[] files = new File(path).listFiles(javaFilter);	
           //count
             int numTabs=files.length;
    		//position tab pane
    		gridConstraints = new GridBagConstraints();
    		gridConstraints.gridx=0;
    		gridConstraints.gridy=0;
    		gridConstraints.anchor=GridBagConstraints.WEST;
    		getContentPane().add(javaClassTabPane,gridConstraints);
    		//add change listener
    		javaClassTabPane.addChangeListener(new ChangeListener() {
    			public void stateChanged(ChangeEvent e) {
    				javaClassTabPaneStateChanged(e);
    			}
    		});
            //create a Tab Holder
            javaClassPanels= new JPanel[numTabs];
            editorTextPanes= new JTextPane[numTabs];
            //we don't really need an array of editor ScrollPane
            //we can create it during the for loop. 
            editorScrollPanes= new JScrollPane[numTabs];
            
            //build Tabs 
            for(int i=0; i<numTabs;i++) {
            	javaClassPanels[i] = new JPanel();
            	javaClassPanels[i].setLayout(new GridBagLayout());
            	editorTextPanes[i]= new JTextPane();
            	editorScrollPanes[i]= new JScrollPane();
            	editorScrollPanes[i].setPreferredSize(new Dimension(getWidth()-50,getHeight()-100));
            	System.out.print(getHeight());
            	editorScrollPanes[i].setViewportView(editorTextPanes[i]);
            	gridConstraints = new GridBagConstraints();
        		gridConstraints.gridx=0;
        		gridConstraints.gridy=0;
        		gridConstraints.insets= new Insets(5,5,5,5);
        		javaClassPanels[i].add(editorScrollPanes[i]);
        		javaClassTabPane.addTab(files[i].getName(), javaClassPanels[i]);
            	//read files to the text editor area
        		try{
        			//open input file
    				FileReader inputFile = new FileReader(files[i]);
    				editorTextPanes[i].read(inputFile,null);
    				inputFile.close();
    			}
    			catch(IOException ex){
    				JOptionPane.showConfirmDialog(null, ex.getMessage(),"Error Opening File",
    						JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
    			}//end catch
            }

            for (File file : files)
            {
                System.out.println(file.getName() + " : " + file.getPath());
                
            }
            System.out.println("Directory: " + path);
        }  
	}
	
	
	/**
	 * This function implements the action of the saveMenuItem
	 */
	private void save_project_function() {
		for(int i=0;i<javaClassTabPane.getTabCount();i++) {
			try {
				//javaClassTabPane.setSelectedIndex(i);
				FileWriter outputFile = new FileWriter(path+"\\"+javaClassTabPane.getTitleAt(i));
				editorTextPanes[i].write(outputFile);
			}catch(IOException e){
				JOptionPane.showConfirmDialog(null, e.getMessage(),"Error Writing File",
						JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void close_project_function() {
		//before closing, save project
		save_project.doClick();
		System.exit(0);
	}
	//We don't really need this function right now. 
	private void javaClassTabPaneStateChanged(ChangeEvent e) {
		
	}
	private void exitForm(WindowEvent e) {
		//We should prompt to user in order to ask for saving the file or not
		//Just suggest, it is up to y'all
		save_project.doClick();
		System.exit(0);
	}
}

//Testing save function
//Testing merging to git 
