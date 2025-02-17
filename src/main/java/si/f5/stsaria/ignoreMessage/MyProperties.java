package si.f5.stsaria.ignoreMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MyProperties extends Properties {
    public int getPropertyInt(String key){
        return Integer.parseInt(getProperty(key));
    }
    public void setDefaultProperty(String key, String value){
        if (this.getProperty(key) == null){
            this.setProperty(key, value);
        }
    }
    public void loadAndSetDefaultProperty() throws IOException {
        if (new File("records/jamming.properties").isFile()) {
            FileInputStream propertiesInput = new FileInputStream("records/jamming.properties");
            this.load(propertiesInput);
            propertiesInput.close();
        }

        this.setDefaultProperty("maxChatTimeOutHour", "2");
        this.setDefaultProperty("minResponsesChat", "2");
        this.setDefaultProperty("maxResponsesChat", "20");

        FileOutputStream propertiesOutput = new FileOutputStream("records/jamming.properties");
        this.store(propertiesOutput, "Jamming Properties");
    }

}