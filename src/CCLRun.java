import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.*;
/*
CCLRun executes a Java program by loading it through a
CompilingClassLoader.
*/
public class CCLRun
{
    static public void main( String args[] ) throws Exception {
//        PrintStream originalOut=System.out;
//        PrintStream originalErr=System.err;
//    String userDir=System.getProperty("user.dir");
//    System.out.println(userDir);
//        PrintStream fileOut=new PrintStream(new File(userDir+"loadedMethod.txt"));
//        PrintStream fileErr=new PrintStream("./err.txt");
//        System.setOut(fileOut);
//        System.setErr(fileErr);
// The first argument is the Java program (class) the user
// wants to run
        String bin_dir=args[0];// this is the class path of the sub program Main
       // System.out.println("class path of Subprogram: "+bin_dir);
        String progClass = args[1];//this is the name of Class object of the subprogram. In this case It is Main
        //System.out.println("Name of subprogram "+ progClass);
// And the arguments to that program are just
// arguments 1..n, so separate those out into
// their own array
        String progArgs[] = new String[args.length-1];
        System.arraycopy( args, 1, progArgs, 0, progArgs.length );// copy arguments for the sub program Main
// Create a CompilingClassLoader
        CompilingClassLoader ccl = new CompilingClassLoader();
        ccl.set_class_path(bin_dir);
// Load the main class through our CCL
        Class clas = ccl.loadClass( progClass );
        //Get all methods in Main
        Method methods[] = clas.getDeclaredMethods();

//        System.out.println("Main");
//        for(Method meth: methods){
//            System.out.println("\t"+meth);
//        }
// Use reflection to call its main() method, and to
// pass the arguments in.
// Get a class representing the type of the main method's argument
        Class mainArgType[] = { (new String[0]).getClass() };
// Find the standard main method in the class
        Method main = clas.getMethod( "main", mainArgType );
// Create a list containing the arguments -- in this case,
// an array of strings
        Object argsArray[] = { progArgs };
// Call the method
        main.invoke( null, argsArray );
    }
}