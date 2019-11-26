import java.io.*;

/*
 
Compiles your Java source code before loading it. Checks for non-existent .class files, or .class files that are older than there corresponding source code.
Java class files are not all loaded in memory at once, rather they are loaded on demand, as needed by the program.
*/

// FIX FORMATTING, ALSO WE NEED TO SET THE DIRECTORY SOMEHOW

public class CompilingClassLoader extends ClassLoader {
	// Given a filename, read the entirety of that file from disk
	// and return it as a byte array.
	private byte[] getBytes( String filename ) throws IOException {
	// Find out the length of the file
		File file = new File( filename );
		long len = file.length();
	// Create an array that's just the right size for the file's
	// contents
		byte raw[] = new byte[(int)len];
	// Open the file
		FileInputStream fin = new FileInputStream( file );
	// Read all of it into the array; if we don't get all,
	// then it's an error.
		int r = fin.read( raw );
		if (r != len)
			throw new IOException( "Can't read all, "+r+" != "+len );
	// Don't forget to close the file!
		fin.close();
		return raw;
	}
	// Spawn a process to compile the java source code file
	// specified in the 'javaFile' parameter. Return a true if
	// the compilation worked, false otherwise.
	private boolean compile( String javaFile ) throws IOException {
	// Let the user know what's going on
		MainFrame.outputClassLoader( "CCL: Compiling "+javaFile+"..." );
	// Start up the compiler
		Process p = Runtime.getRuntime().exec( "javac "+javaFile );
		// Wait for it to finish running
		try {
		p.waitFor();
		} catch( InterruptedException ie ) { System.out.println( ie ); }
		// Check the return code, in case of a compilation error
		int ret = p.exitValue();
		// Tell whether the compilation worked
		return ret==0;
	}

	public Class loadClass( String name, boolean resolve )
			throws ClassNotFoundException {
			
			MainFrame.clearClassLoader();
			Class clas = null;
			clas = findLoadedClass( name );
			MainFrame.outputClassLoader( "findLoadedClass: "+clas );
			
			String fileStub = name.replace( '.', '/' );
			String javaFilename = fileStub+".java";
			String classFilename = fileStub+".class";
			File javaFile = new File( javaFilename );
			File classFile = new File( classFilename );
			MainFrame.outputClassLoader( "j "+javaFile.lastModified()+" c "+ classFile.lastModified() );

			if (javaFile.exists() &&
			(!classFile.exists() ||
			javaFile.lastModified() > classFile.lastModified())) {
			try {
			if (!compile( javaFilename ) || !classFile.exists()) {
			throw new ClassNotFoundException( "Compile failed: "+javaFilename );
			}
			} catch( IOException ie ) {
				
			throw new ClassNotFoundException( ie.toString() );
			}
			}
			
			try {
			// read the bytes
			byte raw[] = getBytes( classFilename );
			// try to turn them into a class
			clas = defineClass( name, raw, 0, raw.length );
			} catch( IOException ie ) {
			// This is not a failure! If we reach here, it might
			}
			MainFrame.outputClassLoader( "defineClass: "+clas );
			// Maybe the class is in a library -- try loading
			// the normal way
			if (clas==null) {
			clas = findSystemClass( name );
			}
			MainFrame.outputClassLoader( "findSystemClass: "+clas );
			// Resolve the class, if any, but only if the "resolve"
			// flag is set to true
			if (resolve && clas != null)
			resolveClass( clas );
			// If we still don't have a class, it's an error
			if (clas == null)
			throw new ClassNotFoundException( name );
			// Otherwise, return the class
			return clas;
			}
}