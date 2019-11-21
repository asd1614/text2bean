package io.github.asd1614.pdf.parse.ini;

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
     * 边界行数匹配 默认10
     */
    private Integer borderLines = 10;
    /**
     * 正则表达式模式
     */
    private Integer flags;

    /**
     * 是否存入allList 默认true
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
