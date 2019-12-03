import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Execute // IMPORTANT: stdin, stdout, stderr handled by console, no need to get streams as done in the Compile class. This is the only way to handle/capture user input without using external dependencies 
{
	private final File script;
	private final List<String> commands;
	private final String projectDirectory;
	private final String binDirectory;
	private final String libDirectory;
	private final String fileName;
	private String errorMessage;
	private boolean externalJARs;
	private boolean error;
	
	public Execute() 
	{
		commands = null;
		projectDirectory = null;
		binDirectory = null;
		libDirectory = null;
		fileName = null;
		script = null;
		errorMessage = null;
		error = false;
	}
	
	public Execute( String projectDirectory, String binDirectory, String libDirectory, boolean externalJARs, String fileName ) 
	{
		this.fileName = fileName;
		this.projectDirectory = projectDirectory; 
		this.libDirectory = libDirectory;
		this.binDirectory = binDirectory;
		this.externalJARs = externalJARs; 
		
		error = false;
		
		if( System.getProperty( "os.name" ).toLowerCase().startsWith( "windows" ) ) 
		{
			if( externalJARs ) 
				commands = Arrays.asList("@echo off", "java -cp lib\\*;bin Main", "@echo on", "@echo off", "pause", "exit");
			else 
				commands = Arrays.asList("@echo off", "java -cp bin Main", "@echo on", "@echo off", "pause", "exit");
			script = new File( binDirectory + "\\run.bat");
		}
		else // TODO: test on Unix
		{
			if( externalJARs )
				commands = Arrays.asList("java -cp lib\\*;bin Main", "echo","read -p \"Press enter to continue...\"", "exit 0");
			else
				commands = Arrays.asList("java -cp bin Main", "echo","read -p \"Press enter to continue...\"", "exit 0");
			script = new File(binDirectory + "\\run.sh");
		}
	}
	
	public String getErrorMessage() 
	{
		return errorMessage;
	}
	private void createScript() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter(script, "UTF-8");
		
		for( String command : commands ) 
		{
			writer.println(command);
		}
		
		writer.close();
	}
	
	public boolean execute() throws FileNotFoundException, UnsupportedEncodingException 
	{
		// Make sure all directories and files still exist / haven't been deleted by the user 
		if( projectDirectory == null ) 
			errorMessage = "Error: no project directory";
		if( projectDirectory != null && Files.notExists( Paths.get( projectDirectory ) )) 
			errorMessage = "Error: directory " + projectDirectory + " does not exists";
			
		if( errorMessage != null )
			return false; 
		
		if( binDirectory == null )
			errorMessage = "Error: no bin directory";
		if( binDirectory != null && Files.notExists( Paths.get( binDirectory ) ) )
			errorMessage = "Error: directory " + binDirectory + " does not exists";
		
		if( errorMessage != null )
			return false;
		
		if( externalJARs ) {
		if( libDirectory == null )
			errorMessage = "Error: no bin directory";
		if( libDirectory != null && Files.notExists( Paths.get( libDirectory ) ) )
			errorMessage = "Error: directory " + libDirectory + " does not exists";
		}
		
		if( errorMessage != null )
			return false;
		
		if( fileName == null )
			errorMessage = "Error: no source file";
		if( fileName != null && !fileName.endsWith( ".class" ) )
			errorMessage = "Error: class file " + fileName + " does not end with .class";
		
		if( Files.notExists( Paths.get( binDirectory + File.separator + fileName ) ) )
			errorMessage = "Error: class file " + binDirectory + File.separator + fileName + " does not exist";

		if( errorMessage != null )
			return false;
		
		createScript();
		
		try
		{
			ProcessBuilder pb = new ProcessBuilder();
			
			if( System.getProperty( "os.name" ).toLowerCase().startsWith( "windows" ) )
				// imitate Visual Studio console using bat file
				pb.command( "cmd.exe", "/c", "start", "bin\\run.bat" );
			else
				pb.command( "sh", "-c", binDirectory + "/run.sh" );
			
			pb.directory( new File( projectDirectory ) );  // set the directory
			Process p = pb.start(); // execute process
			if( p.waitFor() != 0 )
				error = true;
		}
		catch( Exception e )
		{
			errorMessage = "java did not execute";
			error = true;
		}
		
		return error ? false : true;
	}
}