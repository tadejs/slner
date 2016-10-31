4s-query --soft-limit 5000 dbpsl 'SELECT ?s ?nm WHERE { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Organisation>. ?s <http://www.w3.org/2000/01/rdf-schema#label> ?nm . } LIMIT 500 #EOQ'

