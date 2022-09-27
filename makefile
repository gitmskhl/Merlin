

runAll: clean compile run

clean:
	rm com/interpreters/merlin/*.class -f
	rm com/interpreters/merlin/nativeFunctions/*.class
	rm com/interpreters/merlin/std/string/*.class -f


compile:
	javac com/interpreters/merlin/Merlin.java

run:
	java com.interpreters.merlin.Merlin

file: clean compile
	java com.interpreters.merlin.Merlin a.merlin

a:
	java com.interpreters.merlin.Merlin a.merlin

generate:
	rm com/interpreters/tools/Generate.class -f
	javac com/interpreters/tools/Generate.java
	java com.interpreters.tools.Generate com/interpreters/merlin
