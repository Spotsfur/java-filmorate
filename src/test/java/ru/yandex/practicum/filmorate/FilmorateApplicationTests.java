package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmorateApplicationTests {

	static FilmController filmController = new FilmController();
	static UserController userController = new UserController();

	@Test
	void contextLoads() {
	}

	@Test //Валидный фильм
	@Order(1) //Этот тест будет первый, с этими данными будем работать при изменении
	void isTheValidFilm() {
		final Film film = new Film();
		film.setName("Первый фильм");
		film.setDescription("Описание первого фильма");
		film.setReleaseDate(LocalDate.now());
		film.setDuration(100);
		filmController.create(film);
	}

	@Test //Пустой экземпляр фильма
	void theFilmHasNoData() {
		final Film film = new Film();
		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Название не может быть пустым", exception.getMessage());
	}

	@Test //Фильм без названия
	void noNameFilm() {
		final Film film = new Film();
		film.setName(" ");
		film.setDescription("Описание фильма");
		film.setReleaseDate(LocalDate.now());
		film.setDuration(100);
		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Название не может быть пустым", exception.getMessage());
	}

	@Test //Фильм с длиной описания 200
	void descriptionLengthIs200() {
        String description = "0123456789".repeat(20);
		final Film film = new Film();
		film.setName("Название фильма");
		film.setDescription(description);
		film.setReleaseDate(LocalDate.now());
		film.setDuration(100);
		filmController.create(film);
	}

	@Test //Фильм с длиной описания 201
	void descriptionLengthIs201() {
        String description = "0123456789".repeat(20) + "0";
		final Film film = new Film();
		film.setName("Название фильма");
		film.setDescription(description);
		film.setReleaseDate(LocalDate.now());
		film.setDuration(100);
		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
	}

	@Test //Фильм с датой релиза 28 декабря 1895 года
	void releaseDateIs28December1895() {
		final Film film = new Film();
		film.setName("Название фильма");
		film.setDescription("Описание фильма");
		film.setReleaseDate(LocalDate.of(1895, 12, 28));
		film.setDuration(100);
		filmController.create(film);
	}

	@Test //Фильм с датой релиза 27 декабря 1895 года
	void releaseDateIs27December1895() {
		final Film film = new Film();
		film.setName("Название фильма");
		film.setDescription("Описание фильма");
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		film.setDuration(100);
		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
	}

	@Test //Продолжительность фильма - отрицательное число
	void durationIsMinus1() {
		final Film film = new Film();
		film.setName("Название фильма");
		film.setDescription("Описание фильма");
		film.setReleaseDate(LocalDate.now());
		film.setDuration(-1);
		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
	}

	@Test //Попытка изменить фильм без передачи id
	void filmIdIsNullInPutRequest() {
		final Film film = new Film();
		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(film));
		assertEquals("id должен быть указан", exception.getMessage());
	}

	@Test //Попытка изменить фильм по несуществующему id
	void filmIdIs20InPutRequest() {
		final Film film = new Film();
		film.setId(20L);
		Exception exception = assertThrows(NotFoundException.class, () -> filmController.update(film));
		assertEquals("Фильм с id " + film.getId() + " не найден", exception.getMessage());
	}

	@Test //Попытка изменить название фильма на пустое
	void filmNameIsBlankInPutRequest() {
		final Film film = new Film();
		film.setId(1L);
		film.setName(" ");
		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(film));
		assertEquals("Название не может быть пустым", exception.getMessage());
	}

	@Test //Попытка изменить описание фильма на длину 201
	void filmDescriptionLengthIs201InPutRequest() {
		final Film film = new Film();
		film.setId(1L);
		String description = "0123456789".repeat(20) + "0";
		film.setDescription(description);
		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(film));
		assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
	}

	@Test //Попытка изменить дату выхода фильма на 27 декабря 1895 года
	void filmReleaseDateIs27December1895InPutRequest() {
		final Film film = new Film();
		film.setId(1L);
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(film));
		assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
	}

	@Test //Попытка изменить длительность фильма на -1
	void filmDurationIsMinus1InPutRequest() {
		final Film film = new Film();
		film.setId(1L);
		film.setDuration(-1);
		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(film));
		assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
	}

	@Test //Валидный фильм
	@Order(2) //Этот тест будет второй, с этими данными будем работать при изменении
	void isTheValidUser() {
		final User user = new User();
		user.setEmail("первый@пользователь");
		user.setLogin("Логин");
		user.setName("Имя");
		user.setBirthday(LocalDate.now());
		userController.create(user);
	}

	@Test //Пустой экземпляр пользователя
	void theUserHasNoData() {
		final User user = new User();
		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
	}

	@Test //Пользователь с пустой почтой
	void iheUserEmailIsEmpty() {
		final User user = new User();
		user.setEmail(" ");
		user.setLogin("Логин");
		user.setName("Имя");
		user.setBirthday(LocalDate.now());
		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
	}
	@Test //Пользователь с неправильной почтой
	void iheUserEmailHasNoAtSign() {
		final User user = new User();
		user.setEmail("КакаяТоНеправильнаяПочта");
		user.setLogin("Логин");
		user.setName("Имя");
		user.setBirthday(LocalDate.now());
		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
	}

	@Test //Пользователь с нулл логином
	void theUserLoginIsNull() {
		final User user = new User();
		user.setEmail("новый@пользователь");
		user.setName("Имя");
		user.setBirthday(LocalDate.now());
		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
	}

	@Test //Пользователь с пустым логином
	void theUserLoginIsBlank() {
		final User user = new User();
		user.setEmail("новый@пользователь");
		user.setLogin(" ");
		user.setName("Имя");
		user.setBirthday(LocalDate.now());
		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
	}

	@Test //Пустое имя пользователя приравнивается логину
	void nullValueOfUserNameIsLogin() {
		final User user = new User();
		user.setEmail("новый@пользователь");
		user.setLogin("Логин");
		user.setBirthday(LocalDate.now());
		final User newUser = userController.create(user);
		assertEquals(newUser.getLogin(), newUser.getName());
	}

	@Test //Дата рождения в будущем
	void birthdayCantBeInFuture() {
		final User user = new User();
		user.setEmail("новый@пользователь");
		user.setLogin("Логин");
		user.setName("Имя");
		user.setBirthday(LocalDate.now().plusDays(1));
		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
	}

	@Test //Попытка изменить пользователя без передачи id
	void userIdIsNullInPutRequest() {
		final User user = new User();
		Exception exception = assertThrows(ValidationException.class, () -> userController.update(user));
		assertEquals("id должен быть указан", exception.getMessage());
	}

	@Test //Попытка изменить пользователя по несуществующему id
	void userIdIs20InPutRequest() {
		final User user = new User();
		user.setId(20L);
		Exception exception = assertThrows(NotFoundException.class, () -> userController.update(user));
		assertEquals("Пользователь с id " + user.getId() + " не найден", exception.getMessage());
	}

	@Test //Попытка изменить почту на пустую
	void userEmailIsEmptyInPutRequest() {
		final User user = new User();
		user.setId(1L);
		user.setEmail(" ");
		Exception exception = assertThrows(ValidationException.class, () -> userController.update(user));
		assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
	}

	@Test //Попытка изменить логин на пустой
	void userLoginIsEmptyInPutRequest() {
		final User user = new User();
		user.setId(1L);
		user.setLogin(" ");
		Exception exception = assertThrows(ValidationException.class, () -> userController.update(user));
		assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
	}

	@Test //Передача пустого имени подставляет логин в имя
	void userNameIsEmptyInPutRequest() {
		final User user = new User();
		user.setId(1L);
		user.setLogin("Логин");
		user.setName(" ");
		final User newUser = userController.update(user);
		assertEquals(newUser.getLogin(), newUser.getName());
	}

	@Test //Передача пустого имени без логина подставляет старый логин в имя
	void userLoginIsNullAndUserNameIsEmptyInPutRequest() {
		final User user = new User();
		user.setId(1L);
		user.setName(" ");
		final User newUser = userController.update(user);
		assertEquals(newUser.getLogin(), newUser.getName());
	}

	@Test //Попытка установить день рождения в будущем
	void userBirthdayInFutureInPutRequest() {
		final User user = new User();
		user.setId(1L);
		user.setBirthday(LocalDate.now().plusDays(1));
		Exception exception = assertThrows(ValidationException.class, () -> userController.update(user));
		assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
	}
}