# Simple Java Code Editor 

## Notes 

* **Keyword count uses the Java Abstract Syntax Tree, thus it only counts valid if statements, else statements, while statements, and for statements. If the statment is not valid, it will not be counted as a keyword. Invalid keywords are those found in strings or comments or those that are do not have a valid condition. A for loop will not count as a keyword until it has a valid condition and body.**

* Examples of for loops that **will not** count as a keyword <br> 
```
for() {
}
for(int i = 0; i < 10; i++)
```
* Examples of for loops that **will** count as a keyword <br> 
```
for(int i = 0; i < 10; i++) {
}
for(;;)
  doSomething();
```

* Compile All will copmile all project files. Similar to visual studio, if a file is not part of a project it will not be able to compile. 
* Compiling a file auto-save that file. Compiling Main and compile all will auto-save the project.
* As of right now the ClassLoader is not finished.  
