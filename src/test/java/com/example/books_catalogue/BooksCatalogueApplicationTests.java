package com.example.books_catalogue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BooksCatalogueApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

	@Autowired
	TestRestTemplate testRestTemplate;

	@Autowired
	BookRepository bookRepository;
	
	@BeforeEach
	void addTestBookData() {
		Book book1 = new Book("Book 1", "Author 1", "Genre 1", "329", 100, "http://book1.com");
		Book book2 = new Book("Book 2", "Author 2", "Genre 2", "348-40", 200, "http://book2.com");
		Book book3 = new Book("Book 3", "Author 3", "Genre 3", "34843", 300, "http://book3.com");

		bookRepository.save(book1);
		bookRepository.save(book2);
		bookRepository.save(book3);
	}

	@AfterEach
	void clearTestBookData() {
		bookRepository.deleteAll();
	}

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
	void shouldReturnPageOfBooksWithDefaultValuesOfQueryParam() {
		ResponseEntity<String> response = testRestTemplate.getForEntity("/books", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray titles = documentContext.read("$..title");
		assertThat(titles).containsExactly("Book 1", "Book 2", "Book 3"); 
	}

	@Test
	void shouldReturnPageOfBooksBasedOnQueryParam() {
		ResponseEntity<String> response = testRestTemplate.getForEntity("/books?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);

		String title = documentContext.read("$[0].title");
		assertThat(title).isEqualTo("Book 1");
	}

	@Test
	void shouldReturnDescSortedPageOfBookBasedOnQueryParam() {
		ResponseEntity<String> response = testRestTemplate.getForEntity("/books?page=0&size=2&sort=totalPage,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(2);

		JSONArray totalPages = documentContext.read("$..totalPage");
		assertThat(totalPages).containsExactly(300, 200);
	}

	@Test
	void shouldReturnABook() {
		ResponseEntity<Book> getResponse = testRestTemplate.getForEntity("/books/329", Book.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		Book book = getResponse.getBody();
		assertThat(book).isNotNull();
		assertThat(book.getTitle()).isEqualTo("Book 1");
		assertThat(book.getAuthor()).isEqualTo("Author 1");
		assertThat(book.getGenre()).isEqualTo("Genre 1");
		assertThat(book.getISBN()).isEqualTo("329");
		assertThat(book.getTotalPage()).isEqualTo(100);
		assertThat(book.getCoverImageURL()).isEqualTo("http://book1.com");
	}

	@Test
	void shouldNotReturnABook() {
		ResponseEntity<String> getResponse = testRestTemplate.getForEntity("/books/not-isbn", String.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldUpdateExistingBook() {
		ResponseEntity<Book> firstGetResponse = testRestTemplate.getForEntity("/books/329", Book.class);
		assertThat(firstGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		Book bookToUpdate = firstGetResponse.getBody();
		assertThat(bookToUpdate).isNotNull();
		String updatedTitle = "Book 329";
		bookToUpdate.setTitle(updatedTitle);
		HttpEntity<Book> updatedBook = new HttpEntity<>(bookToUpdate);

		ResponseEntity<String> putResponse = testRestTemplate.exchange("/books/329", HttpMethod.PUT, updatedBook, String.class);
		assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> secondGetResponse = testRestTemplate.getForEntity("/books/329", String.class);
		assertThat(secondGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(secondGetResponse.getBody());
		String ISBN =  documentContext.read("$.isbn");
		String title = documentContext.read("$.title");

		assertThat(ISBN).isEqualTo("329");
		assertThat(title).isEqualTo("Book 329");
	}

	@Test 
	void shouldNotUpdateNonExistingBook() {
		Book failedToUpdateBook = new Book(null, null, null, null, null, null);

		HttpEntity<Book> bookRequest = new HttpEntity<>(failedToUpdateBook);
		
		ResponseEntity<String> putResponse = testRestTemplate.exchange("/books/not-isbn", HttpMethod.PUT, bookRequest,String.class);
		assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldDeleteExistingBook() {
		ResponseEntity<Void> deleteResponse = testRestTemplate.exchange("/books/329", HttpMethod.DELETE, null, Void.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}
}
