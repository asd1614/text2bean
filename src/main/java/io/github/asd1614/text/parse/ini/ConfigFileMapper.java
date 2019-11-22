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

import io.github.asd1614.text.parse.ini.Config.TYPE;
import org.apache.commons.lang.StringUtils;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * load ini file tool
 * Maintained list of config according to ini order
 */
public class ConfigFileMapper {

    private Ini ini;

    private List<Config> configs;

    public ConfigFileMapper(Ini ini) {
        this.ini = ini;
    }

    public ConfigFileMapper(String name) {
        InputStream input = this.getClass().getResourceAsStream(name);
        try {
            this.ini = new Ini();
            ini.getConfig().setEscape(false);
            this.ini.load(input);
        } catch (IOException e) {
            throw new RuntimeException("ini classpath invalid", e);
        }
    }


    public List<Config> getConfigs() {
        if (configs == null || configs.isEmpty()) {
            loadConfig();
        }
        return configs;
    }

    protected void loadConfig() {
        if (ini == null) {
            throw new NullPointerException("ini can not be null");
        }
        if (ini.isEmpty()) {
            configs = Collections.emptyList();
            return;
        }
        configs = new ArrayList<>(ini.size());
        for (Map.Entry<String, Section> el : ini.entrySet()) {
            String key = el.getKey();
            if (key.contains("/")) {
                // child config loading on parent parse
                continue;
            }
            Section section = el.getValue();
            Config config = createConfig(section);
            if (config == null) {
                continue;
            }
            config.setKey(key);
            if (config.getType() == TYPE.block) {
                // loading block node and itself children
                List<Config> childs = getChildSortList(config, section);
                config.setChilds(childs);
            }
            // if flags is not null recomile regex with flags
            if (config.getFlags() != null) {
                config.setRegex(Pattern.compile(config.getRegex().pattern(), config.getFlags()));
            }
            configs.add(config);
        }
        // sort list of config by seq
        Collections.sort(configs, new Comparator<Config>(){
            @Override
            public int compare(Config o1, Config o2) {
                return o1.getSeq().compareTo(o2.getSeq());
            }
        });
    }

    private Config createConfig(Section section) {
        Config config = new Config();
        if (section.isEmpty()) {
            return null;
        }
        config.setProperties(new HashMap<>(section.size()));
        for (Map.Entry<String, String> sectionEl : section.entrySet()) {
            if ("seq".equals(sectionEl.getKey())) {
                config.setSeq(Integer.valueOf(sectionEl.getValue()));
            } else if ("class".equals(sectionEl.getKey())) {
                String className = sectionEl.getValue();
                if (StringUtils.isBlank(className)) {
                    continue;
                }
                Class<?> cls = null;
                try {
                    cls = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("can not find class", e);
                }
                config.setClassName(cls);
            } else if ("type".equals(sectionEl.getKey())) {
                config.setType(Config.TYPE.get(sectionEl.getValue()));
            } else if ("regex".equals(sectionEl.getKey())) {
                String regex = sectionEl.getValue();
                config.setRegex(Pattern.compile(regex));
            } else if ("flags".equals(sectionEl.getKey())) {
                config.setFlags(Integer.parseInt(sectionEl.getValue(), 16));
            } else if ("childs".equals(sectionEl.getKey())) {
                continue;
            } else if ("border".equals(sectionEl.getKey())) {
                String regex = sectionEl.getValue();
                config.setBorder(Pattern.compile(regex, Pattern.MULTILINE));
            } else if ("borderLines".equals(sectionEl.getKey())) {
                String borderLines = sectionEl.getValue();
                if (borderLines != null) {
                    config.setBorderLines(Integer.parseInt(borderLines));
                }
            } else if ("inAll".equals(sectionEl.getKey())) {
                String inAll = sectionEl.getValue();
                config.setInAll(Boolean.parseBoolean(inAll));
            } else {
                config.getProperties().put(sectionEl.getKey(), sectionEl.getValue());
            }
        }
        return config;
    }

    /**
     * check config settings
     * @param config
     */
    private void checkConfig(Config config) {
        if (config.getType() == TYPE.ignore) {
            if (config.getRegex() == null)
                throw new IllegalArgumentException(String.format("%s.regex can not be null", config.getKey()));
        } else if (config.getType() == TYPE.single) {
            if (config.getClassName() == null && !config.getKey().contains("/"))
                throw new IllegalArgumentException(String.format("%s.class can not be null", config.getKey()));
            if (config.getRegex() == null)
                throw new IllegalArgumentException(String.format("%s.regex can not be null", config.getKey()));
        } else if (config.getType() == TYPE.list) {
            if (config.getClassName() == null)
                throw new IllegalArgumentException(String.format("%s.class can not be null", config.getKey()));
            if (config.getRegex() == null)
                throw new IllegalArgumentException(String.format("%s.regex can not be null", config.getKey()));
        } else if (config.getType() == TYPE.block) {
            if (config.getClassName() == null)
                throw new IllegalArgumentException(String.format("%s.class can not be null", config.getKey()));
            if (config.getRegex() == null)
                throw new IllegalArgumentException(String.format("%s.regex can not be null", config.getKey()));
            if (config.getBorder() == null)
                throw new IllegalArgumentException(String.format("%s.border can not be null", config.getKey()));
        }
    }

    /**
     * only type is block enter this method
     * according block.childs load child node
     * @param parent
     * @param section
     * @return sort list by seq
     */
    private List<Config> getChildSortList(Config parent, Section section) {
        if (section.get("childs") == null ) {
            return null;
        }
        String[] childs = StringUtils.split(section.get("childs"), " ");
        List<Config> configList = new LinkedList<>();
        if (childs != null ) {
            for (String child : childs) {
                Section childSection = section.getChild(child);
                if (childSection == null || childSection.isEmpty()) {
                    continue;
                }
                Config childConfig = createConfig(childSection);
                childConfig.setKey(parent.getKey() + "_" + child);
                childConfig.setClassName(parent.getClassName());
                configList.add(childConfig);
            }
        }
        if (!configList.isEmpty()) {
            Collections.sort(configList, new Comparator<Config>(){
                @Override
                public int compare(Config o1, Config o2) {
                    return o1.getSeq().compareTo(o2.getSeq());
                }
            });
        }
        return configList;
    }
}
