package test.ini;

import io.github.asd1614.text.parse.ini.Config;
import io.github.asd1614.text.parse.ini.ConfigFileMapper;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniFileTest {

    @Test
    public void loadInit() {

        InputStream iniInput = this.getClass().getResourceAsStream("/simple.ini");
        Assert.assertNotNull(iniInput);

        try {
            Ini ini = new Ini();
            ini.getConfig().setEscape(false);
            ini.load(iniInput);

            ConfigFileMapper mapping = new ConfigFileMapper(ini);
            List<Config> configs = mapping.getConfigs();
            System.out.println(configs.size());
            System.out.println(ini.getComment());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void iniEnum() {
        System.out.println(Config.TYPE.get("ignore"));
        System.out.println(Config.TYPE.get("single"));
        System.out.println(Config.TYPE.get("list"));
    }

    @Test
    public void testRegexOnIni() {
        InputStream iniInput = this.getClass().getResourceAsStream("/simple.ini");
        Ini ini = null;
        try {
            ini = new Ini();
            ini.getConfig().setEscape(false);
            ini.load(iniInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(iniInput);
        for (Map.Entry<String, Section> el : ini.entrySet()) {
            Section section = el.getValue();
            if (section.isEmpty()) {
                continue;
            }
            System.out.println("###############################################################");
            String seq = section.get("seq");
            System.out.println(String.format("key %s, seq %s", el.getKey(), seq));
            String exp = section.get("regex");
            String testText = section.getComment("regex");
            System.out.println(String.format("regex: %s \ntext: %s", exp, testText));
            if (testText == null || exp == null) {
                continue;
            }
            Pattern pattern = Pattern.compile(exp);
            Matcher matcher = pattern.matcher(testText);
            Assert.assertTrue(matcher.matches());
            if (matcher.matches()) {
                int count = matcher.groupCount();
                System.out.println(String.format("group count %s ", matcher.groupCount()));
                for (int i=1; i<=count; i++) {
                    System.out.println(String.format("group %s %s ", i, matcher.group(i)));
                }
            }
        }
    }
}
