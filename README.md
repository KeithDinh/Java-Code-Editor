# Java-Code-Editor

Important Notes:<br/>
Save All = Save Project<br/>
In the next iteration the View menu will be changed to Tabs, each tab will store a seperate JTextpane<br/>

Features that currently work:<br/>

View > file.java<br/>
File > New Project<br/>
File > Open<br/>
FIle > Add File<br/>
File > Remove File<br/>
File > Save<br/>
File > Save All<br/>
File > Exit<br/>
Build > Compile<br/>
Build > Compile All<br/>

Notes about features<br/>

File > Open Project...<br/>

This only opens a directory, and extracts all the .java files within the directory. If the user is opening a directory where the source files are stored in another directory called "src".<br/>
The software will automatically open the .java files from the "src" folder.<br/> 
If Main.java cannot be found then it outputs an error message<br/>

File > New Project...<br/>

This function creates a directory where the name is specified from user input. The method will automatically create a Main.java file<br/>

Build > Compile & Build > Compile All<br/>

Compiling a file will automatically save a file, because the function to compile requires the directory where the files are saved, thus it is necessary to save before compiling<br/>
