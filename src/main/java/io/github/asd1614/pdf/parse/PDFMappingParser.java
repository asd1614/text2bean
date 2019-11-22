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

package io.github.asd1614.pdf.parse;

import io.github.asd1614.pdf.parse.ini.Config;
import io.github.asd1614.pdf.parse.ini.Config.TYPE;
import io.github.asd1614.pdf.parse.ini.PDFMapper;
import io.github.asd1614.pdf.parse.text.PDFLayoutTextStripper;
import io.github.asd1614.pdf.parse.tools.ParserMethods;
import io.github.asd1614.pdf.parse.tools.ReflectionUtils;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFMappingParser {

    private Pattern map_key_pattern = Pattern.compile("([0-9a-zA-z]+)_([0-9a-zA-z]*)");

    /**
     * all object set key
     */
    public static final String ALL_OBJ_KEY = "all_object";

    private PDFMapper mapping;

    private MultiKeyMap pdfToObjMap = new MultiKeyMap();

    public PDFMappingParser(String name) {
        InputStream iniInput = this.getClass().getResourceAsStream(name);
        Ini ini = null;
        try {
            ini = new Ini();
            ini.getConfig().setEscape(false);
            ini.load(iniInput);
        } catch (IOException e) {
            throw new RuntimeException("ini file not found", e);
        }
        this.mapping = new PDFMapper(ini);
    }

    public PDFMappingParser(Ini ini) {
        this.mapping = new PDFMapper(ini);
    }

    public PDFMappingParser(PDFMapper mapping) {
        this.mapping = mapping;
    }

    public MultiKeyMap getRsult() {
        return pdfToObjMap;
    }

    public void parse(RandomAccess randomAccess) throws Exception{
        pdfToObjMap.clear();
        // parse pdf get then fulltext
        PDFParser pdfParser = new PDFParser(randomAccess);
        pdfParser.parse();
        PDDocument pdDocument = new PDDocument(pdfParser.getDocument());
        PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
        String fullText = pdfTextStripper.getText(pdDocument);
        // split newline char
        String[] splitLines = StringUtils.split(fullText, "\n");
        List<String> lines = Arrays.asList(splitLines);

        List<Config> configs = this.mapping.getConfigs();

        if (lines.isEmpty() || configs.isEmpty()) {
            return;
        }

        Deque<Config> queue = new LinkedList<>(configs);

        Config currentConfig = queue.pollFirst();
        int len = lines.size();
        for (int k = 0; k < len; k++) {
            String line = lines.get(k);

            if (currentConfig.getType() == TYPE.single) {
                if (singleParse(currentConfig,  line)) {
                    currentConfig = queue.pollFirst();
                }
            } else if (currentConfig.getType() == TYPE.list) {
                String nextLine = k+1 < len ? lines.get(k+1) : null;
                if (listParse(currentConfig, line, queue.peekFirst(), nextLine)) {
                    // end of table
                    currentConfig = queue.pollFirst();
                }
            } else if (currentConfig.getType() == TYPE.ignore){
                if (ignoreParse(currentConfig, line)) {
                    currentConfig = queue.pollFirst();
                }
            } else if (currentConfig.getType() == TYPE.block) {
                k = blockParse(currentConfig, lines, k, queue.peekFirst());
                // when block invoke end, poll new config from queue
                currentConfig = queue.pollFirst();
            }
            // queue is empty
            if (currentConfig == null) {
                break;
            }
        }
    }

    /**
     * parse text, mappiing to JavaBean
     * single process
     * @param config
     * @param line text
     * @return true matche
     */
    private boolean singleParse(Config config, String line) {
        boolean flag = false;
        Matcher matcher = config.getRegex().matcher(line);
        if (matcher.matches()) {
            flag = true;

            if (!config.getProperties().isEmpty()) {
                Object obj = this.getObject(config.getKey(), config.getType().name(), config.getClassName());
                for (Map.Entry<String, String> p : config.getProperties().entrySet()) {
                    String fieldName = p.getKey();
                    String valExp = p.getValue();
                    String val = this.evalExpression(valExp, matcher);
                    ReflectionUtils.invokeSetterMethod(obj, fieldName, val);
                }
                this.setObjToAllList(obj);
            }
        }

        return flag;
    }

    /**
     * type list process
     * @param config
     * @param line
     * @param nextConfig
     * @param nextLine
     * @return end of table true
     */
    private boolean listParse(Config config, String line, Config nextConfig, String nextLine) {

        boolean flag = false;
        // 判断当前行是否为尾行
        if (nextConfig != null && nextLine != null &&
                nextConfig.getRegex().matcher(nextLine).matches()) {
            flag = true;
        }

        Matcher matcher = config.getRegex().matcher(line);
        if (matcher.matches()) {
            if (!config.getProperties().isEmpty()) {
                Object obj = this.getObject(config.getKey(), config.getType().name(), config.getClassName());
                for (Map.Entry<String, String> p : config.getProperties().entrySet()) {
                    String fieldName = p.getKey();
                    String valExp = p.getValue();
                    String val = this.evalExpression(valExp, matcher);
                    ReflectionUtils.invokeSetterMethod(obj, fieldName, val);
                }
                if (config.getInAll()) {
                    this.setObjToAllList(obj);
                }
            }
        }
        return flag;
    }

    /**
     * type block process
     * @param blockConfig
     * @param lines
     * @param index
     * @param nextConfig
     * @return
     */
    private int blockParse(Config blockConfig, List<String> lines, int index, Config nextConfig) {
        // save original index
        int originalIndex = index;
        if (blockConfig.getChilds() == null || blockConfig.getChilds().isEmpty()) {
            return --index ;
        }

        int k = index;
        int len = lines.size();
        while(k < len) {
            Object obj = this.getObject(blockConfig.getKey(), blockConfig.getType().name(), blockConfig.getClassName(), false);
            boolean breakFlag = false;
            boolean saveFlag = false;
            Deque<Config> queue = new LinkedList<>(blockConfig.getChilds());
            Config currentConfig = queue.pollFirst();
            int lastMatcheIndex = k;
            while (currentConfig != null && k < len) {
                if (currentConfig.getType() == TYPE.single) {
                    StringBuffer text = new StringBuffer(lines.get(k));
                    // possessive quantifiers
                    if (currentConfig.getFlags() != null && currentConfig.getFlags() == Pattern.MULTILINE) {
                        int j = k;
                        while(j < len && !currentConfig.getRegex().matcher(text.toString()).matches()) {
                            j++;
                            if (j >= len) {
                                break;
                            }
                            text.append("\n").append(lines.get(j));
                        }
                        k = k >= len ? k : j;
                    }
                    Matcher matcher = currentConfig.getRegex().matcher(text.toString());
                    if (matcher.matches()) {
                        saveFlag = true;
                        if (!currentConfig.getProperties().isEmpty()) {
                            for (Map.Entry<String, String> p : currentConfig.getProperties().entrySet()) {
                                String fieldName = p.getKey();
                                String valExp = p.getValue();
                                String val = this.evalExpression(valExp, matcher);
                                ReflectionUtils.invokeSetterMethod(obj, fieldName, val);
                            }
                        }
                        currentConfig = queue.pollFirst();
                        lastMatcheIndex = k;
                    }
                } else if (currentConfig.getType() == TYPE.ignore){
                    String line = lines.get(k);
                    if (ignoreParse(currentConfig, line)) {
                        currentConfig = queue.pollFirst();
                        lastMatcheIndex = k;
                    }
                }
                k++;
                // block border check
                if (checkBlockBorder(blockConfig, lines, k)) {
                    // reset current index， poll new config
                    if (queue.peekFirst() != null) {
                        currentConfig = queue.pollFirst();
                        k = lastMatcheIndex;
                    } else {
                        break;
                    }
                }
            }
            // break determine
            if (k+1 < len && nextConfig != null) {
                breakFlag = nextConfig.getRegex().matcher(lines.get(k+1)).matches();
            }
            if (saveFlag) {
                String newKey = getObjectKey(blockConfig.getKey());
                List list = (List) this.pdfToObjMap.get(newKey, blockConfig.getType().name());
                list.add(obj);
                this.setObjToAllList(obj);
            }
            if (breakFlag) {
                break;
            }
        }

        return k >= len ? originalIndex : k;
    }

    /**
     * check border
     * @param border
     * @param lines
     * @param index
     * @return
     */
    private boolean checkBlockBorder(Config blockConfig, List<String> lines, int index) {
        Pattern border = blockConfig.getBorder();
        int borderLines = blockConfig.getBorderLines() == null ? 10 : blockConfig.getBorderLines();
        if (border == null) {
            return true;
        }
        StringBuffer text = new StringBuffer();
        int len = lines.size();
        for (int j = index; j < index + borderLines && j < len; j++) {
            text.append(lines.get(j)).append("\n");
            if (border.matcher(text.toString()).matches()) {
                break;
            }
        }
        return border.matcher(text.toString()).matches();
    }

    private String getObjectKey(String key) {
        String newKey = key;
        Matcher matcher = map_key_pattern.matcher(newKey);
        if (matcher.matches()) {
            newKey = matcher.group(1);
        }
        return newKey;
    }

    private <T> T getObject(String key, String type, Class<T> cls) {
        return getObject(key, type, cls, true);
    }

    /**
     * get object from pdfToObjMap, if is null, then according cls create new Object
     * @param key
     * @param type
     * @param cls
     * @param <T>
     * @return
     */
    private <T> T getObject(String key, String type, Class<T> cls, boolean addToMap) {
        String newKey = getObjectKey(key);
        if (TYPE.single == TYPE.get(type)) {
            T t = (T) this.pdfToObjMap.get(newKey, type);
            if (t == null) {
                try {
                    t = cls.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e );
                }
                if (addToMap) {
                    this.setObject(newKey, type, t);
                }
            }
            return t;
        } else if (TYPE.list == TYPE.get(type) || TYPE.block == TYPE.get(type)) {
            List list = (List) this.pdfToObjMap.get(newKey, type);
            if (list == null) {
                list = new LinkedList();
                setObject(newKey, type, list);
            }
            try {
                T t = cls.newInstance();
                if (addToMap) {
                    list.add(t);
                }
                return t;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e );
            }
        } else {
            try {
                T t = cls.newInstance();
                if (addToMap) {
                    setObject(newKey, type, t);
                }
                return t;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e );
            }
        }
    }

    private void setObject(String key, String type, Object obj) {
        this.pdfToObjMap.put(key, type, obj);
    }

    /**
     * put all object into set
     * @param obj
     */
    private void setObjToAllList(Object obj) {
        Set set = (Set) this.pdfToObjMap.get(ALL_OBJ_KEY, HashSet.class.getName());
        if (set == null) {
            set = new HashSet();
            this.pdfToObjMap.put(ALL_OBJ_KEY, HashSet.class.getName(), set);
        }
        set.add(obj);
    }

    /**
     * return all object set
     * @return set
     */
    public Set getAllList() {
        Set set = (Set) this.pdfToObjMap.get(ALL_OBJ_KEY, HashSet.class.getName());
        if (set == null) {
            set = new HashSet();
        }
        return set;
    }

    /**
     * eval expression
     * @param expression
     * @param matcher
     * @return
     */
    private String evalExpression(String expression, Matcher matcher) {
        if (ParserMethods.containsOperate(expression)) {
            return ParserMethods.eval(matcher, expression);
        } else {
            int group = Integer.valueOf(expression);
            return matcher.group(group);
        }
    }

    /**
     * type ignore process
     * @param config
     * @param line
     * @return
     */
    private boolean ignoreParse(Config config, String line) {
        Matcher matcher = config.getRegex().matcher(line);
        return matcher.matches();
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
