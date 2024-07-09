package com.example.books_catalogue;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/books")
public class BookController {
    
    @GetMapping("")
    public String findAllBooks() {
        return "Hello World";
    }
    
}
