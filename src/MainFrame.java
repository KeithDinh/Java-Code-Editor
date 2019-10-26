//package cmd_prompt;

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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
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
				private JMenuItem remove_file;
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
		private JTextArea console_text_area = new JTextArea();
		private JSplitPane splitPane= new JSplitPane();
		private JPanel bottom_terminal_panel = new JPanel();
		private JTabbedPane tab_bar = new JTabbedPane(JTabbedPane.TOP);
	//{
		private ArrayList<Tab> tab = new ArrayList<Tab>();
		private JTextArea terminal_tab;
		int lastCaretPositionFromOuput=0;
		int endCaretPos=0;
		private String completion="";
		private File istrFile;
		BufferedWriter bw;


	//}


//Global variables
	private String project_dir; 			//store current project path
	private String src_dir;
	private String last_project_path;		//will save the recent closed project path
	Process process, process2;						//for compile and execute
	Process Exprocess, Exprocess2;


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
		super("Java Editor by C--"); 				//Set Program's Name
		setIconImage(new ImageIcon("icons/javaTextEditorIcon2.PNG").getImage());
		getContentPane().setLayout(new GridLayout() );

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
        getContentPane().add(splitPane);

        //Center the frame on the Screen
        Dimension screenSize= Toolkit.getDefaultToolkit().getScreenSize();//screenSize
		setBounds((int)(0.5*(screenSize.width-getWidth())),
				(int)(0.5*(screenSize.height-getHeight())),getWidth(),getHeight());
	}

	private void createMenuItem()
	{
		//************ Add menuButton to project menu ************//

		create_project = new JMenuItem("New Project",new ImageIcon("icons/project-new.PNG"));
		create_project.addActionListener(this);
		project_menu.add(create_project);
		project_menu.addSeparator();


		open_project = new JMenuItem("Open Project",new ImageIcon("icons/project-open.PNG"));
		open_project.addActionListener(this);
		project_menu.add(open_project);
		project_menu.addSeparator();

		save_project = new JMenuItem("Save Project (Save All)",new ImageIcon("icons/project-save.PNG"));
		save_project.setEnabled(false);
		save_project.addActionListener(this);
		project_menu.add(save_project);
		project_menu.addSeparator();

		close_project = new JMenuItem("Close Project",new ImageIcon("icons/project-close.PNG"));
		close_project.addActionListener(this);
		close_project.setEnabled(false);
		project_menu.add(close_project);


		//************ Add menuButton to file menu ************//

		create_file = new JMenuItem("New File",new ImageIcon("icons/file-new.PNG"));
		create_file.addActionListener(this);
		file_menu.add(create_file);
		file_menu.addSeparator();

		open_file = new JMenuItem("Open File",new ImageIcon("icons/file-open.PNG"));
		open_file.addActionListener(this);
		file_menu.add(open_file);
		file_menu.addSeparator();

		save_file = new JMenuItem("Save File",new ImageIcon("icons/file-save.PNG"));
		save_file.addActionListener(this);
		file_menu.add(save_file);
		file_menu.addSeparator();
		save_file.setEnabled(false); //initialize save_file menuItem in disable mode when no file to be saved


		close_file = new JMenuItem("Close File",new ImageIcon("icons/file-close.PNG"));
		close_file.addActionListener(this);
		close_file.setEnabled(false);
		file_menu.add(close_file);
		file_menu.addSeparator();

		remove_file = new JMenuItem("Remove File",new ImageIcon("icons/file-remove.PNG"));
		remove_file.addActionListener(this);
		remove_file.setEnabled(false);
		file_menu.add(remove_file);

		//************ Add menuButton to edit menu ************//

		//Buid edit_menu with cut_copy_paste_action()
		cut_copy_paste_action();
		findReplaceMenuItem = new JMenuItem("Find/Replace",new ImageIcon("icons/edit-find-and-replace.PNG"));
		findReplaceMenuItem.setEnabled(false);//enable when exists a opened file.
		edit_menu.add(findReplaceMenuItem);
		findReplaceMenuItem.addActionListener(this);

		//************ Add menuButton to build menu ************//

		compile = new JMenuItem("Compile",new ImageIcon("icons/build-compile.PNG"));
		compile.addActionListener(this);
		compile.setEnabled(false);
		build_menu.add(compile);
		build_menu.addSeparator();

		execute = new JMenuItem("Execute",new ImageIcon("icons/build-run.PNG"));
		execute.addActionListener(this);
		execute.setEnabled(false);
		build_menu.add(execute);

	}

	private void enableShortCutKeys(boolean enableMode)
	{
		if(enableMode==true)
		{	//short cut keys for menus
			//project_menu.setMnemonic('P');
			//file_menu.setMnemonic('F');
			//edit_menu.setMnemonic('E');
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
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void add_close_tab_button(String title)
	{
		int index = tab_bar.indexOfTab(title);	//get the index from tab title

		JPanel Tab_with_close = new JPanel(new BorderLayout());
		ImageIcon disableCloseIcon= new ImageIcon("icons/tab-close1.PNG");
		ImageIcon enableCloseIcon= new ImageIcon("icons/tab-close2.PNG");
		JLabel tab_title = new JLabel(title);		//name of tab
		JButton close_button = new JButton(disableCloseIcon);	//x button
		//set a clear border for close button
		close_button.setBorder(BorderFactory.createEmptyBorder(0,5,0,0)); //change size
		close_button.setContentAreaFilled(false);
		close_button.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent e) {
				close_button.setIcon(enableCloseIcon);
			}
			public void mouseExited(MouseEvent e) {
				close_button.setIcon(disableCloseIcon);
			}
		});

		Tab_with_close.setOpaque(false);			//set components' bg color match tab's bg color
		Tab_with_close.add(tab_title,BorderLayout.WEST);
		Tab_with_close.add(close_button,BorderLayout.EAST);

		tab_bar.setTabComponentAt(index, Tab_with_close);	//set old tab with new feature(x button)

		close_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int index_selected_tab = tab_bar.indexOfTab(title);

				if(tab.get(index_selected_tab).modified == true)
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
				tab.remove(index_selected_tab);
				tab_bar.remove(index_selected_tab);
				if(tab.size()==0)
				{
					active_project_status(false);
				}
			}

		});
	}
	//***************PROJECT FUNCTIONS*******************//


	private void create_project_function()
	{
		System.out.println("***CREATE PROJECT***");
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

				//create folder
				new File(dir_path+ "\\" + folderName).mkdir();
				new File(dir_path+ "\\" + folderName + "\\lib").mkdir();
				new File(dir_path+ "\\" + folderName + "\\src").mkdir();

				if(new File(dir_path + "\\" + folderName).exists()) //created but not exist? -> invalid
				{
					dir_path += "\\" + folderName;
					project_dir =dir_path;
					src_dir=project_dir+"\\src";
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

		System.out.println("***END CREATE PROJECT***");

		return;
	}

	private void open_project_function() throws IOException
	{
		System.out.println("***OPENING PROJECT***");
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

            if(files == null)
            	return ;

            //Each file will be presented on a tab
            //Iterate through files, get contents, open and write on the tab
            for(int i=0; i<files.size();i++)
            {
            	//create tab with string (readFileFromPath return the contents in string)
            	open_file_on_new_tab(files.get(i).getName(),files.get(i).getPath());
            	add_close_tab_button(tab.get(i).tabName);
            }

            //if open project successfully
            active_project_status(true);
            //restore the path to current project folder
            project_dir = path;
            if(project_dir.endsWith("\\src"))
            {
            	project_dir=project_dir.substring(0, project_dir.length()-3);
            }
        }
	    System.out.println("***END OPENING PROJECT***");
        return;
	}






	/**
	 * This function will save the current project.
	 */
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


	/**This function will close the current active project before creating new project
	 * or opening another project
	 * @return boolean
	 */
	private boolean close_current_active_project() {
		if(project_dir != null||project_dir=="")
		{
			Object[] options = { "OK", "CANCEL" };
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
			if(tab.get(i).modified == true)
			{
				project_modified = true;
				break;
			}
		}
		if(project_modified == true)
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
				System.out.println("***STOP CLOSE PROJECT***");
				return  false;
			}
		}
		tab_bar.removeAll();
		active_project_status(false);
		tab.clear();
		last_project_path = project_dir;
		project_dir = null;
		System.out.println("***END CLOSE PROJECT***");
		return true;

	}



	/** This function will enable all menu items save_file, close_file, findReplaceMenuItem,
	 * save_project,close_project, compile,execute when a project is active. And this function
	 * will disable these menu items otherwise
	 * @param isActive
	 */
	protected void active_project_status(boolean isActive) {
		remove_file.setEnabled(isActive);
		save_file.setEnabled(isActive);
		close_file.setEnabled(isActive);
		findReplaceMenuItem.setEnabled(isActive);
		save_project.setEnabled(isActive);
		close_project.setEnabled(isActive);
		compile.setEnabled(isActive);
		execute.setEnabled(false);

	}


	//****************FILE FUNCTIONS******************//


	/**
	 * This function will create an default Main.java file when a project is created.
	 */
	private boolean create_Main_function()
	{
		System.out.println("***NEW FILE***");
		String fileName = "Main.java";
		System.out.println(project_dir);
		String filePath = project_dir + "\\src\\" + fileName;

		//-5 = remove ".java" to get the name only
		//create default content for Main.java file
		String contents = "public class Main\n{\n\tpublic static void main(String[] args)\n\t{\n\t}\n}";
		write_content_to_filepath(contents,filePath);

		//files.add(new File(filePath));
    	open_file_on_new_tab(fileName,filePath);
		System.out.println("***END NEW FILE***");
		return true;
	}


	/**
	 * This function will create a new file and display it on new Tab
	 */
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
			open_file_on_new_tab(fileName,filePath);

		System.out.println("***END NEW FILE***");
	}



	/**
	 * @throws IOException
	 */
	private void open_file_function() throws IOException
	{
		System.out.println("***OPENING FILE***");
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

           //create file with filter, then store all valid files in it
            File single_file = new File(path);

            if(single_file == null)
            	return;
        	//////////////////////////
            open_file_on_new_tab(single_file.getName(),single_file.getPath());
        }
        System.out.println("***END OPENING FILE***");
	}



	/**
	 *
	 */
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



	/**
	 *
	 */
	private void close_file_function()
	{
		if(getCurrentTab().modified == true)
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
		int index_selected_tab = tab_bar.getSelectedIndex();
		tab.remove(index_selected_tab);
		tab_bar.remove(index_selected_tab);
		if(tab.size()==0)
		{
			active_project_status(false);
		}
		System.out.println("***END CLOSE FILE***");
	}


	private void remove_file_function()
	{
		System.out.println(getCurrentTab().tabName);
		if(!getCurrentTab().tabName.equals("Main.java"))
		{
			Object[] options = { "Yes", "No" };
			int result = JOptionPane.showOptionDialog(null, "Do you want to delete file?", "Warning",
			        JOptionPane.DEFAULT_OPTION,
			        JOptionPane.WARNING_MESSAGE,
			        null, options, options[0]);
			if(result==0)
	        {
				String file_path_to_remove = getCurrentTab().path;
				int index_selected_tab = tab_bar.getSelectedIndex();
				tab.remove(index_selected_tab);
				tab_bar.remove(index_selected_tab);

				new File(file_path_to_remove).delete();
				if(tab.size() == 0)
					remove_file.setEnabled(false);
	        }
		}
		else
		{
			Object[] options = { "Yes", "No" };
			int result = JOptionPane.showOptionDialog(null, "Project will not compile without Main\nDo you want to delete Main?", "Warning",
			        JOptionPane.DEFAULT_OPTION,
			        JOptionPane.WARNING_MESSAGE,
			        null, options, options[0]);
			if(result==0)
	        {
				String file_path_to_remove = getCurrentTab().path;
				int index_selected_tab = tab_bar.getSelectedIndex();
				tab.remove(index_selected_tab);
				tab_bar.remove(index_selected_tab);
				new File(file_path_to_remove).delete();
				if(tab.size() == 0)
					remove_file.setEnabled(false);
	        }
		}
	}



	/**This function will write a string  to a file with a filePath is given.
	 * @param  content
	 * @param  filePath
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
	protected void open_file_on_new_tab(String fileName, String filePath) {
		tab.add(new Tab(
				readFileFromPath(filePath),
				fileName,
				filePath,
				new File(filePath)));

    	tab_bar.addTab(
    			tab.get(tab.size()-1).tabName,
    			tab.get(tab.size()-1).container);
    	add_close_tab_button(tab.get(tab.size()-1).tabName);

    	save_file.setEnabled(true);
    	close_file.setEnabled(true);
	}




	//****************EDIT FUNCTIONS*******************//
	/**
	 *
	 */
	public void cut_copy_paste_action() {
		Action cutAction = new DefaultEditorKit.CutAction();
		Action copyAction = new DefaultEditorKit.CopyAction();
		Action pasteAction = new DefaultEditorKit.PasteAction();

		cutAction.putValue(Action.NAME,"Cut");
		cutAction.putValue(Action.SMALL_ICON, new ImageIcon("icons/edit-cut.PNG"));
		cutAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('X',Event.CTRL_MASK));
		edit_menu.add(cutAction);
		edit_menu.addSeparator();

		copyAction.putValue(Action.NAME,"Copy");
		copyAction.putValue(Action.SMALL_ICON, new ImageIcon("icons/edit-copy.PNG"));
		copyAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('C',Event.CTRL_MASK));
		edit_menu.add(copyAction);
		edit_menu.addSeparator();

		pasteAction.putValue(Action.NAME,"Paste");
		pasteAction.putValue(Action.SMALL_ICON, new ImageIcon("icons/edit-paste.PNG"));
		pasteAction.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke('V',Event.CTRL_MASK));
		edit_menu.add(pasteAction);
		edit_menu.addSeparator();
	}

	//****************BUILD FUNCTIONS*******************//




	/**
	 * @throws IOException

	/*public void compile_function() throws IOException
	*/

	public void compile_function() throws IOException
	{
		String file_path;
		//1. get Main file that need to be compile
		if (new File(project_dir + "\\src").exists())            //if src folder exists set path in src
			file_path = project_dir + "src\\";                //else set path in folder
		else
			file_path = project_dir + "\\";

		System.out.println(file_path);                        //check if the path correct

		//2. Compile
		Process p;

            try {
                // We are running "dir" and "ping" command on cmd
                //p = Runtime.getRuntime().exec("cmd /c cmd.exe /k \"cd " + file_path );
                Runtime rt = Runtime.getRuntime();
                if (isWindows) {
                    p = rt.exec("cmd.exe /c cd \"" + file_path + "\" & start cmd.exe /c \"javac Main.java\"");
                }
                else {

                    p = rt.exec("cmd.exe /c cd \"" + file_path + "\" & start cmd.exe /c \"javac Main.java\""); //need Linux terminal command
                }
                //p = Runtime.getRuntime().exec("cmd cd /d"+ file_path);
                new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
                new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
                PrintWriter stdin = new PrintWriter(p.getOutputStream());
                stdin.flush();
                stdin.close();
                p.waitFor();

                if (p.exitValue() == 0) //if compile error -> exitValue() != 0
                {
                    execute.setEnabled(true);
                }

            } catch (Exception e) {
                System.out.println("Error Compiling file. Please check your Main file and try again!");
                e.printStackTrace();
            }
	}

	public static boolean isAlive(Process p) {
		try {
			p.exitValue();
			return false;
		}
		catch (IllegalThreadStateException e) {
			return true;
		}
	}
    boolean isWindows = System.getProperty("os.name")
            .toLowerCase().startsWith("windows");

	/**
	 * @throws IOException
	 */
	public void execute_function() throws IOException, InterruptedException {
		String file_path;
		//1. get Main file that need to be executed
		if (new File(project_dir + "\\src").exists())            //if src folder exists set path in src
			file_path = project_dir + "src\\";                //else set path in folder
		else
			file_path = project_dir + "\\";

		System.out.println(file_path);                        //check if the path correct

		//2.Execute main class file - works with user input
		Process p;

		try
		{
			Runtime rt = Runtime.getRuntime();
			p =rt.exec("cmd.exe /c cd \""+file_path+"\" & start cmd.exe /k \"java Main");

			new Thread (new SyncPipe(p.getErrorStream(),System.err)).start();
			new Thread (new SyncPipe(p.getInputStream(),System.out)).start();
			PrintWriter stdin = new PrintWriter(p.getOutputStream());
			stdin.flush();
			stdin.close();
			p.waitFor();
            p =rt.exec("&& exit"); //TODO close cmd window after executing

		}
		catch (Exception e)
		{
			System.out.println("ERROR EXECUTING FILE! Please recompile try again! ");
			e.printStackTrace();
		}

	}//end execute_function

	/**
	 * This function creates a terminal Pane in the bottom of the main Pane
	 */
	protected void openTerminal() {
	//with this layout, grid default is (1,1),when we add textArea to thisPanel,
	//this panel will be filled out completely
	bottom_terminal_panel.setLayout(new GridLayout());

	//the problem is what if user want to type in input?
	console_text_area.setEditable(false);
	console_text_area.setSize(600,50);
	console_text_area.setBackground(new Color(0,49,82));//set background color
	console_text_area.setForeground(new Color(255,255,255));//set text color
	console_text_area.setText("");
	//
	console();

	JTabbedPane terminal_tab_bar=new JTabbedPane();
	JScrollPane console_scroll_pane=new JScrollPane();
	console_scroll_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	console_scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	console_scroll_pane.setViewportView(console_text_area);
	ImageIcon console_icon= new ImageIcon("icons/terminal-console.PNG");
	String tooptip="This is a terminal";//hovering text
	JButton console_clear= new JButton(new ImageIcon("icons/terminal-clear"));

	terminal_tab_bar.addTab("Console",console_icon,console_scroll_pane,tooptip);
	bottom_terminal_panel.add(terminal_tab_bar);
	bottom_terminal_panel.setOpaque(true);

		//add clear button to console
		//JButton console_clear= new JButton(new ImageIcon("icons/terminal-clear"));
		StringBuilder input = new StringBuilder();
	/*console_text_area.addKeyListener(new KeyRelease=(){
		public void onKeyPress(KeyEvent e) {
			input.append(e.getKeyCode());//have to handler backspace and other
		}

	});*/
	}


	/**This function will write the current project directory and the  output to the
	 * terminal.
	 * @param output
	 * @param project_dir
	 */
	private void outputToTerminal(String output,String project_dir) {
		console_text_area.setText("");//clear console
		console_text_area.setText(project_dir+"\n"+output);
		console_text_area.setLineWrap(true);
		console_text_area.setForeground(Color.WHITE);
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
		});

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


	}

}//end MainFrame










