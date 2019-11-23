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

package io.github.asd1614.text.parse.support;

import io.github.asd1614.text.parse.TextProcessor;
import io.github.asd1614.text.parse.ini.Config;
import io.github.asd1614.text.parse.ini.Config.TYPE;
import io.github.asd1614.text.parse.support.spel.MappingEvaluationContext;
import io.github.asd1614.text.parse.support.spel.MatcherWrapper;
import io.github.asd1614.text.parse.tools.ReflectionUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StanderTextProcessor implements TextProcessor {

    private Pattern map_key_pattern = Pattern.compile("([0-9a-zA-z]+)_([0-9a-zA-z]*)");

    /**
     * all object set key
     */
    public static final String ALL_OBJ_KEY = "all_object";

    @Override
    public boolean singleParse(MappingEvaluationContext context) {

        boolean flag = false;
        Config config = context.getCurrentConfig();
        String line = context.getCurrentLine();
        Matcher matcher = config.getRegex().matcher(line);
        if (matcher.matches()) {
            flag = true;
            if (!config.getProperties().isEmpty()) {
                Object obj = this.getObject(context, config.getKey(), config.getType().name(), config.getClassName());
                for (Map.Entry<String, String> p : config.getProperties().entrySet()) {
                    String fieldName = p.getKey();
                    String valExp = p.getValue();
                    String val = this.evalExpression(context, valExp, matcher);
                    invokeSetter(obj, fieldName, val);
                }
                this.setObjToAllList(context, obj);
            }
        }

        return flag;
    }

    @Override
    public boolean listParse(MappingEvaluationContext context) {
        boolean flag = false;
        Config config = context.getCurrentConfig();
        String line = context.getCurrentLine();
        Config nextConfig = context.getConfigQueue().peekFirst();
        int index = context.getCurrentIndex();
        List<String> lines = context.getFullTextLines();
        int len = lines.size();
        String nextLine = index+1 < len ? lines.get(index+1) : null;
        // 判断当前行是否为尾行
        if (nextConfig != null && nextLine != null &&
                nextConfig.getRegex().matcher(nextLine).matches()) {
            flag = true;
        }

        Matcher matcher = config.getRegex().matcher(line);
        if (matcher.matches()) {
            if (!config.getProperties().isEmpty()) {
                Object obj = this.getObject(context, config.getKey(), config.getType().name(), config.getClassName());
                for (Map.Entry<String, String> p : config.getProperties().entrySet()) {
                    String fieldName = p.getKey();
                    String valExp = p.getValue();
                    String val = this.evalExpression(context, valExp, matcher);
                    invokeSetter(obj, fieldName, val);
                }
                if (config.getInAll()) {
                    this.setObjToAllList(context, obj);
                }
            }
        }
        return flag;
    }

    @Override
    public boolean blockParse(MappingEvaluationContext context) {
        Config blockConfig = context.getCurrentConfig();
        int index = context.getCurrentIndex();
        List<String> lines = context.getFullTextLines();
        Config nextConfig = context.getConfigQueue().peekFirst();
        // save original index
        int originalIndex = index;
        if (blockConfig.getChilds() == null || blockConfig.getChilds().isEmpty()) {
            context.setCurrentIndex(--index);
            return true ;
        }

        int k = index;
        int len = lines.size();
        while(k < len) {
            Object obj = this.getObject(context, blockConfig.getKey(), blockConfig.getType().name(), blockConfig.getClassName(), false);
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
                                String val = this.evalExpression(context, valExp, matcher);
                                invokeSetter(obj, fieldName, val);
                            }
                        }
                        currentConfig = queue.pollFirst();
                        lastMatcheIndex = k;
                    }
                } else if (currentConfig.getType() == TYPE.ignore){
                    String line = lines.get(k);
                    MappingEvaluationContext ignoreCtx = new MappingEvaluationContext();
                    ignoreCtx.setCurrentIndex(k);
                    ignoreCtx.setCurrentConfig(currentConfig);
                    ignoreCtx.setCurrentLine(line);
                    ignoreCtx.setConfigQueue(queue);
                    if (ignoreParse(ignoreCtx)) {
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
                List list = (List)context.getDataPool().get(newKey, blockConfig.getType().name());
                list.add(obj);
                this.setObjToAllList(context, obj);
            }
            if (breakFlag) {
                break;
            }
        }

        int returnIndex = k >= len ? originalIndex : k;
        context.setCurrentIndex(returnIndex);
        return true;
    }

    @Override
    public boolean ignoreParse(MappingEvaluationContext context) {
        Config config = context.getCurrentConfig();
        String line = context.getCurrentLine();
        Matcher matcher = config.getRegex().matcher(line);
        return matcher.matches();
    }

    /**
     * eval expression
     * @param expression
     * @param matcher
     * @return
     */
    private String evalExpression(MappingEvaluationContext context, String expression, Matcher matcher) {
        MatcherWrapper wrapper = new MatcherWrapper(matcher);
        SpelExpressionParser spelParser = new SpelExpressionParser();
        Expression exp = spelParser.parseExpression(expression);
        String val = exp.getValue(context, wrapper, String.class);
        return val;
    }

    /**
     * set val to object, even if target is a map
     * @param target
     * @param name
     * @param val
     */
    private void invokeSetter(Object target, String name, Object val) {
        if (target instanceof Map) {
            Map map = (Map) target;
            map.put(name, val);
        } else {
            ReflectionUtils.invokeSetterMethod(target, name, val);
        }
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

    private <T> T getObject(MappingEvaluationContext context, String key, String type, Class<T> cls) {
        return getObject(context, key, type, cls, true);
    }

    /**
     * get object from dataPool, if is null, then according cls create new Object
     * @param key
     * @param type
     * @param cls
     * @param <T>
     * @return
     */
    private <T> T getObject(MappingEvaluationContext context, String key, String type, Class<T> cls, boolean addToMap) {
        String newKey = getObjectKey(key);
        if (TYPE.single == TYPE.get(type)) {
            T t = (T) context.getDataPool().get(newKey, type);
            if (t == null) {
                t = newInstanceByClass(cls);
                if (addToMap) {
                    this.setObject(context, newKey, type, t);
                }
            }
            return t;
        } else if (TYPE.list == TYPE.get(type) || TYPE.block == TYPE.get(type)) {
            List list = (List) context.getDataPool().get(newKey, type);
            if (list == null) {
                list = new LinkedList();
                setObject(context, newKey, type, list);
            }
            T t = newInstanceByClass(cls);
            if (addToMap) {
                list.add(t);
            }
            return t;
        } else {
            T t = newInstanceByClass(cls);
            if (addToMap) {
                setObject(context, newKey, type, t);
            }
            return t;
        }
    }

    private <T> T newInstanceByClass(Class<T> cls) {
        try {
            T t = cls.newInstance();
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e );
        }
    }

    private void setObject(MappingEvaluationContext context, String key, String type, Object obj) {
        context.getDataPool().put(key, type, obj);
    }

    /**
     * put all object into set
     * @param obj
     */
    private void setObjToAllList(MappingEvaluationContext context, Object obj) {
        Set set = (Set) context.getDataPool().get(ALL_OBJ_KEY, HashSet.class.getName());
        if (set == null) {
            set = new HashSet();
            context.getDataPool().put(ALL_OBJ_KEY, HashSet.class.getName(), set);
        }
        set.add(obj);
    }

}
