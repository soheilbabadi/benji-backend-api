package social.benji.benji_backend_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BenjiBackendApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BenjiBackendApiApplication.class, args);
	}

}
