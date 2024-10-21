.PHONY: all clean doc compile

LIB_JARS=`find -L lib/ -name "*.jar" | tr [:space:] :`

compile:
    mkdir -p classes
    javac -sourcepath src/main/java/biJump -classpath $(LIB_JARS) -d classes src/main/java/biJump/*.java

doc:
    mkdir -p doc
    javadoc -sourcepath src/main/java/biJump -classpath $(LIB_JARS) -d doc src/main/java/biJump/*.java