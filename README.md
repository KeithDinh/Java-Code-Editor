# Simple Java Code Editor 

## KeyWord Count 

* **Keyword count uses the Java Abstract Syntax Tree, thus the keyword count is updated only when there are no compile errors.**

* Examples of valid keywords <br> 
```
for(int i = 0; i < 10; i++) {
}

for(;;)
  doSomething();
  
for( Object obj : arr ){
}

do{
}while(true);

while(true){}

if(true){}

if(true)
  i = 0;
else
  i = 1;
```

* Examples of **invalid** keywords <br> 
```
String s = "if";

// while 
```

## Compiling

* Compile All will copmile all project files. Similar to visual studio, if a file is not part of a project it will not be able to compile. 
* Compiling a file auto-save that file. Compiling Main and compile all will auto-save the project.

## Other

* As of right now the ClassLoader is not finished.  
