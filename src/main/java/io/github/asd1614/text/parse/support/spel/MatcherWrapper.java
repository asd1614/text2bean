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

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;

/**
 * wrapper Matcher as MappingEvaluationContext rootObject
 */
public class MatcherWrapper {

    private Matcher matcher;

    private boolean matches;

    public MatcherWrapper(Matcher matcher) {
        this.matcher = matcher;
        matches = this.matcher.matches();
    }

    public int groupCount() {
        return this.matcher.groupCount();
    }

    public String group(int group) {
        if (!matches) return null;
        return this.matcher.group(group);
    }

    public String group(String name) {
        if (!matches) return null;
        return this.matcher.group(name);
    }

    public String group() {
        if (!matches) return null;
        return this.matcher.group();
    }

    public boolean matches() {
        matches = this.matcher.matches();
        return matches;
    }

    /**
     * remove space string
     * @param expression  matcher group index
     * @return string
     */
    public String trim(String expression) {
        if (matches) {
            String val = getGroupVal(expression);
            return StringUtils.trim(val).replaceAll("\\s+", "");
        }
        return null;
    }

    /**
     * remove surplus space string, save only one space
     * @param expression matcher group index
     * @return string
     */
    public String surplus(String expression) {
        if (matches) {
            String val = getGroupVal(expression);
            if (val != null) {
                return StringUtils.trim(val).replaceAll("\\s{2,}", " ");
            }
        }
        return null;
    }

    public Matcher getMatcher() {
        return this.matcher;
    }

    /**
     * null safe
     * @param exp
     * @return string
     */
    private String getGroupVal(String exp) {
        String group = ParserPropertyAccessor.checkName(exp);
        String val = null;
        if (group != null && ParserPropertyAccessor.isDigit(group)) {
            int groupNum = Integer.parseInt(exp);
            val = matcher.group(groupNum);
        } else if (group != null) {
            val = matcher.group(group);
        } else if (group == null && ParserPropertyAccessor.isDigit(exp)) {
            int groupNum = Integer.parseInt(exp);
            val = matcher.group(groupNum);
        } else {
            val = exp;
        }
        return val == null ? "" : val;
    }
}
