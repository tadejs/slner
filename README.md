# Slovene Named Entity Extractor

## Usage

Build `jar` or run `test`s:

```bash
gradle build
gradle test
```

Running with [run.sh](sbing/run.sh) wrapper.

```bash
./sbin/run.sh --out-model modelx.ser.gz --in ./corpus/jos16534_entities.tsv
```

## Usage (OLD):

Compile into single .jar file:
./build.sh

Download training dataset:
./download.sh

Train with downloaded corpus:
./train.sh

Evaluate with training corpus
java -jar slner-all.jar --in corpus.tei.xml

Train:
java -jar slner-all.jar --out-model model.ser.gz --in corpus.tei.xml

Tag:
java -jar slner-all.jar --in corpus.tei.xml --in-model model.ser.gz --out corpus_with_entities.tei.xml

For implementation details, see the [Štajner, T., Erjavec, T., Krek, S. (2013): Razpoznavanje imenskih entitet v slovenskem besedilu. Slovenščina 2.0, 1 (2): 58–81.](http://www.trojina.org/slovenscina2.0/arhiv/2013/2/Slo2.0_2013_2_04.pdf).

## License

- [Apache License, Version 2.0](LICENSE)