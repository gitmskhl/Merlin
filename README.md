# Merlin
Scripting programming language

This interpreter is written in Java and implements the Tree-walk interpreter model.


### Run

There are 2 ways to run the interpreter.
The first one is using makefile.

```
make compile
make run [filename]
```

For example, if your source code file is called source.merlin then run the following command

```
make compile
make run source.merlin
```

If you do not specify filename, the interpreter will start in prompt mode.


The second one is using java command.

```
javac com/interpreters/merlin/Merlin.java -d bin
java -cp ./bin/ com.interpreters.merlin.Merlin [filename]
```

### Language description
The Merlin language syntax is a symbiosis of the Python programming languages and C-like languages such as C++ and Java.
For example, Merlin supports C-like loop instructions

```
for (var i = 0; i < n; i += 1) {
    /// do something
}
```

At the same time, the language has Python-like loops

```
for i in range(n) {
    /// do something
}
```


Merlin is a dynamically typed programming language. The 'var' keyword is used to declare a variable.

```
var p = 16;
p = "Hello";
```

The language also supports functional programming and anonymous lambda functions.

```
/// some function to handle the event
def eventHandler(callback) {
    /// body
}


eventHandler(def () {//some actions});
```


The language supports object-oriented programming with classes, inheritance and polymorphism.

```
class MyFirstClass {
    /// this is a constructor of the Class
    def init(name) {
        this.name = name;
    }


    def getName() {
        return this.name;
    }
}


class MySecondClass : MyFirstClass {

    def init (name, age) {
        super(name);
        this.age = age;
    }

    def getAge() {
        return this.age;
    }

}



def main() {
    var a = MyFirstClass("Jhon"),
        b = MySecondClass("Alex", 24);

    print(a.getName() + " " + b.getAge());
}

main();

```

Also the language supports the ability to create variables with the same names in different parts of the code


```
var a = 6;

{
    var a = 17;

    {
        var a = 24;
        println(a);
    }

    println(a);
}

println(a);

```
Output:
```
24
17
6
```

The language supports the ability to create Python-style lists

```
from string import string;
from std import os;

var lst = [word for word in string(input()).split() if len(word) > 5]; 
```

Advanced indexing using lists is also supported

```
var lst = range(10, 20);

print(lst[range(0, 10, 2)]);
println(lst[[1, 3]])
```
Output:
```
[10, 12, 14, 16, 18]
[11, 13]
```

### Resolver
The language implementation contains a tool called a Resolver. It performs static code checking before the abstract syntax tree is built.
The resolver is able to catch many errors during the check phase before launch. For example, the Resolver will stop the execution of the following code without allowing the program to run:


```

var a = a + 4; /// Resolver error 

if (false) { /// Resolver warning: conditional instruction is deliberately false
    
}

```

