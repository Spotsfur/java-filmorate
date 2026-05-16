package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

//Переделал тестовый класс под SpringBootTest обратно со своими тестами, но под базу данных
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {

	private final FilmController filmController;
	private final UserController userController;

	@Test //Валидный фильм
	void isTheValidFilm() {
		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Первый фильм")
				.description("Описание первого фильма")
				.releaseDate(LocalDate.now())
				.duration(100)
				.mpa(mpa)
				.build();

		filmController.create(film);
	}

	@Test //Пустой экземпляр фильма
	void theFilmHasNoData() {
		Film film = Film.builder()
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Название фильма не может быть пустым", exception.getMessage());
	}

	@Test //Фильм без названия
	void noNameFilm() {
		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name(" ")
				.description("Описание фильма")
				.releaseDate(LocalDate.now())
				.duration(100)
				.mpa(mpa)
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Название фильма не может быть пустым", exception.getMessage());
	}

	@Test //Фильм с длиной описания 200
	void descriptionLengthIs200() {
		String description = "0123456789".repeat(20);

		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Название фильма")
				.description(description)
				.releaseDate(LocalDate.now())
				.duration(100)
				.mpa(mpa)
				.build();

		filmController.create(film);
	}

	@Test //Фильм с длиной описания 201
	void descriptionLengthIs201() {
		String description = "0123456789".repeat(20) + "0";

		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Название фильма")
				.description(description)
				.releaseDate(LocalDate.now())
				.duration(100)
				.mpa(mpa)
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
	}

	@Test //Фильм с датой релиза 28 декабря 1895 года
	void releaseDateIs28December1895() {
		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Название фильма")
				.description("Описание фильма")
				.releaseDate(LocalDate.of(1895, 12, 28))
				.duration(100)
				.mpa(mpa)
				.build();

		filmController.create(film);
	}

	@Test //Фильм с датой релиза 27 декабря 1895 года
	void releaseDateIs27December1895() {
		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Название фильма")
				.description("Описание фильма")
				.releaseDate(LocalDate.of(1895, 12, 27))
				.duration(100)
				.mpa(mpa)
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
	}

	@Test //Продолжительность фильма - отрицательное число
	void durationIsMinus1() {
		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Название фильма")
				.description("Описание фильма")
				.releaseDate(LocalDate.now())
				.duration(-1)
				.mpa(mpa)
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> filmController.create(film));
		assertEquals("Продолжительность фильма должна быть положительной", exception.getMessage());
	}

	@Test //Попытка изменить фильм без передачи id
	void filmIdIsNullInPutRequest() {
		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(film));
		assertEquals("id фильма должен быть указан", exception.getMessage());
	}

	@Test //Попытка изменить фильм по несуществующему id
	void filmIdIs20InPutRequest() {
		Film film = Film.builder()
				.id(20L)
				.build();

		Exception exception = assertThrows(NotFoundException.class, () -> filmController.update(film));
		assertEquals("Фильм с id " + film.getId() + " не найден", exception.getMessage());
	}

	@Test //Попытка изменить название фильма на пустое
	void filmNameIsBlankInPutRequest() {
		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Первый фильм")
				.description("Описание первого фильма")
				.releaseDate(LocalDate.now())
				.duration(100)
				.mpa(mpa)
				.build();

		filmController.create(film);

		final Film newFilm = Film.builder()
				.id(1L)
				.name(" ")
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(newFilm));
		assertEquals("Название фильма не может быть пустым", exception.getMessage());
	}

	@Test //Попытка изменить описание фильма на длину 201
	void filmDescriptionLengthIs201InPutRequest() {
		String description = "0123456789".repeat(20) + "0";

		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Первый фильм")
				.description("Описание первого фильма")
				.releaseDate(LocalDate.now())
				.duration(100)
				.mpa(mpa)
				.build();

		filmController.create(film);

		final Film newFilm = Film.builder()
				.id(1L)
				.description(description)
				.build();

		film.setDescription(description);
		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(newFilm));
		assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
	}

	@Test //Попытка изменить дату выхода фильма на 27 декабря 1895 года
	void filmReleaseDateIs27December1895InPutRequest() {
		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Первый фильм")
				.description("Описание первого фильма")
				.releaseDate(LocalDate.now())
				.duration(100)
				.mpa(mpa)
				.build();

		filmController.create(film);

		final Film newFilm = Film.builder()
				.id(1L)
				.releaseDate(LocalDate.of(1895, 12, 27))
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(newFilm));
		assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
	}

	@Test //Попытка изменить длительность фильма на -1
	void filmDurationIsMinus1InPutRequest() {
		Mpa mpa = Mpa.builder()
				.id(1)
				.name("G")
				.build();

		Film film = Film.builder()
				.name("Первый фильм")
				.description("Описание первого фильма")
				.releaseDate(LocalDate.now())
				.duration(100)
				.mpa(mpa)
				.build();

		filmController.create(film);

		final Film newFilm = Film.builder()
				.id(1L)
				.duration(-1)
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> filmController.update(newFilm));
		assertEquals("Продолжительность фильма должна быть положительной", exception.getMessage());
	}

	@Test //Валидный пользователь
	void isTheValidUser() {
		User user = User.builder()
				.email("первый@пользователь")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		userController.create(user);
	}

	@Test //Пустой экземпляр пользователя
	void theUserHasNoData() {
		final User user = User.builder().build();
		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
	}

	@Test //Пользователь с пустой почтой
	void iheUserEmailIsEmpty() {
		User user = User.builder()
				.email(" ")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
	}

	@Test //Пользователь с неправильной почтой
	void iheUserEmailHasNoAtSign() {
		User user = User.builder()
				.email("КакаяТоНеправильнаяПочта")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
	}

	@Test //Пользователь с нулл логином
	void theUserLoginIsNull() {
		User user = User.builder()
				.email("новый@пользователь")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
	}

	@Test //Пользователь с пустым логином
	void theUserLoginIsBlank() {
		User user = User.builder()
				.email("новый@пользователь")
				.login(" ")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();user.setBirthday(LocalDate.now());

		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
	}

	@Test //Пустое имя пользователя приравнивается логину
	void nullValueOfUserNameIsLogin() {
		User user = User.builder()
				.email("новый@пользователь")
				.login("Логин")
				.birthday(LocalDate.now())
				.build();

		final User newUser = userController.create(user);
		assertEquals(newUser.getLogin(), newUser.getName());
	}

	@Test //Дата рождения в будущем
	void birthdayCantBeInFuture() {
		User user = User.builder()
				.email("новый@пользователь")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now().plusDays(1))
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> userController.create(user));
		assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
	}

	@Test //Попытка изменить пользователя без передачи id
	void userIdIsNullInPutRequest() {
		User user = User.builder()
				.email("новый@пользователь")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		userController.create(user);

		User newUser = User.builder()
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> userController.update(newUser));
		assertEquals("id должен быть указан", exception.getMessage());
	}

	@Test //Попытка изменить пользователя по несуществующему id
	void userIdIs20InPutRequest() {
		User user = User.builder()
				.email("новый@пользователь")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		userController.create(user);

		User newUser = User.builder()
				.id(20L)
				.build();

		Exception exception = assertThrows(NotFoundException.class, () -> userController.update(newUser));
		assertEquals("Пользователь с id " + newUser.getId() + " не найден", exception.getMessage());
	}

	@Test //Попытка изменить почту на пустую
	void userEmailIsEmptyInPutRequest() {
		User user = User.builder()
				.email("новый@пользователь")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		userController.create(user);

		User newUser = User.builder()
				.id(1L)
				.email(" ")
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> userController.update(newUser));
		assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
	}

	@Test //Попытка изменить логин на пустой
	void userLoginIsEmptyInPutRequest() {
		User user = User.builder()
				.email("новый@пользователь")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		userController.create(user);

		User newUser = User.builder()
				.id(1L)
				.login(" ")
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> userController.update(newUser));
		assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
	}

	@Test //Передача пустого имени подставляет логин в имя
	void userNameIsEmptyInPutRequest() {
		User user = User.builder()
				.email("новый@пользователь")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		userController.create(user);

		User newUser = User.builder()
				.id(1L)
				.login("Логин")
				.name(" ")
				.build();

		userController.update(newUser);

		assertEquals(newUser.getLogin(), newUser.getName());
	}

	@Test //Передача пустого имени без логина подставляет старый логин в имя
	void userLoginIsNullAndUserNameIsEmptyInPutRequest() {
		User user = User.builder()
				.email("новый@пользователь")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		userController.create(user);

		User newUser = User.builder()
				.id(1L)
				.name(" ")
				.build();

		userController.update(newUser);

		assertEquals(newUser.getLogin(), newUser.getName());
	}

	@Test //Попытка установить день рождения в будущем
	void userBirthdayInFutureInPutRequest() {
		User user = User.builder()
				.email("новый@пользователь")
				.login("Логин")
				.name("Имя")
				.birthday(LocalDate.now())
				.build();

		userController.create(user);

		User newUser = User.builder()
				.id(1L)
				.birthday(LocalDate.now().plusDays(1))
				.build();

		Exception exception = assertThrows(ValidationException.class, () -> userController.update(newUser));
		assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
	}
}