package com.example.books_catalogue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BooksCatalogueApplicationTests {


	@Autowired
	TestRestTemplate testRestTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldAddANewBook() {
		Book newBook = new Book("The Psychology of Money: Timeless Lessons on Wealth, Greed, and Happiness", "Morgan Housel", "Non-Fiction", "978-0857197689", 266, "http://what.ve");

		ResponseEntity<Void> addResponse = testRestTemplate.postForEntity("/books", newBook, Void.class);

		assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewBook = addResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = testRestTemplate.getForEntity(locationOfNewBook, String.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		String title = documentContext.read("$.title");
		String author = documentContext.read("$.author");
		String genre = documentContext.read("$.genre");
		String ISBN = documentContext.read("$.ISBN");
		int pageCount = documentContext.read("$.pageCount");
		String coverImageURL = documentContext.read("$.coverImageURL");

		assertThat(title).isEqualTo("The Psychology of Money: Timeless Lessons on Wealth, Greed, and Happiness");
		assertThat(author).isEqualTo("Morgan Housel");
		assertThat(genre).isEqualTo("Non-Fiction");
		assertThat(ISBN).isEqualTo("978-0857197689");
		assertThat(pageCount).isEqualTo(266);
		assertThat(coverImageURL).isEqualTo("http://what.ve");
	}

}
