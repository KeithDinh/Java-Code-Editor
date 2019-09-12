import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

public class Compile implements Runnable
{
	public class CompilationResult
	{
		public boolean success;
		public boolean directoryExists;
		public boolean fileExists;
		public boolean fileEndsWithDotJava;
		public StringBuilder javacOutput;
		public boolean javacOutputComplete;
		public Exception javacOutputException;
		public boolean javacProcessExecuted;
		public Exception javacProcessExecutionException;
		public int javacExitCode;
		public boolean classFileCreated;
		public String errorMessage;

		public CompilationResult()
		{
			success = false;
			directoryExists = false;
			fileExists = false;
			fileEndsWithDotJava = false;
			javacOutput = null;
			javacOutputComplete = false;
			javacOutputException = null;
			javacProcessExecuted = false;
			javacProcessExecutionException = null;
			javacExitCode = -1;
			classFileCreated = false;
			errorMessage = null;
		}

		// for debug
		public void print()
		{
			System.out.print( "success:  " );  System.out.println( success );
			System.out.print( "directory exists:  " ); System.out.println( directoryExists );
			System.out.print( "source file exists:  " ); System.out.println( fileExists );
			System.out.print( "source file ends with .java:  " ); System.out.println( fileEndsWithDotJava );
			System.out.println( "javac standard output and standard error: " );  System.out.println( ( javacOutput == null ) ? ( "(none)" ) : ( javacOutput.toString() ) );
			System.out.print( "javac standard output and standard error complete:  " ); System.out.println( javacOutputComplete );
			System.out.print( "javac output exception:  " );  System.out.println( ( javacOutputException == null ) ? ( "(none)" ) : ( javacOutputException.getMessage() ) );
			System.out.print( "javac executed:  " ); System.out.println( javacProcessExecuted );
			System.out.print( "javac process execution exception:  " );  System.out.println( ( javacProcessExecutionException == null ) ? ( "(none)" ) : ( javacProcessExecutionException.getMessage() ) );
			System.out.print( "javac exit code:  " ); System.out.println( javacExitCode );
			System.out.print( "class filed created:  " );  System.out.println( classFileCreated );
			System.out.print( "error message:  " );    System.out.println( ( errorMessage == null ) ? ( "(none)" ) : ( errorMessage ) );
		}
	}

	private String directory;
	private String file;
	private InputStream processInputStream;
	private CompilationResult result;
		
	public Compile()
	{
		directory = null;
		file = null;
		processInputStream = null;
		result = null;
	}
	public Compile( String directory, String file )
	{
		this.directory = directory;
		this.file = file;
		processInputStream = null;
		result = null;
	}
	private Compile( InputStream processInputStream ) // constructor for thread that reads and saves standard output and standard error
	{
		directory = null;
		file = null;
		this.processInputStream = processInputStream;
		result = new CompilationResult();
	}

	public void setDirectory( String directory )
	{
		this.directory = directory;
	}
	public String getDirectory()
	{
		return directory;
	}
	public void setFile( String file )
	{
		this.file = file;
	}
	public String getFile()
	{
		return file;
	}
	private CompilationResult getCompilationResult()
	{
		return result;
	}

	// method for thread that reads and saves the standard output AND standard error from javac

	public void run()
	{
		if( processInputStream == null )
		{
			result.errorMessage = "the standard input/standard output stream is null";
			return;
		}

		BufferedReader br = null;
		try
		{
			result.javacOutput = new StringBuilder();
			br = new BufferedReader( new InputStreamReader( processInputStream ) );
			String line = br.readLine();
			while( line != null )
			{
				result.javacOutput.append( line );
				result.javacOutput.append( System.lineSeparator() );
				line = br.readLine();
			}
			result.javacOutputComplete = true;
			result.javacOutputException = null;
		}
		catch( Exception e )
		{
			result.javacOutputException = e;
			result.errorMessage = "unable to read all the standard output and standard error from javac";
		}
		finally
		{
			try { if( br != null ) br.close(); } catch( Exception ee ) { }
		}
	}

	// compiles one java source file

	public CompilationResult compile()
	{
		result = new CompilationResult();

		if( directory == null )
			result.errorMessage = "no directory";
		if( directory != null && Files.notExists( Paths.get( directory ) ) )
			result.errorMessage = "directory " + directory + " does not exists";
		result.directoryExists = true;

		if( file == null )
			result.errorMessage = "no source file";
		if( file != null && !file.endsWith( ".java" ) )
			result.errorMessage = "source file " + file + " does not end with .java";
		result.fileEndsWithDotJava = true;
		if( result.directoryExists && Files.notExists( Paths.get( directory + File.separator + file ) ) )
			result.errorMessage = "source file " + directory + File.separator + file + " does not exist";
		else
			result.fileExists = true;

		if( result.errorMessage != null )
			return result;

		Compile c = null;
		try
		{
			ProcessBuilder pb = new ProcessBuilder();

			// use cmd.exe /c javac <file> with Windows.  use sh -c javac <file> with UNIX

			if( System.getProperty( "os.name" ).toLowerCase().startsWith( "windows" ) )
				pb.command( "cmd.exe", "/c", "javac", file );
			else
				pb.command( "sh", "-c", "javac", file );

			pb.directory( new File( directory ) ); // set working directory
			pb.redirectErrorStream( true ); // combine standard output and standard error
			long beforeCompilationTime = System.currentTimeMillis(); // make sure the last modified time on the created class file is greater than the current time
			Process p = pb.start(); // execute javac
			c = new Compile( p.getInputStream() ); // create a new object for the thread that reads and saves standard output and standard error from javac
			result = c.getCompilationResult(); // use the CompilationResult from the new thread but already know we have a valid directory and file
			result.directoryExists = true;
			result.fileEndsWithDotJava = true;
			result.fileExists = true;
			new Thread( c ).start(); // get standard output and standard error from javac
			result.javacExitCode = p.waitFor(); // wait for javac to end
			if( result.javacExitCode != 0 )
				result.errorMessage = "javac did not return a 0 exit code";  // if compilation fails, javac will set the exit code to a non-zero value
			result.javacProcessExecuted = true;
			result.javacProcessExecutionException = null;

			if( result.javacExitCode == 0 ) // check to make sure the .class file was created even thought javac returned a 0 exit code
			{
				String classFile = directory + File.separator + file.substring( 0, file.length() - 5 ) + ".class";
				if( Files.exists( Paths.get( classFile ) ) )
				{
					long afterCompilationTime = Files.readAttributes( Paths.get( classFile ), BasicFileAttributes.class ).lastModifiedTime().toMillis();
					if( beforeCompilationTime > afterCompilationTime )
						result.errorMessage = "a new class file " + classFile + " was not created";
					else
						result.classFileCreated = true;
				}
				else
				{
					result.errorMessage = "class file " + classFile + " was not created";
				}
			}
		}
		catch( Exception e )
		{
			result.javacProcessExecutionException = e;
			result.errorMessage = "javac did not execute";
		}

		result.success =
				( result.directoryExists )
			&&	( result.fileExists )
			&&	( result.fileEndsWithDotJava )
			&&	( result.javacOutput != null )
			&&	( result.javacOutputComplete )
			&&	( result.javacOutputException == null )
			&&	( result.javacProcessExecuted )
			&&	( result.javacProcessExecutionException == null )
			&&	( result.javacExitCode == 0 )
			&&	( result.classFileCreated );
		return result;
	}

	// debug code

//	public static void main( String[] args )
//	{
//		String directory = ".";
//		String file = "Foo.java";
//
//		System.out.print( "Compiling " + directory + File.separator + file + "... " );
//		System.out.flush();
//
//		Compile.CompilationResult r = new Compile( directory, file ).compile();
//		if( r.success )
//		{
//			System.out.println( "done" );
//		}
//		else
//		{
//			System.out.println( "failed" );
//			if( r.javacOutput != null && r.javacOutputComplete )
//			{
//				System.out.print( r.javacOutput.toString() );
//			}
//			else
//			{
//				System.out.println( r.errorMessage );
//			}
//		}
//	}
}