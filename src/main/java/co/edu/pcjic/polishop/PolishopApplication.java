package co.edu.pcjic.polishop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class  PolishopApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolishopApplication.class, args);
	}

}