package si.f5.stsaria.ignoreMessage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IgnoreMessageApplication {
	public final static MyProperties properties = new MyProperties();
	public static void main(String[] args) {
		SpringApplication.run(IgnoreMessageApplication.class, args);
	}

}
