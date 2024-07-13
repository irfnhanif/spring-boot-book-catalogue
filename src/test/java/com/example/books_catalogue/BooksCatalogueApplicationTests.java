package com.example.books_catalogue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BooksCatalogueApplicationTests {

	@Autowired
	TestRestTemplate testRestTemplate;

	@Test
	void shouldAddANewBook() {
		Book newBook = new Book("The Psychology of Money: Timeless Lessons on Wealth, Greed, and Happiness", "Morgan Housel", "Non-Fiction", "978-0857197689", 266, "http://what.ve");

		ResponseEntity<Void> addResponse = testRestTemplate.postForEntity("/books", newBook, Void.class);

		assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewBook = addResponse.getHeaders().getLocation();
		ResponseEntity<Book> getResponse = testRestTemplate.getForEntity(locationOfNewBook, Book.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		Book retrievedBook = getResponse.getBody();
		assertThat(retrievedBook).isNotNull();
		assertThat(retrievedBook.getTitle()).isEqualTo("The Psychology of Money: Timeless Lessons on Wealth, Greed, and Happiness");
		assertThat(retrievedBook.getAuthor()).isEqualTo("Morgan Housel");
		assertThat(retrievedBook.getGenre()).isEqualTo("Non-Fiction");
		assertThat(retrievedBook.getISBN()).isEqualTo("978-0857197689");
		assertThat(retrievedBook.getTotalPage()).isEqualTo(266);
		assertThat(retrievedBook.getCoverImageURL()).isEqualTo("http://what.ve");
	}

	@Test
	void shouldNotReturnABook() {
		ResponseEntity<String> getResponse = testRestTemplate.getForEntity("/books/99999", String.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
