Slovene Named Entity Extractor
------------------------------



From the paper:
Tadej Štajner, Tomaž Erjavec and Simon Krek. Razpoznavanje imenskih entitet v slovenskem besedilu; In Proceedings of 15th Internation Multiconference on Information Society - Jezikovne Tehnologije 2012, Ljubljana, Slovenia



Licence: Apache 2.0

   Copyright 2012 Jožef Stefan Institute

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   
Usage:
-----------

Compile into single .jar file:
./build.sh

Download datasets:
./download.sh

Train with downloaded corpus:
./train.sh

Evaluate:
java -jar slner-all.jar --in corpus.tei.xml

Train:
java -jar slner-all.jar --out-model model.ser.gz --in corpus.tei.xml

Tag:
java -jar slner-all.jar --in corpus.tei.xml --in-model model.ser.gz --out corpus_with_entities.tei.xml

