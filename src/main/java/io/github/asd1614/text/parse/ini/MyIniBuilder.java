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

package io.github.asd1614.text.parse.ini;

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
