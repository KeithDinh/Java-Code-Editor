import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
 
/*
Compiles your Java source code before loading it. Checks for non-existent .class files, or .class files that are older than there corresponding source code.
Java class files are not all loaded in memory at once, rather they are loaded on demand, as needed by the program.
*/
 
public class CompilingClassLoader extends ClassLoader {
   
    private String binpath;
    private String srcpath;
    private String libpath;
    private String name;
    private boolean resolve;
    private boolean externalJARs;
    private List<Method> methods;
    private List<Class> loadedClasses;
   
    @SuppressWarnings("resource")
    private byte[] getBytes( String filename ) throws IOException {
        File file = new File( filename );
        long len = file.length();
        byte raw[] = new byte[(int)len];
        FileInputStream fin = new FileInputStream( file );
        int r = fin.read( raw );
        if (r != len)
            throw new IOException( "Can't read all, " + r + " != " + len );
        fin.close();
        return raw;
    }
   
    private void moveFile( File classFile )
    {
        File replace = new File(binpath + "\\" + classFile.getName() );
        if( replace.exists() )
            replace.delete();
        if(classFile.renameTo( new File(binpath + "\\" + classFile.getName() ) ) )
            classFile.delete();
    }
   
    private void moveClassFilestoBin()
    {
        File file = new File(binpath); // make sure that the bin folder still exists, if not create it
        if (!file.exists() || !file.isDirectory() )
            file.mkdir();
        new ArrayList<File>(Arrays.asList(new File(srcpath).listFiles(MainFrame.classFilter))).forEach( classFile -> moveFile(classFile) );
    }
       
    private boolean compile(String javaFileName ) throws IOException // maybe change later so that we store the compiled classes in a folder called bin
    {
        if( new Compile( srcpath, name + ".java", libpath, externalJARs ).compile().success )
        {
            moveClassFilestoBin();
            return true;
        }
        return false;
    }
   
    public CompilingClassLoader( String binpath, String srcpath, String libpath, String name, boolean resolve, boolean externalJARs )
    {
        this.binpath = binpath;
        this.srcpath = srcpath;
        this.libpath = libpath;
        this.name = name;
        this.resolve = resolve;
        this.externalJARs = externalJARs;
        methods = new ArrayList<Method>();
        loadedClasses = new ArrayList<Class>();
    }
   
    public List<Method> getMethods( )
    {
        return methods;
    }
   
    public List<Class> getLoadedClasses( )
    {
        return loadedClasses;
    }
   
    public Class loadClass() throws ClassNotFoundException
    {
        MainFrame.clearClassLoader();
       
        // Clas will not be loaded in memory and thus is will always be null
        Class clas = null;
       
        String fileStub = name.replace( '.', '/' );
        final String javaFilename = srcpath +   File.separator + fileStub + ".java";
        final String classFilename = binpath + File.separator + fileStub + ".class";
        File javaFile = new File( javaFilename );
        File classFile = new File( classFilename );
 
        if (javaFile.exists() && (!classFile.exists() || javaFile.lastModified() > classFile.lastModified())) {
            try {
                if (!compile( javaFilename ) || !classFile.exists())
                    throw new ClassNotFoundException( "Compile failed: "+ javaFilename );
            } catch( IOException ie ) {
                throw new ClassNotFoundException( ie.toString() );
            }
        }
           
        try {
            byte raw[] = getBytes( classFilename );
            clas = defineClass( name, raw, 0, raw.length );
        } catch( IOException ie ) {
        }
       
        // check to see if the class is already loaded?
        if (clas==null) {
            clas = findSystemClass( name );
        }
       
        if (resolve && clas != null)
            resolveClass( clas );
       
        if (clas == null)
            throw new ClassNotFoundException( name );
         
        if( clas.getDeclaredMethods() != null )
            methods.addAll(Arrays.asList(clas.getDeclaredMethods()));
       
        loadedClasses.add( clas );
       
        return clas;
    }
}