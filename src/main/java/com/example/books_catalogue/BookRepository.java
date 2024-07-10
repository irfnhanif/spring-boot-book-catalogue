package com.example.books_catalogue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BookRepository extends CrudRepository<Book, Integer>, PagingAndSortingRepository<Book, Integer> {
    Page<Book> findAll(PageRequest pageRequest);
}
