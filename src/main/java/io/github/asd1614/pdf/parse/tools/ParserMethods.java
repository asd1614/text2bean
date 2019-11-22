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

package io.github.asd1614.pdf.parse.tools;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParserMethods {

    /**
     * determine expression if contains support method keyword
     */
    public static List<String> supportOperates = Arrays.asList("+", "_trim", "_surplus");

    public static boolean containsOperate(String exp) {
        for (String op : supportOperates) {
            if (exp.contains(op)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO need optimization
     * @param matcher
     * @param exp
     * @return
     */
    public static String eval(Matcher matcher, String exp) {
        if (exp.contains("+")) {
            return concat(matcher, exp);
        } else if (exp.contains("trim")) {
            return trim(matcher, exp);
        } else if (exp.contains("surplus")) {
            return trim(matcher, exp);
        } else {
            throw new IllegalArgumentException("do not support this operation");
        }
    }
    /**
     * concat string
     * eg:
     *  string : 2019.10.14             16:17:37
     *  regex : \s+(\S+)\s+(\S+)\s*
     *  1 = 2019.10.14
     *  2 = 16:17:37
     *  1+2 2019.10.1416:17:37
     * @param matcher
     * @param expression
     * @return do not match reutrn null
     */
    public static String concat(Matcher matcher, String expression) {
        if (matcher.matches()) {
            String[] groups = StringUtils.split(expression, "+");
            String[] strs = new String[groups.length];
            for (int i = 0; i < groups.length; i++) {
                strs[i] = matcher.group(Integer.valueOf(groups[i]));
            }
            return StringUtils.join(strs);
        } else {
            return null;
        }
    }

    /**
     * remove space string
     * eg:
     *  4_trim
     * @param matcher
     * @param expression
     * @return
     */
    public static String trim(Matcher matcher, String expression) {
        if (matcher.matches()) {
            String regex = "(\\d+)_trim";
            Pattern pattern = Pattern.compile(regex);
            Matcher trimMatcher = pattern.matcher(expression);
            if (trimMatcher.matches()) {
                int group = Integer.valueOf(trimMatcher.group(1));
                String val = matcher.group(group);
                return StringUtils.trim(val).replaceAll("\\s+", "");
            }
        }
        return null;
    }

    /**
     * remove surplus space string, save only one space
     * @param matcher
     * @param expression
     * @return
     */
    public static String surplus(Matcher matcher, String expression) {
        if (matcher.matches()) {
            String regex = "(\\d+)_surplus";
            Pattern pattern = Pattern.compile(regex);
            Matcher trimMatcher = pattern.matcher(expression);
            if (trimMatcher.matches()) {
                int group = Integer.valueOf(trimMatcher.group(1));
                String val = matcher.group(group);
                if (val != null) {
                    return StringUtils.trim(val).replaceAll("\\s{2,}", " ");
                }
            }
        }
        return null;
    }
}
