package test.regex;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

    public void match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
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
        String str = "        报告编号:xxxxxxxxxxxxxx       查询请求时间:2019.10.14             16:17:37         报告时间:2019.10.14          16:17:38          ";
        match(regex, str);
    }

}
