#!/usr/bin/python

import sys
import codecs
from xml.dom.minidom import parse, parseString





if __name__ == '__main__':
	args = sys.argv
	input = args[-2]
	output = args[-1]

	with codecs.open(input, "r", "utf8") as inp:
		aa = inp.read()
		doc = parseString(aa.encode("utf8"))
		words = doc.getElementsByTagName("w")
		known = set()
		with codecs.open(output, "w", "utf8") as out:
			for word in words:
				lemm = word.getAttribute("lemma").rstrip(",-.")
				if lemm in known:
					continue
				
				known.add(lemm)
				out.write(lemm)
				out.write(u'\n')

	
	
	
