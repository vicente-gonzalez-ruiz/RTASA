%.class: %.java
	javac $*.java

RTASA.jar: RTASA.class
	jar cvfm RTASA.jar meta-inf/manifest.mf -C . *.class

EXE = RTASA.jar

all:	$(EXE)

jar:	all
	jar cvfm RTASA.jar meta-inf/manifest.mf -C . *.class

clean:
	rm -f *.class $(EXE)

test:	jar
	arecord -f cd | java -jar RTASA.jar 512 44100

install:	$(EXE)
	cp $(EXE) ~/bin

publish:
	rm -rf /tmp/RTASA 
	svn export . /tmp/RTASA
	tar --create --file=/tmp/RTASA.tar -C /tmp RTASA
	gzip -9 /tmp/RTASA.tar
	scp /tmp/RTASA.tar.gz www.ace.ual.es:~/public_html/imyso
	rm /tmp/RTASA.tar.gz
