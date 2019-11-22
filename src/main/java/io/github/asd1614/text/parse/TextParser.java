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

package io.github.asd1614.text.parse;

import io.github.asd1614.text.parse.ini.Config;
import io.github.asd1614.text.parse.ini.Config.TYPE;
import io.github.asd1614.text.parse.ini.ConfigFileMapper;
import io.github.asd1614.text.parse.support.StanderTextProcessor;
import io.github.asd1614.text.parse.support.spel.MappingEvaluationContext;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * according ini file parse text to Bean
 */
public class TextParser {

    private MultiKeyMap dataPool = new MultiKeyMap();

    private ConfigFileMapper mapping;

    private TextProcessor processor;

    public TextParser(String name) {
        InputStream iniInput = this.getClass().getResourceAsStream(name);
        Ini ini = null;
        try {
            ini = new Ini();
            ini.getConfig().setEscape(false);
            ini.load(iniInput);
        } catch (IOException e) {
            throw new RuntimeException("ini file not found", e);
        }
        this.mapping = new ConfigFileMapper(ini);
    }

    public TextParser(Ini ini) {
        this.mapping = new ConfigFileMapper(ini);
    }

    public TextParser(ConfigFileMapper mapping) {
        this.mapping = mapping;
    }

    public void parse(String text) {
        if (processor == null) {
            processor = new StanderTextProcessor();
        }
        dataPool.clear();
        // split newline char
        String[] splitLines = StringUtils.split(text, "\n");
        List<String> lines = Arrays.asList(splitLines);

        List<Config> configs = this.mapping.getConfigs();

        if (lines.isEmpty() || configs.isEmpty()) {
            return;
        }

        Deque<Config> queue = new LinkedList<>(configs);
        Config currentConfig = queue.pollFirst();
        // create expression evaluation context
        MappingEvaluationContext ctx = new MappingEvaluationContext();
        ctx.setConfigQueue(queue);
        ctx.setFullText(text);
        ctx.setFullTextLines(lines);
        ctx.setDataPool(this.dataPool);
        int len = lines.size();
        for (int k = 0; k < len; k++) {
            String line = lines.get(k);
            // init current ctx state
            ctx.setCurrentLine(line);
            ctx.setCurrentIndex(k);
            ctx.setCurrentConfig(currentConfig);
            if (currentConfig.getType() == TYPE.single) {
                if (processor.singleParse(ctx)) {
                    currentConfig = queue.pollFirst();
                }
            } else if (currentConfig.getType() == TYPE.list) {
                if (processor.listParse(ctx)) {
                    // end of table
                    currentConfig = queue.pollFirst();
                }
            } else if (currentConfig.getType() == TYPE.ignore){
                if (processor.ignoreParse(ctx)) {
                    currentConfig = queue.pollFirst();
                }
            } else if (currentConfig.getType() == TYPE.block) {
                processor.blockParse(ctx);
                k = ctx.getCurrentIndex();
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
     * return all object set
     * @return set
     */
    public Set getAllList() {
        Set set = (Set) this.dataPool.get(StanderTextProcessor.ALL_OBJ_KEY, HashSet.class.getName());
        if (set == null) {
            set = new HashSet();
        }
        return set;
    }

    public MultiKeyMap getRsult() {
        return this.dataPool;
    }

    public void parse(File file) throws Exception{
        String text = IOUtils.toString(new FileInputStream(file));
        parse(text);
    }

    public void parse(InputStream input) throws Exception{
        String text = IOUtils.toString(input);
        parse(text);
    }

    public TextProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(TextProcessor processor) {
        this.processor = processor;
    }
}
