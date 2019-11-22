package test.regex;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

    public void match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            System.out.println(matcher.group());
            int count = matcher.groupCount();
            System.out.println(String.format("group Count %s", count));
            for (int i=1; i<=count; i++) {
                System.out.println(String.format("group %s %s", i, matcher.group(i)));
            }
        }
    }

    @Test
    public void regexTest() {
        String regex = "\\s+报告编号:(\\d+)\\s+查询请求时间:([0-9\\.]+)\\s+(\\s1+[0-9\\:]+)\\s+报告时间:([0-9\\.]+)\\s+(\\s1+[0-9\\:]+)\\s*";
        String str = "        报告编号:243242342324       查询请求时间:2019.10.14             16:17:37         报告时间:2019.10.14          16:17:38          ";
        match(regex, str);
    }

    @Test
    public void groupRegexTest() {
        Pattern VALID_GROUP_ID_PATTERN = Pattern.compile("^\\$([\\p{L}\\p{N}]+|\\p{N}+)");
        Matcher matcher = VALID_GROUP_ID_PATTERN.matcher("$3");
        if (matcher.matches()) {
            System.out.println(matcher.group(1));
        } else {
            System.out.println("no matcher");
        }
    }
}
