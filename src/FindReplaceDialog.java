

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

/**Description: This class provide an Find/Replace Dialog that gives
 * the user a tool to search over the current Text Area inorder to look up
 * a specified keyword. 
 * project.
 * @author 
 *
 */
public  class FindReplaceDialog {

	//declare control and variable used
			protected JLabel findLabel;
			protected JLabel replaceLabel;
			protected static JComboBox findComboBox;
			 final static int findComboBoxMax=15;
			protected JTextField replaceTextField;
			protected JButton findNextButton;
			protected JButton findPrevButton;
			protected JButton replaceButton;
			protected JButton replaceAllButton ;
			protected JButton closeButton;
			private JDialog findReplaceDialog;
			private RSyntaxTextArea textArea;
			String thisStr;
			boolean searchDirection; // if forward searchDirection =true. 
			
			
	/** Constructor that recevies a object JFrame as a parameter
	 * @param frame is the owner frame that a FindReplaceDialog object 
	 * is about to work on.  
	 */
	public FindReplaceDialog(JFrame frame) {
		buildFindReplaceDialog(frame);
	}
	
	
	/**
	 * @param visible
	 */
	public void setVisible(boolean visible) {
			findReplaceDialog.setVisible(visible);
	}
	 /**
	 * @param frame
	 */
	public void buildFindReplaceDialog(JFrame frame) {
		 	//declare control used
			thisStr="";
			searchDirection = true;
			findLabel=new  JLabel("Find");
			replaceLabel = new JLabel("Replace");
			//findedTextList= new String[10];
			findComboBox = new JComboBox();
			replaceTextField= new JTextField();
			findNextButton = new JButton("Find Next");
			findPrevButton = new JButton("Find Previous");
			replaceButton = new JButton("Replace");
			replaceAllButton = new JButton("Replace All");
			closeButton = new JButton("Close");
			findReplaceDialog=new JDialog(frame,"Find/Replace",true);
			//visible=false;
			
			//Build the 
			findReplaceDialog.setLayout(new GridBagLayout());//set layout manager for the Find/Replace Dialog
			findReplaceDialog.setSize(400,250);
			findReplaceDialog.setResizable(false);
			///////////// Add Label ///////////////
			GridBagConstraints gridCs= new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=0;
			gridCs.anchor = GridBagConstraints.CENTER;
			gridCs.insets = new Insets(5,5,5,5);
			findReplaceDialog.add(findLabel,gridCs);
			
			////add Find TextField
			findComboBox.setPreferredSize(new Dimension(170,20));
			findComboBox.setEditable(true);
			findComboBox.setFocusable(true);
			
			findComboBox.setBackground(Color.WHITE);
			////gridCs.anchor=GridBagConstraints.NORTH;
			gridCs.gridx=1;
			gridCs.gridy=0;
			gridCs.gridwidth=2;
			gridCs.insets = new Insets(5,5,5,5);
			gridCs.anchor = GridBagConstraints.WEST;
			findReplaceDialog.add(findComboBox,gridCs);
			findComboBox.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent e) {
		        	 if(findComboBox.getSelectedItem()!=null) {
		    			 thisStr = findComboBox.getSelectedItem().toString();
		            findNextButton.doClick();
		            updateFindComboBox(thisStr);
		        	 }
		         }
		      });
			
			////////////////// add replace Label ////////////////////
			gridCs= new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=1;
			gridCs.insets = new Insets(5,5,5,5);
			gridCs.anchor = GridBagConstraints.CENTER;
			findReplaceDialog.add(replaceLabel,gridCs);
			
			//////////////////// add replace TextField /////////////////
			gridCs= new GridBagConstraints();
			replaceTextField.setText("");
			replaceTextField.setColumns(17);
			replaceTextField.setBackground(Color.WHITE);
			gridCs= new GridBagConstraints();
			gridCs.gridx=1;
			gridCs.gridy=1;
			gridCs.ipadx=1;
			gridCs.gridwidth=2;
			gridCs.anchor = GridBagConstraints.WEST;
			gridCs.insets = new Insets(5,5,10,5);
			findReplaceDialog.add(replaceTextField,gridCs);
			
			//////////////add Find Next Button//////////////
			gridCs= new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=2;
			gridCs.insets = new Insets(5,10,10,5);
			gridCs.anchor = GridBagConstraints.WEST;
			findReplaceDialog.add(findNextButton,gridCs);
			findNextButton.setActionCommand("FindNext");
			findNextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					searchDirection=true;
					findButtonActionPerformed(e);
				}
			});
			//////////////add Find Prev Button//////////////
			gridCs= new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=3;
			gridCs.insets = new Insets(5,10,10,5);
			gridCs.anchor = GridBagConstraints.WEST;
			findReplaceDialog.add(findPrevButton,gridCs);
			findPrevButton.setActionCommand("FindPrevious");
			findPrevButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					searchDirection=false;
					findButtonActionPerformed(e);
				}
			});
			
			/////////////////// add Replace Button /////////////////////////
			replaceButton.setEnabled(true);
			gridCs= new GridBagConstraints();
			gridCs.gridx=1;
			gridCs.gridy=2;
			gridCs.insets = new Insets(5,5,5,5);
			gridCs.anchor = GridBagConstraints.CENTER;
			findReplaceDialog.add(replaceButton,gridCs);
			//replaceButton.addActionListener(this);
			
			/////////////////// add Replace All Button ///////////////
			replaceAllButton.setEnabled(true);
			gridCs= new GridBagConstraints();
			gridCs.gridx=1;
			gridCs.gridy=3;
			gridCs.anchor = GridBagConstraints.CENTER;
			gridCs.insets = new Insets(5,5,5,5);
			findReplaceDialog.add(replaceAllButton,gridCs);
			//replaceAllButton.addActionListener(this);
			
			/////////////////// add Close Button //////////////////////
			gridCs.gridx=2;
			gridCs.gridy=2;
			gridCs.anchor = GridBagConstraints.EAST;
			gridCs.insets = new Insets(5,5,5,5);
			findReplaceDialog.add(closeButton,gridCs);
			closeButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					findReplaceDialog.setVisible(false);
				}
			});
			
			gridCs.fill=GridBagConstraints.BOTH;
			findReplaceDialog.setLocationRelativeTo(frame);
			frame.pack();
			////findReplaceDialog.setVisible(showMode);
	 }
	 
	 /**This function keeps tracks of the keywords that users have searched. 
	  * The Maximum Number of searched keyword this box can hold is findComboBoxMax=15
	 ** @param str
	 **/
	private void updateFindComboBox(String str) {
		 if((findComboBox.getItemCount()==findComboBoxMax)&& str!="") {
			 findComboBox.removeItemAt(findComboBoxMax-1);
			 findComboBox.addItem(str);
		 }
		 else if(str!="") {
			 findComboBox.addItem(str);
		 }
	 }
	/**
	 * @param toBeSearchedArea
	 */
	public void searchThisArea(RSyntaxTextArea toBeSearchedArea) {
		setRSTA(toBeSearchedArea);
		setVisible(true);
		
	}
	 public void setRSTA(RSyntaxTextArea textArea) {
		 this.textArea=textArea;
	 }
	 
	 /**Source:
		 * https://www.codota.com/code/java/methods/org.fife.ui.rtextarea.SearchContext/setSearchForward
		 **/
	 private void findButtonActionPerformed(ActionEvent e) {
		 String command= e.getActionCommand();
		 SearchContext context = new SearchContext();
	    	  if(thisStr!="") {
	    		  context.setSearchFor(thisStr);
	    		  context.setMarkAll(true);
	    	      context.setMatchCase(true);
	    	      context.setRegularExpression(true);
	    	      context.setSearchForward(searchDirection);
	    	      context.setWholeWord(false);
	    	     // text
	    	      boolean found = SearchEngine.find(textArea, context).wasFound();
	    	      if (!found) {
	    	    	  System.out.println("NOT FOUND");
	    	    	  JOptionPane.showMessageDialog(null,this,"String Not Found",0);
	    	      	}
		      }
	    	  else return;
	     /* if(e.getSource()==replaceButton) {
	    	  String replaceWith=replaceTextField.getText().toString();
	    	  context.setReplaceWith(replaceWith); 
	      }*/
	 }
	 
}

