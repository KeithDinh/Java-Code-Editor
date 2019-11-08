import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Execute // simple version, stdin, stdout, stderr handled by console
{
	private final File script;
	private final List<String> commands;
	private final String binDirectory;		
	private final String fileName;
	private String errorMessage;
	private boolean directoryExists;
	private boolean error;
	
	public Execute() 
	{
		commands = null;
		binDirectory = null;
		fileName = null;
		script = null;
		errorMessage = null;
		directoryExists = false;
		error = false;
	}
	
	public Execute( String binDirectory, String fileName ) 
	{
		this.fileName = fileName;
		this.binDirectory = binDirectory;
		error = false;
		
		if( System.getProperty( "os.name" ).toLowerCase().startsWith( "windows" ) ) 
		{
			commands = Arrays.asList("@echo off", "java Main", "@echo on", "@echo off", "pause", "exit");
			script = new File( binDirectory + "\\run.bat");
		}
		else // for linux/unix change later
		{
			commands = Arrays.asList("java Main", "echo","read -p \"Press enter to continue...\"", "exit 0");
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
		
		if( binDirectory == null )
			errorMessage = "no directory";
		if( binDirectory != null && Files.notExists( Paths.get( binDirectory ) ) )
			errorMessage = "directory " + binDirectory + " does not exists";
		else
			directoryExists = true;
		
		if( fileName == null )
			errorMessage = "no source file";
		if( fileName != null && !fileName.endsWith( ".class" ) )
			errorMessage = "source file " + fileName + " does not end with .java";
		
		if( directoryExists && Files.notExists( Paths.get( binDirectory + File.separator + fileName ) ) )
			errorMessage = "source file " + binDirectory + File.separator + fileName + " does not exist";

		if( errorMessage != null )
			return false;
		
		createScript();
		
		try
		{
			ProcessBuilder pb = new ProcessBuilder();

			if( System.getProperty( "os.name" ).toLowerCase().startsWith( "windows" ) )
				pb.command( "cmd.exe", "/c", "start", "cd" + binDirectory, "run" );
			else
				pb.command( "sh", "-c", "cd" + binDirectory, "./run.sh" );

			pb.directory( new File( binDirectory ) ); 
			pb.redirectErrorStream( true ); // combine standard output and standard error
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
