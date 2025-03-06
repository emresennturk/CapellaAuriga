package com.example.oauth2_mysql_demo;

import com.example.oauth2_mysql_demo.entity.User;
import com.example.oauth2_mysql_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Oauth2MysqlDemoApplication implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(Oauth2MysqlDemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Admin kullanıcısını oluştur
		if (userRepository.findByUsername("admin").isEmpty()) {
			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode("admin123"));
			admin.setRoles("ADMIN");
			userRepository.save(admin);
		}

		// 7 arkadaş için kullanıcılar oluştur
		String[] usernames = {"taha", "arda", "semih", "iso", "teo", "serdar", "emre"};
		for (String username : usernames) {
			if (userRepository.findByUsername(username).isEmpty()) {
				User user = new User();
				user.setUsername(username);
				user.setPassword(passwordEncoder.encode(username + "123")); // Ör: ali123
				user.setRoles("USER");
				userRepository.save(user);
			}
		}
	}
}
