package ru.yandex.practicum.filmorate;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmorateApplication {
	private final static Logger log = LoggerFactory.getLogger(FilmorateApplication.class);

	public static void main(String[] args) {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
				.setLevel(Level.INFO);
		log.info("Запускаю SpringApplication");
		SpringApplication.run(FilmorateApplication.class, args);
	}

}
