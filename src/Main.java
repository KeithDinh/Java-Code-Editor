import java.awt.BorderLayout;
import javax.swing.*;

/* **************************************************************************
 * **************************** JAVA CODE EDITOR ****************************
 * ****************************     Group: C--   ****************************
 * Contributors: Caleb Strain, Kiet Dinh, Tuyen Cao, Y Nguyen, Zachary Brewer
 * ****************************     COSC 4353    ****************************
 * ****************************  Software Design ****************************
 * **************************************************************************
 * ****************************    References:    ***************************
 * - RSyntaxTextArea by bobbylight: github.com/bobbylight/RSyntaxTextArea
 * - Oracle Documentation: docs.oracle.com/javase/7/docs/api/javax/swing/
 * - Java2s: http://www.java2s.com/Code/Java/Swing-JFC
 * **************************************************************************
 */

public class Main 
{

	public static void main(String[] args) 
	{	
		//set UI STYLE for our program
		try {
           /* for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }*/
			//just for testing purpose. We can change to SystemLookAndFeel Later
            final UIManager.LookAndFeelInfo[] infos =
                    UIManager.getInstalledLookAndFeels();
            
            UIManager.setLookAndFeel(infos[3].getClassName());
            UIManager.getDefaults().put("Button.showMnemonics", Boolean.TRUE);
            // plafInfo[0]=Meta,plafInfo[1]=Nimbus,plafInfo=[2]=Motif,plafInfo[3,4]=Windows
            
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        	System.out.println("NO SUPPORT FOR UI");
        } catch (Exception system) {
            system.printStackTrace();
        }
		
		new MainFrame();
	}
	
}
