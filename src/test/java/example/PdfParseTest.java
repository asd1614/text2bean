package example;

import io.github.asd1614.pdf.parse.PDFMappingParser;
import io.github.asd1614.pdf.parse.text.PDFLayoutTextStripper;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

public class PdfParseTest {

    @Test
    public void readPDF2Text() throws Exception{

        InputStream pdf = this.getClass().getResourceAsStream("/simple/simple.pdf");
        RandomAccess randomAccess = new RandomAccessBuffer(pdf);
        PDFParser pdfParser = new PDFParser(randomAccess);
        pdfParser.parse();
        PDDocument pdDocument = new PDDocument(pdfParser.getDocument());
        PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
        String fullText = pdfTextStripper.getText(pdDocument);
        System.out.println(fullText);
    }


    @Test
    public void testPdfParser() throws Exception {
        PDFMappingParser parser = new PDFMappingParser("/simple.ini");
        InputStream pdf = this.getClass().getResourceAsStream("/simple/simple.pdf");
        parser.parse(pdf);
        MultiKeyMap result = parser.getRsult();
        System.out.println(result.size());

        Set set = parser.getAllList();
        for (Object obj: set) {
            System.out.println(obj.toString());
        }

    }
}
