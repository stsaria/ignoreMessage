package si.f5.stsaria.ignoreMessage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Files;

@SpringBootApplication
public class IgnoreMessageApplication {
	public final static MyProperties properties = new MyProperties();
	public static void main(String[] args) {
		File recordsDirFile = new File("records");
		try {
			if (recordsDirFile.isFile()) {
				Files.delete(recordsDirFile.toPath());
			}
			if (!recordsDirFile.isDirectory()) {
				Files.createDirectory(recordsDirFile.toPath());
			}
			properties.loadAndSetDefaultProperty();
		} catch (Exception ignore) {
			return;
		}
		SpringApplication.run(IgnoreMessageApplication.class, args);
	}

}
