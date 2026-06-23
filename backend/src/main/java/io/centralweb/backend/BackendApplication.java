package io.centralweb.backend;

import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.model.User;
import io.centralweb.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByEmail("admin@centralweb.io").isEmpty()) {
				User admin = new User();
				admin.setEmail("admin@centralweb.io");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(UserRole.ADMIN);

				userRepository.save(admin);
				System.out.println("Usuário ADMIN criado com sucesso!");
			}
		};
	}
}
