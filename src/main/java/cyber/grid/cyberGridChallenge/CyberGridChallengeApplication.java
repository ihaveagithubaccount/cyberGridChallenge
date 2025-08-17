package cyber.grid.cyberGridChallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CyberGridChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CyberGridChallengeApplication.class, args);
	}

}
