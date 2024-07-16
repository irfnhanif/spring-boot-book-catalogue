package com.example.books_catalogue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BookRepository extends CrudRepository<Book, Integer>, PagingAndSortingRepository<Book, Integer> {
    Page<Book> findAll(Pageable pageable);
    Book findByISBN(String ISBN);
    boolean existsByISBN(String ISBN);
    void deleteByISBN(String ISBN);
}
