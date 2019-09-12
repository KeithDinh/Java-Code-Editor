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
=======
Project Description
Fall 2019
A small code editor for Java programming language Code editors help programmers to develop software efficiently. 
They help developers to browse, edit, build and execute their programs. In this project, you develop a small editor.
Core functionality of the system:
    1. Open/create/close/save project
    2. Open/create/close/edit/save/remove file
    3. Compile project
    4. Execute project
    5. Providing real-time statistics about the number of keywords in the project
    6. Use blue to color for “if”, “else”, “for”, “while” keywords.
    7. Use red to color arithmetic and Boolean operators, e.g., “+”, “-“, “/”, “||”.
    8. Use green for strings.

Assumptions:
    • Each project is stored in separate directory from others.
    • There is only one project open in the editor
    • Content of each project is stored in separate directories that is selected by users.
    • A project has one Main.java file that includes the main method, and at most two other Java files, all in the same directory.
    • All dependencies (jar files) of a project is stored in a /lib subdirectory under the project directory

Timetable:
• Sept 5--- Lo-Fi prototype
• Sept 12--- 1
• Sept 26---2,6,7
• Oct 10---3,4,5
• Oct 24--- removing code smells
• Nov 7---new feature 1
• Nov 21---new feature 2
