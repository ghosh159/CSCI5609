
JFLAGS = -g
JC = javac

CONFIG = server_config.txt
ID = 0
THREADS = 2

.SUFFIXES: .java .class

.java.class: 
	$(JC) $(JFLAGS) $*.java

CLASSES = \
		  FNV1aHash.java \
		  Node.java \
		  ChordNode.java \
		  Client.java \
		  DictionaryLoader.java \
		  CreateNodes.java

default: compile

compile: $(CLASSES:.java=.class)

clean:
	$(RM) *.class