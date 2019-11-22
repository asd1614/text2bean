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

import io.github.asd1614.pdf.PDFTextParser;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Config {

    private String key;

    private Integer seq;

    private Class<?> className;

    /**
     * @see TYPE
     */
    private TYPE type;

    private Pattern regex;

    private Pattern border;

    /**
     * border lines
     * default 10
     */
    private Integer borderLines = 10;
    /**
     * Match flags, a bit mask that may include
     * {@link Pattern.CASE_INSENSITIVE}, {@link Pattern.MULTILINE}, {@link Pattern.DOTALL},
     * {@link Pattern.UNICODE_CASE}, {@link Pattern.CANON_EQ}, {@link Pattern.UNIX_LINES},
     * {@link Pattern.LITERAL}, {@link Pattern.UNICODE_CHARACTER_CLASS}
     * and {@link Pattern.COMMENTS}
     */
    private Integer flags;

    /**
     * default true
     * whether put to map with {@link PDFTextParser.ALL_OBJ_KEY}
     */
    private Boolean inAll = Boolean.TRUE;

    private Map<String, String> properties;

    /**
     * block child
     */
    private List<Config> childs;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public Class<?> getClassName() {
        return className;
    }

    public void setClassName(Class<?> className) {
        this.className = className;
    }

    public Pattern getRegex() {
        return regex;
    }

    public void setRegex(Pattern regex) {
        this.regex = regex;
    }

    public Pattern getBorder() {
        return border;
    }

    public void setBorder(Pattern border) {
        this.border = border;
    }

    public Integer getBorderLines() {
        return borderLines;
    }

    public void setBorderLines(Integer borderLines) {
        this.borderLines = borderLines;
    }

    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public Boolean getInAll() {
        return inAll;
    }

    public void setInAll(Boolean inAll) {
        this.inAll = inAll;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public List<Config> getChilds() {
        return childs;
    }

    public void setChilds(List<Config> childs) {
        this.childs = childs;
    }

    public static enum TYPE {
        single,
        list,
        block,
        ignore;

        public static TYPE get(String name) {
            return Enum.valueOf(TYPE.class, name);
        }
    }

    public String toString() {
        return this.key + ", " + this.regex;
    }
}
