package Config;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesReader {
    
	/* 取得translator的ID&key */
    public Map<String, String> getTranslatorProperties() {

        Properties props = new Properties();
        Map<String, String> map = new HashMap<String, String>();
        try {

            InputStream input = getClass().getResourceAsStream("Translator.properties");
            props.load(input);
            Enumeration<?> en = props.propertyNames();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String property = props.getProperty(key);
                map.put(key, property);
                //System.out.println(key + "  " + property);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}