package si.ijs.slner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;

import javax.xml.stream.XMLStreamException;

import si.ijs.slner.tei.Doc;
import si.ijs.slner.tei.DocReaders;
import si.ijs.slner.tei.TsvWriter;

public class TEI2Tsv {

	public static void main(String[] args) throws ZipException, IOException, XMLStreamException {
		if (args.length != 2) {
			throw new RuntimeException("run: TEI2Tsv infile.xml outfile.tsv");
		}
		String in = args[0];
		String out = args[1];
		TsvWriter wri = new TsvWriter();
		
		System.out.println("Opening " + in);
		List<Doc> docs = DocReaders.openFile(new File(in));
		FileOutputStream fos = new FileOutputStream(out);
		System.out.println("Writing to " + out);
		for (Doc d : docs) {
			wri.write(d, fos);
		}
		fos.flush();
		fos.close();
		System.out.println("Done!");
		
	}
}
