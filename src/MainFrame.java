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
	private JMenuItem create_project;
	private JMenuItem open_project;
	private JMenuItem save_project;
	private JMenuItem close_project;
	private File[] files;
	private JTabbedPane tab_bar = new JTabbedPane(JTabbedPane.TOP);
	private Tab[] tab;
	
	
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
	
        getContentPane().add(tab_bar);
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
            int response= chooser.showOpenDialog(this); 		//show the browse box
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);	
            if (response== JFileChooser.APPROVE_OPTION) 	//if Open button is hit
            {
            
            }
		}
		else if(e.getSource() == open_project)
		{
	            try {
					open_project_function();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		else if(e.getSource() == save_project) //Save all files
		{
			save_project_function();
		}
		else if(e.getSource() == close_project)
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
	}
	private void open_project_function() throws IOException {
		JFileChooser chooser = new JFileChooser(); 						//this class is to open file/directory
        chooser.setCurrentDirectory(new File(".")); 					//set current dir as window popup
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 	//will save current dir/selected dir
        int r = chooser.showOpenDialog(this); 
        
        if (r == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getPath() + "\\src";
            
           //create array of file with filter, then store all valid files in it
            files = new File(path).listFiles(javaFilter);
            tab = new Tab[files.length];
            
            
            for(int i=0; i<files.length;i++)
            {
            	tab[i]= new Tab(readFileFromPath(files[i].getPath()), files[i].getName());
            	tab_bar.addTab(tab[i].tabName, tab[i].text_pane_with_scroll);
            	System.out.println(tab[i].tabName);
            }
            
            
        }  
	}
	
	private void save_project_function()
	{
		for(int i=0; i<files.length;i++)
        {
			String content = tab[i].text_pane.getText();
		    try 
		    {
		    	BufferedWriter writer = new BufferedWriter(new FileWriter(files[i].getPath()));
				writer.write(content);
				writer.close();
			} 
		    catch (IOException e1) 
		    {
				e1.printStackTrace();
			}
		    
        }
	}

	private String readFileFromPath(String filePath) //put all content of file to a string
    {
        StringBuilder contentBuilder = new StringBuilder();
 
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
 
        return contentBuilder.toString();
    }
}
