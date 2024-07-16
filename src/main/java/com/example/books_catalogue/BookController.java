package com.example.books_catalogue;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.transaction.Transactional;

import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/books")
public class BookController {

    private BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    @GetMapping()
    public ResponseEntity<List<Book>> findAllBooks(Pageable pageable) {
        Page<Book> page = bookRepository.findAll(PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            pageable.getSortOr(Sort.by(Sort.Direction.ASC, "ISBN"))
        ));

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{bookISBN}")
    public ResponseEntity<Book> findOneBookById(@PathVariable String bookISBN) {
        Book book = bookRepository.findByISBN(bookISBN);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(book);
    }
    
    @PostMapping()
    public ResponseEntity<Void> addNewBook(@RequestBody Book newBookRequest, UriComponentsBuilder uriComponentsBuilder) {
        Book bookToSave = new Book(newBookRequest.getTitle(), newBookRequest.getAuthor(), newBookRequest.getGenre(), newBookRequest.getISBN(), newBookRequest.getTotalPage(), newBookRequest.getCoverImageURL());
        Book savedBook = bookRepository.save(bookToSave);
        URI locationOfNewBook = uriComponentsBuilder
                                .path("/books/{ISBN}")
                                .buildAndExpand(savedBook.getISBN())
                                .toUri();

        return ResponseEntity.created(locationOfNewBook).build();
    }
    
    @PutMapping("/{bookISBN}")
    public ResponseEntity<Void> updateExistingBook(@PathVariable String bookISBN, @RequestBody Book bookRequest) {
        Book book = bookRepository.findByISBN(bookISBN);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        bookRepository.save(bookRequest);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bookISBN}")
    @Transactional
    public ResponseEntity<Void> deleteExistingBook(@PathVariable String bookISBN) {
        if (bookRepository.existsByISBN(bookISBN) == false) {
            return ResponseEntity.notFound().build();
        }
        bookRepository.deleteByISBN(bookISBN);
        
        return ResponseEntity.noContent().build();
    }
}
