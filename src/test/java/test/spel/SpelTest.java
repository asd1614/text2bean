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

package test.spel;

import io.github.asd1614.text.parse.support.spel.MappingEvaluationContext;
import io.github.asd1614.text.parse.support.spel.MatcherWrapper;
import org.junit.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpelTest {

    @Test
    public void spelExpressionTest() {
        String str = "                 1.   2017 年 12 月-2018 年 11 月这是第一个表格类表单信息          ";
        String regex = "\\s*(\\d+\\.\\s*\\d{4}\\s*年\\s*\\d{0,2}\\s*月-\\d{4}\\s*年\\s*\\d{0,2}\\s*月\\S+)\\s*";
        String exp = "trim($1)";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        MatcherWrapper wrapper = new MatcherWrapper(matcher);
        SpelExpressionParser spelParser = new SpelExpressionParser();
        EvaluationContext ctx = new MappingEvaluationContext();

        Expression expression = spelParser.parseExpression(exp);
        Object val = expression.getValue(ctx, wrapper);
        System.out.println(val);
    }
}
