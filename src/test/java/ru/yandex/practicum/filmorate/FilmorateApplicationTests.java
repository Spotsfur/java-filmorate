package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class})
class FilmorateApplicationTests {

	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;

	@Test
	void contextLoads() {
	}

	//ТЕСТЫ СЛОЯ USER

	@Test
	public void testCreateAndFindUserById() {
		User newUser = User.builder()
				.email("first@user.com")
				.login("UserLogin")
				.name("UserName")
				.birthday(LocalDate.of(1995, 5, 10))
				.build();

		User savedUser = userStorage.create(newUser);
		Optional<User> userOptional = userStorage.findOne(savedUser.getId());

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user -> {
					assertThat(user).hasFieldOrPropertyWithValue("id", savedUser.getId());
					assertThat(user).hasFieldOrPropertyWithValue("email", "first@user.com");
					assertThat(user).hasFieldOrPropertyWithValue("login", "UserLogin");
					assertThat(user).hasFieldOrPropertyWithValue("name", "UserName");
				});
	}

	@Test
	public void testNullValueOfUserNameIsLogin() {
		//Пустое имя пользователя приравнивается к логину
		User userWithoutName = User.builder()
				.email("new@user.com")
				.login("OnlyLogin")
				.birthday(LocalDate.of(2000, 1, 1))
				.build();

		User savedUser = userStorage.create(userWithoutName);

		assertThat(savedUser.getName()).isEqualTo("OnlyLogin");
	}

	@Test
	public void testUpdateUser() {
		User user = userStorage.create(User.builder()
				.email("old@email.com")
				.login("login")
				.name("Old Name")
				.birthday(LocalDate.of(1990, 1, 1))
				.build());

		user.setEmail("new@email.com");
		user.setName("New Name");
		userStorage.update(user);

		Optional<User> userOptional = userStorage.findOne(user.getId());

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(u -> {
					assertThat(u).hasFieldOrPropertyWithValue("email", "new@email.com");
					assertThat(u).hasFieldOrPropertyWithValue("name", "New Name");
				});
	}

	@Test
	public void testFindAllUsers() {
		User user1 = userStorage.create(User.builder().email("1@e.com").login("l1").birthday(LocalDate.now()).build());
		User user2 = userStorage.create(User.builder().email("2@e.com").login("l2").birthday(LocalDate.now()).build());

		Collection<User> users = userStorage.findAll();

		assertThat(users)
				.isNotEmpty()
				.hasSize(2)
				.extracting(User::getLogin)
				.containsExactlyInAnyOrder("l1", "l2");
	}

	//ТЕСТЫ СЛОЯ FILM

	@Test
	public void testCreateAndFindFilmById() {
		Film newFilm = Film.builder()
				.name("Первый фильм")
				.description("Описание первого фильма")
				.releaseDate(LocalDate.of(2020, 1, 1))
				.duration(100)
				.mpa(Mpa.builder().id(1).build()) //В базе из data.sql id=1 это рейтинг 'G'
				.build();

		Film savedFilm = filmStorage.create(newFilm);
		Optional<Film> filmOptional = filmStorage.findOne(savedFilm.getId());

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film -> {
					assertThat(film).hasFieldOrPropertyWithValue("id", savedFilm.getId());
					assertThat(film).hasFieldOrPropertyWithValue("name", "Первый фильм");
					assertThat(film).hasFieldOrPropertyWithValue("duration", 100);
					assertThat(film.getMpa().getId()).isEqualTo(1);
					assertThat(film.getMpa().getName()).isEqualTo("G");
				});
	}

	@Test
	public void testUpdateFilm() {
		Film film = filmStorage.create(Film.builder()
				.name("Old Name")
				.description("Desc")
				.releaseDate(LocalDate.of(2010, 5, 5))
				.duration(90)
				.mpa(Mpa.builder().id(1).build())
				.build());

		film.setName("Film Updated");
		film.setDuration(190);
		film.setMpa(Mpa.builder().id(2).build());
		filmStorage.update(film);

		Optional<Film> filmOptional = filmStorage.findOne(film.getId());

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(f -> {
					assertThat(f).hasFieldOrPropertyWithValue("name", "Film Updated");
					assertThat(f).hasFieldOrPropertyWithValue("duration", 190);
					assertThat(f.getMpa().getId()).isEqualTo(2);
					assertThat(f.getMpa().getName()).isEqualTo("PG");
				});
	}

	@Test
	public void testFindAllFilms() {
		Film f1 = filmStorage.create(Film.builder().name("Film 1").releaseDate(LocalDate.now()).duration(60).mpa(Mpa.builder().id(1).build()).build());
		Film f2 = filmStorage.create(Film.builder().name("Film 2").releaseDate(LocalDate.now()).duration(120).mpa(Mpa.builder().id(1).build()).build());

		Collection<Film> films = filmStorage.findAll();

		assertThat(films)
				.isNotEmpty()
				.hasSize(2)
				.extracting(Film::getName)
				.containsExactlyInAnyOrder("Film 1", "Film 2");
	}

	//ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ВАЛИДАЦИИ

	@Test
	public void testUpdateUserNotFoundException() {
		//Попытка изменить пользователя по несуществующему id
		User nonExistentUser = User.builder()
				.id(999L)
				.email("test@email.com")
				.login("login")
				.birthday(LocalDate.of(2000, 1, 1))
				.build();

		org.junit.jupiter.api.Assertions.assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class, () -> {
			userStorage.update(nonExistentUser);
		});
	}

	@Test
	public void testUpdateUserWithNullFieldsPreservesOldData() {
		//Передача null-полей в update() оставляет старые данные из базы
		User originalUser = userStorage.create(User.builder()
				.email("original@email.com")
				.login("originalLogin")
				.name("Original Name")
				.birthday(LocalDate.of(1990, 5, 5))
				.build());

		User updateRequest = User.builder()
				.id(originalUser.getId())
				.email("updated@email.com")
				.login(null)
				.name(null)
				.birthday(null)
				.build();

		User resultUser = userStorage.update(updateRequest);

		//Проверяем, что email изменился, а login, name и birthday подтянулись из старой записи
		assertThat(resultUser).hasFieldOrPropertyWithValue("email", "updated@email.com");
		assertThat(resultUser).hasFieldOrPropertyWithValue("login", "originalLogin");
		assertThat(resultUser).hasFieldOrPropertyWithValue("name", "Original Name");
		assertThat(resultUser).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1990, 5, 5));
	}

	@Test
	public void testUpdateUserNameIsEmptyPreservesOldName() {
		//Передача null в поле name при обновлении сохраняет старое имя, даже если оно совпадало со старым логином, а логин при этом меняется
		User originalUser = userStorage.create(User.builder()
				.email("user@email.com")
				.login("oldLogin")
				.name("oldLogin")
				.birthday(LocalDate.of(1990, 5, 5))
				.build());

		User updateRequest = User.builder()
				.id(originalUser.getId())
				.login("newLogin")
				.name(null)
				.build();

		User resultUser = userStorage.update(updateRequest);

		assertThat(resultUser).hasFieldOrPropertyWithValue("login", "newLogin");
		assertThat(resultUser).hasFieldOrPropertyWithValue("name", "oldLogin");
	}

	@Test
	public void testUpdateUserNameIsBlankSubstitutesLogin() {
		//Передача пустого имени принудительно подставляет актуальный логин пользователя
		User originalUser = userStorage.create(User.builder()
				.email("user@email.com")
				.login("oldLogin")
				.name("Some Name")
				.birthday(LocalDate.of(1990, 5, 5))
				.build());

		User updateRequest = User.builder()
				.id(originalUser.getId())
				.login("newLogin")
				.name(" ")
				.build();

		User resultUser = userStorage.update(updateRequest);

		assertThat(resultUser).hasFieldOrPropertyWithValue("name", "newLogin");
	}

}
