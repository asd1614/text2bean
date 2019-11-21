package io.github.asd1614.pdf.parse.ini;

import org.ini4j.Ini;
import org.ini4j.spi.IniBuilder;

public class MyIniBuilder extends IniBuilder {

    private Ini _ini;

    /**
     * section read sequence
     */
    private int sequence = 1;

    private String currentSectionName;

    @Override
    public void startIni() {
        sequence = 1;
        super.startIni();
    }

    @Override
    public void startSection(String sectionName) {
        super.startSection(sectionName);
        currentSectionName = sectionName;
    }

    @Override
    public void endSection() {
        super.endSection();
        if (!this._ini.get(currentSectionName).containsKey("seq")) {
            this._ini.get(currentSectionName).put("seq", String.valueOf(sequence));
        }
        sequence++;
        currentSectionName = null;
    }

    public void setIni(Ini value)
    {
        _ini = value;
        super.setIni(value);
    }
}
