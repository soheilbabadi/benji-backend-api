package social.benji.benji_backend_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class BenjiBackendApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BenjiBackendApiApplication.class, args);
	}

}
