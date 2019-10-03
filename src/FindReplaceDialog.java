
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
			
			protected JPanel searchOptionPanel;
			protected ButtonGroup searchOptionButtonGroup;//group contains all the search options
			protected JCheckBox matchCaseCheckBox;// seach match case option
			protected JCheckBox wholeWordCheckBox; // search whole word option
			protected JCheckBox regexCheckBox;//regularExpression search option
			
			protected JButton findNextButton;
			protected JButton findPrevButton;
			protected JButton replaceButton;
			protected JButton replaceAllButton ;
			protected JButton closeButton;
			private JDialog findReplaceDialog;
			private RSyntaxTextArea textArea;
			String keyString;
			boolean searchDirection; // if forward searchDirection =true. 
			
			
	/** Constructor that recevies a object JFrame as a parameter
	 * @param frame is the owner frame that a FindReplaceDialog object 
	 * is about to work on.  
	 */
	public FindReplaceDialog(JFrame frame) 
	{
		buildFindReplaceDialog(frame);
	}
	
	
	/**
	 * @param visible
	 */
	public void setVisible(boolean visible) 
	{
			findReplaceDialog.setVisible(visible);
	}
	
	
	 /**
	 * @param frame
	 */
	public void buildFindReplaceDialog(JFrame frame) 
	{
			findReplaceDialog=new JDialog(frame,"Find/Replace",true);//create the dialog
			GridBagConstraints gridCs;
			
			findLabel=new  JLabel("Find");
			replaceLabel = new JLabel("Replace");
			
			searchOptionPanel = new JPanel();
			matchCaseCheckBox = new JCheckBox("Match Case");	
			wholeWordCheckBox = new JCheckBox("Whole Word");
			regexCheckBox = new JCheckBox("Regular Expression");
			
			
			
			findComboBox = new JComboBox();			//create a dropdown to save past searching words
			replaceTextField= new JTextField();
			
			findNextButton = new JButton("Find Next");
			findPrevButton = new JButton("Find Previous");
			
			replaceButton = new JButton("Replace");
			replaceAllButton = new JButton("Replace All");
			
			closeButton = new JButton("Close");
			
			
			
			//Build the Dialog frame 
			findReplaceDialog.setLayout(new GridBagLayout());//set layout manager for the Find/Replace Dialog
			findReplaceDialog.setSize(390,270);
			findReplaceDialog.setResizable(false);
			
			///////////// Add findLabel ///////////////
			gridCs= new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=0;
			gridCs.anchor = GridBagConstraints.CENTER;
			gridCs.insets = new Insets(5,5,5,5);
			findReplaceDialog.add(findLabel,gridCs);
			
			//////////////Add search-Options selection///////////////
			searchOptionPanel.setLayout(new GridBagLayout());
			searchOptionPanel.setBorder(BorderFactory.createTitledBorder("Search Options"));
			//add matchCaseCheckBox to searchOptionPanel
			gridCs=new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=0;
			gridCs.insets= new Insets(5,5,5,5);
			searchOptionPanel.add(matchCaseCheckBox);
			//add WholeWord CheckBox to searchOptionPanel
			gridCs=new GridBagConstraints();
			gridCs.gridx=1;
			gridCs.gridy=0;
			gridCs.insets= new Insets(5,5,5,5);
			searchOptionPanel.add(wholeWordCheckBox);
			//add regexCheckBox to searchOptionPanel
			gridCs=new GridBagConstraints();
			gridCs.gridx=2;
			gridCs.gridy=0;
			gridCs.insets= new Insets(5,5,5,5);
			searchOptionPanel.add(regexCheckBox);
			//finally add searchOptionPanel to the findReplaceDialog
			gridCs=new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=2;
			gridCs.insets = new Insets(5,5,5,5);
			gridCs.gridwidth=3;
			findReplaceDialog.add(searchOptionPanel,gridCs);
			setDefault();//setdefault selection with findNext, Match Case
			
			////add findComboBox
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
			//add actionListener to receive events from this findComboBox
			findComboBox.addActionListener(new ActionListener() {
		         public void actionPerformed(ActionEvent e) {
		        	 if(findComboBox.getSelectedItem()!=null) {
		        		 findComboBox.setFocusable(true);
		    			 keyString = findComboBox.getSelectedItem().toString();
		    			 updateFindComboBox(keyString);
		        	 }
		         }
		      });
			
			////////////////// add replaceLabel ////////////////////
			gridCs= new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=1;
			gridCs.insets = new Insets(5,5,5,5);
			gridCs.anchor = GridBagConstraints.CENTER;
			findReplaceDialog.add(replaceLabel,gridCs);
			
			//////////////////// add replaceTextField /////////////////
			gridCs= new GridBagConstraints();
			replaceTextField.setText("");
			replaceTextField.setColumns(17);
			replaceTextField.setBackground(Color.WHITE);
			gridCs= new GridBagConstraints();
			gridCs.gridx=1;
			gridCs.gridy=1;
			gridCs.ipadx=1;
			gridCs.gridwidth=3;
			gridCs.anchor = GridBagConstraints.WEST;
			gridCs.insets = new Insets(5,5,10,5);
			findReplaceDialog.add(replaceTextField,gridCs);
			
			//////////////add Find Next Button//////////////
			gridCs= new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=3;
			gridCs.gridwidth = 1;
			gridCs.fill = GridBagConstraints.HORIZONTAL;
			gridCs.insets = new Insets(5,10,5,5);
			gridCs.anchor = GridBagConstraints.WEST;
			findReplaceDialog.add(findNextButton,gridCs);
			findNextButton.setActionCommand("FindNext");
		////// add new listener to receive events from this button//////
			findNextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					searchDirection=true;
					findButtonActionPerformed(e);
				}
			});
			//////////////add Find Prev Button//////////////
			gridCs= new GridBagConstraints();
			gridCs.gridx=0;
			gridCs.gridy=4;
			gridCs.gridwidth = 1;
			gridCs.fill = GridBagConstraints.HORIZONTAL;
			gridCs.insets = new Insets(5,10,5,5);
			gridCs.anchor = GridBagConstraints.WEST;
			findReplaceDialog.add(findPrevButton,gridCs);
			findPrevButton.setActionCommand("FindPrevious");
		////// add new listener to receive events from this button//////
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
			gridCs.gridy=4;
			gridCs.gridwidth = 1;
			gridCs.fill = GridBagConstraints.HORIZONTAL;
			gridCs.insets = new Insets(5,5,5,5);
			gridCs.anchor = GridBagConstraints.CENTER;
			findReplaceDialog.add(replaceButton,gridCs);
			////// add new listener to receive events from REPLACE button//////
			replaceButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					searchDirection=true;
					replaceButtonActionPerformed(e);
				}
			});
			
			/////////////////// add Replace All Button ///////////////
			replaceAllButton.setEnabled(true);
			gridCs= new GridBagConstraints();
			gridCs.gridx=1;
			gridCs.gridy=3;
			gridCs.gridwidth = 1;
			gridCs.fill = GridBagConstraints.HORIZONTAL;
			gridCs.anchor = GridBagConstraints.CENTER;
			gridCs.insets = new Insets(5,5,5,5);
			findReplaceDialog.add(replaceAllButton,gridCs);
			////// add new listener to receive events from REPLACE ALL button//////
					replaceAllButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							searchDirection=true;
							replaceButtonActionPerformed(e);
						}
					});
			
			/////////////////// add Close Button //////////////////////
			gridCs.gridx=2;
			gridCs.gridy=3;
			gridCs.gridwidth = 1;
			gridCs.fill = GridBagConstraints.HORIZONTAL;
			gridCs.anchor = GridBagConstraints.EAST;
			gridCs.insets = new Insets(5,5,5,10);
			findReplaceDialog.add(closeButton,gridCs);
		////// add new listener to receive events from this button//////
			closeButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					//if close button is clicked, hide this dialog
					findReplaceDialog.dispose();
					//i want to use clearMarkAllHighlights function, but
					//somehow it doesn't work. I have to do this manually 
					//to deselect the highlighted strings
					keyString="";
					findComboBox.setSelectedItem(keyString);
					findNextButton.doClick();
					setDefault();
				}
			});
			
			gridCs.fill=GridBagConstraints.BOTH;
			findReplaceDialog.setLocationRelativeTo(null);
			frame.pack();
	 }
	 
	
	 /**This function keeps tracks of the keywords that users have searched. 
	  * The Maximum Number of searched keyword this box can hold is findComboBoxMax=15
	 ** @param str
	 **/
	private void updateFindComboBox(String str) {
		 if((findComboBox.getItemCount()==findComboBoxMax)&& str!=null) 
		 {
			 findComboBox.removeItemAt(findComboBoxMax-1);
			 findComboBox.addItem(str);
		 }
		 else if(str!="") 
		 {
			 findComboBox.addItem(str);
		 }
	 }
	
	
	/**This function receives an RSyntaxTextArea object that it will do the find/replace action on. 
	 * @param toBeSearchedArea
	 */
	public void searchThisArea(RSyntaxTextArea toBeSearchedArea) 
	{
		setRSTA(toBeSearchedArea);
		setVisible(true);
		
	}
	
	
	 /**
	 * @param textArea
	 */
	public void setRSTA(RSyntaxTextArea textArea) {
		 this.textArea=textArea;
		 try {
		 keyString=textArea.getSelectedText().toString();
		 if(keyString.length()!=0) {
			 findComboBox.setSelectedItem(keyString);
		 }//catch error if no keyWord is selected for searching
		 }catch(Exception e) {
			 keyString="";
		 }
	 }
	 
	 /** This function will be called if findNext button is presed
	  * or findPrevious button is pressed.The function will search for the keyword
	  * based on the searchDirection. 
	  * This function will find the next matched keyword if searchDirection is Truel
	  * This function will find the previous match if searchDirection is False
	  * Source:
	  * https://www.codota.com/code/java/methods/org.fife.ui.rtextarea.SearchContext/setSearchForward
	  **/
	 private void findButtonActionPerformed(ActionEvent e) 
	 {
		 SearchContext context = new SearchContext();
		 //System.out.println(textArea.getCaretPosition());
    	  if(keyString.length()!=0 || keyString!=null || textArea.getText()!=null) 
    	  {
    		  context.setSearchFor(keyString);
    		  context.setMarkAll(true);//highlight all matched strings
    	      context.setMatchCase(matchCaseCheckBox.isSelected());
    	      context.setRegularExpression(regexCheckBox.isSelected());
    	      context.setSearchForward(searchDirection);
    	      context.setWholeWord(matchCaseCheckBox.isSelected());
    	     // text
    	     // textArea.setCaretPosition(0);
    	      boolean found = SearchEngine.find(textArea, context).wasFound();
    	      //this no thing is found, prompt a message to user
    	      if (!found) 
    	      {
    	    	  textArea.setCaretPosition(0);// start search from beginning
    	    	  SearchEngine.find(textArea,context);
    	    	  //JOptionPane.showMessageDialog(findReplaceDialog,"String Not Found","Find Result",JOptionPane.INFORMATION_MESSAGE);
    	      }
	      }
    	  else return;
	 }
	 
	 
	 /**Description: this function will be invoked when replace Button or replace All Button is clicked.
	 * @param e
	 */
	private void replaceButtonActionPerformed(ActionEvent e) {
		 String textReplace = replaceTextField.getText();
		
		 if(keyString.length()==0 || keyString==null || textArea.getText()==null) 
		 return ;
		 
		 if(!textReplace.equals(keyString)) {
			 System.out.println(textReplace);
			 SearchContext context = new SearchContext();
			 context.setSearchFor(keyString);
	   		  context.setMarkAll(true);
	   	      context.setMatchCase(matchCaseCheckBox.isSelected());
	   	      context.setRegularExpression(regexCheckBox.isSelected());
	   	      context.setSearchForward(searchDirection);
	   	      context.setWholeWord(matchCaseCheckBox.isSelected());
	   	      context.setReplaceWith(textReplace);
	   	     if(e.getSource()==replaceAllButton)
	   	    	  SearchEngine.replaceAll(textArea,context);
	   	     else
	   	    	  SearchEngine.replace(textArea,context);

		 }
   		 
	 }
	 
	 /**
	 * 
	 */
	private void setDefault() {
		 keyString="";
		 findComboBox.setSelectedItem(keyString);
		 replaceTextField.setText("");
		 matchCaseCheckBox.setSelected(true);//check match case option
		 searchDirection=true;
	 }
	 
}

