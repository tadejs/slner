package si.ijs.slner_test;

import si.ijs.slner.SloveneNER;
import org.junit.Test;
import si.ijs.slner.tei.Doc;
import si.ijs.slner.tei.DocReaders;
import si.ijs.slner.tei.Token;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;

public class SloveneNERTest {
    private void println(String in) {
        System.out.println(in);
    }

    @Test
    public void testSimpleThing() throws ClassNotFoundException, IOException, XMLStreamException {
        final String MODEL_PATH = "model.ser.gz";
        final String TEXT_PATH = "what.txt";

        FileInputStream in = new FileInputStream(MODEL_PATH);
        GZIPInputStream gis = new GZIPInputStream(in);

        SloveneNER ner = new SloveneNER(gis);
        File testFile = new File(TEXT_PATH);

        Doc doc = new Doc();
        List<List<String>> tags = ner.tagTokens(doc);
        assert tags.size() == 0;

        in.close();
        gis.close();
    }
}
