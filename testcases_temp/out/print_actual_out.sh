#!/bin/bash

# Compile all 10 Java files
# ./clean.sh
# echo "Compiling Java files..."
# javac Main.java

# Run each compiled Java class
echo "Running Java programs..."
for i in {1..10}
do
    echo "Output of Testcase : TC0$i.java"
    javac TC0$i.java
    java TC0$i
    rm *.class
    echo "---------------------------"
done
# ./clean.sh