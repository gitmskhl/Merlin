

runAll: clean compile run

clean:
	rm com/interpreters/merlin/*.class -f

compile:
	javac com/interpreters/merlin/Merlin.java

run:
	java com.interpreters.merlin.Merlin

file: clean compile
	java com.interpreters.merlin.Merlin a.merlin

a:
	java com.interpreters.merlin.Merlin a.merlin