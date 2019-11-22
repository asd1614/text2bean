/*
 * Copyright 2019 asd1614
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.asd1614.pdf;

import io.github.asd1614.text.parse.TextParser;
import io.github.asd1614.text.parse.ini.ConfigFileMapper;
import io.github.asd1614.text.parse.text.PDFLayoutTextStripper;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.ini4j.Ini;

import java.io.File;
import java.io.InputStream;

public class PDFTextParser extends TextParser {

    public PDFTextParser(String name) {
        super(name);
    }

    public PDFTextParser(Ini ini) {
        super(ini);
    }

    public PDFTextParser(ConfigFileMapper mapping) {
        super(mapping);
    }

    public void parse(RandomAccess randomAccess) throws Exception{
        // parse pdf get then fulltext
        PDFParser pdfParser = new PDFParser(randomAccess);
        pdfParser.parse();
        PDDocument pdDocument = new PDDocument(pdfParser.getDocument());
        PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
        String fullText = pdfTextStripper.getText(pdDocument);
        super.parse(fullText);
    }

    public void parse(File file) throws Exception{
        RandomAccess randomAccess = new RandomAccessFile(file, "r");
        parse(randomAccess);
    }

    public void parse(InputStream input) throws Exception{
        RandomAccess randomAccess = new RandomAccessBuffer(input);
        parse(randomAccess);
    }

}
