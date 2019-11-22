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

package io.github.asd1614.text.parse.support.spel;

import io.github.asd1614.text.parse.TextProcessor;
import io.github.asd1614.text.parse.ini.Config;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Deque;
import java.util.List;

public class MappingEvaluationContext extends StandardEvaluationContext {

    static final Log log = LogFactory.getLog(MappingEvaluationContext.class);

    public MappingEvaluationContext() {
        setRootObject(null);
        this.addMethodResolver(new ParserMethodResolver());
        this.addPropertyAccessor(new ParserPropertyAccessor());
    }

    public MappingEvaluationContext(Object rootObject) {
        super();
        setRootObject(rootObject);
    }

    @Override
    public Object lookupVariable(String name) {
        log.debug("lookup variable name is " + name);
        return super.lookupVariable(name);
    }


    public Config getCurrentConfig() {
        return (Config) super.lookupVariable(TextProcessor.CURRENT_CONFIG);
    }

    public void setCurrentConfig(Config config) {
        this.setVariable(TextProcessor.CURRENT_CONFIG, config);
    }

    public String getCurrentLine() {
        return (String) super.lookupVariable(TextProcessor.CURRENT_LINE);
    }

    public void setCurrentLine(String line) {
        this.setVariable(TextProcessor.CURRENT_LINE, line);
    }

    public Integer getCurrentIndex() {
        return (Integer) super.lookupVariable(TextProcessor.CURRENT_INDEX);
    }

    public void setCurrentIndex(Integer index) {
        this.setVariable(TextProcessor.CURRENT_INDEX, index);
    }

    public Deque<Config> getConfigQueue() {
        return (Deque<Config>) super.lookupVariable(TextProcessor.CONFIG_QUEUE);
    }

    public void setConfigQueue(Deque<Config> queue) {
        this.setVariable(TextProcessor.CONFIG_QUEUE, queue);
    }

    public List<String> getFullTextLines() {
        return (List<String>) super.lookupVariable(TextProcessor.FULL_TEXTLINES);
    }

    public void setFullTextLines(List<String> fullTextLines) {
        this.setVariable(TextProcessor.FULL_TEXTLINES, fullTextLines);
    }

    public String getFullText() {
        return (String) super.lookupVariable(TextProcessor.FULL_TEXT);
    }

    public void setFullText(String fullText) {
        this.setVariable(TextProcessor.FULL_TEXT, fullText);
    }

    public MultiKeyMap getDataPool() {
        return (MultiKeyMap) super.lookupVariable(TextProcessor.PARSER_DATAPOLL);
    }

    public void setDataPool(MultiKeyMap dataPool) {
        this.setVariable(TextProcessor.PARSER_DATAPOLL, dataPool);
    }
}

