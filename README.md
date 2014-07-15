Slovene Named Entity Extractor
------------------------------


Licence: Apache 2.0

   Copyright 2012 Jozef Stefan Institute

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

For implementation details, see the [paper][slo20].

[slo20]: http://www.trojina.org/slovenscina2.0/arhiv/2013/2/Slo2.0_2013_2_04.pdf "Štajner, T., Erjavec, T., Krek, S. (2013): Razpoznavanje imenskih entitet v slovenskem besedilu. Slovenščina 2.0, 1 (2): 58–81. URL:" 

