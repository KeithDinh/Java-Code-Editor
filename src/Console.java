import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.*;

import javax.swing.*;

public class Console extends JDialog implements WindowListener 
{
	private JTextArea outputTextArea;
	private JScrollPane scrollPane;
	
	Console()
	{
		scrollPane = new JScrollPane();
		outputTextArea = new JTextArea();
		outputTextArea.setEditable( false );
		outputTextArea.setFont( new Font("Courier New", Font.PLAIN, 14));
		outputTextArea.setForeground( Color.white );
		outputTextArea.setBackground( Color.black );
		
		getContentPane().add(scrollPane);
		scrollPane.setViewportView( outputTextArea );
		setTitle("Console");
		setSize(700, 300);
	}
	
	public void append(String s) 
	{
		outputTextArea.append( s );
	}
	
	public void windowOpened(WindowEvent e) {
	}
	public void windowClosing(WindowEvent e) {
		outputTextArea.setText("");
	}
	public void windowClosed(WindowEvent e) {		
	}
	public void windowIconified(WindowEvent e) {
	}
	public void windowDeiconified(WindowEvent e) {
	}
	public void windowActivated(WindowEvent e) {
	}
	public void windowDeactivated(WindowEvent e) {
	}
}
