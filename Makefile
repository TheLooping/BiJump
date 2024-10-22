.PHONY: all clean doc compile

LIB_JARS=lib\bcprov-jdk18on-1.78.1.jar;lib\djep-1.0.0.jar;lib\jep-2.3.0.jar;lib\peersim-1.0.5.jar
SOURCE_PATH=src/peersim/biJump
OUTPUT_DIR=classes
DOC_DIR=doc

compile:
	if not exist classes mkdir classes
	copy src\resources\* classes
	javac -encoding UTF-8 -sourcepath $(SOURCE_PATH) -classpath $(LIB_JARS) -d $(OUTPUT_DIR) $(SOURCE_PATH)/*.java
doc:
	if not exist doc mkdir doc
	javadoc -sourcepath $(SOURCE_PATH) -classpath -classpath $(LIB_JARS) -d $(DOC_DIR) peersim.biJump

run:
	java -Xmx500m -cp $(LIB_JARS);classes peersim.Simulator example.cfg

all: compile run

clean:
	rd /s /q classes doc