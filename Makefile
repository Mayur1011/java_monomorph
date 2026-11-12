N ?= 1

all:clean
	javac testcases/Test$(N)/Test.java
	javac -cp .:soot-4.6.0-jar-with-dependencies.jar Main.java
	java -cp .:soot-4.6.0-jar-with-dependencies.jar Main Test$(N)/

clean:
	rm -rf testcases/Test$(N)/*.class
	rm -f *.class
	rm -rf sootOutput/*
