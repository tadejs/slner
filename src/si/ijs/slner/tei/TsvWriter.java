package si.ijs.slner.tei;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public class TsvWriter {

	public TsvWriter() {
		
	}
	
	public void write(Doc doc, OutputStream os) throws IOException {
		BufferedWriter wri = new BufferedWriter(new OutputStreamWriter(os)); 
		for (List<Token> sentence : doc.getSentences()) {
			writeSentence(wri, sentence);
		}
		wri.flush();
		wri.close();
	}

	public static void writeSentence(Writer wri, List<Token> sentence)
			throws IOException {
		for (Token tok : sentence) {
			wri.write(tok.getLiteral());
			wri.write('\t');
			if (tok.getLemma() != null) {
				wri.write(tok.getLemma());
				wri.write('\t' );
			}
			wri.write(tok.getPos());
			wri.write('\t');

			if (tok.getTokenClass() != null) {
				wri.write(tok.getTokenClass());
			} else {
				wri.write('-');
			}
			wri.write('\n');	
		}
		wri.write('\n');
	}
	
}
