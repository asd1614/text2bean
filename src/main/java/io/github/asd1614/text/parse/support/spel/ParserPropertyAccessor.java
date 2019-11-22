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

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * access Matcher group value
 */
public class ParserPropertyAccessor implements PropertyAccessor {

    private static final Pattern VALID_GROUP_ID_PATTERN = Pattern.compile("^\\$([\\p{L}\\p{N}]+|\\p{N}+)");

    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\p{N}+");

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class[]{MatcherWrapper.class};
    }

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        return this.read(context, target, name) != null;
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        MatcherWrapper matcher = this.getTarget(target);
        String group = checkName(name);
        if (group == null) {
            return null;
        } else if (isDigit(group) && Integer.parseInt(group) <= matcher.groupCount()) {
            return new TypedValue(matcher.group(Integer.parseInt(group)));
        } else {
            try {
                return new TypedValue(matcher.group(group));
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return false;
    }

    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {

    }

    private MatcherWrapper getTarget(Object target) {
        return (MatcherWrapper) target;
    }

    public static String checkName(String name) {
        Matcher matcher = VALID_GROUP_ID_PATTERN.matcher(name);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static boolean isDigit(String name) {
        return DIGIT_PATTERN.matcher(name).matches();
    }
}
