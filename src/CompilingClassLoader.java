import java.io.*;

/*
 
Compiles your Java source code before loading it. Checks for non-existent .class files, or .class files that are older than there corresponding source code.
Java class files are not all loaded in memory at once, rather they are loaded on demand, as needed by the program.

*/

public class CompilingClassLoader extends ClassLoader {

	private String className;
	
	public CompilingClassLoader( String className ) // In the case of our program this will be Main
	{
		this.className = className;
	}
	
	
}
