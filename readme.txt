tadej.stajner@ijs.si

Evaluate:
java -jar slner-all.jar --in corpus.tei.xml

Train:
java -jar slner-all.jar --out-model model.ser.gz --in corpus.tei.xml

Tag:
java -jar slner-all.jar --in corpus.tei.xml --in-model model.ser.gz --out corpus_with_entities.tei.xml

