package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper) {
       this.bookService = bookService;
        this.booksMapper = booksMapper;
    }
    
//liste des livres existants 
    @GetMapping("/books")
    public Collection<BookDTO> books(@RequestParam(value = "q", required = false) String query) {
        Collection<Book> authors;
        if (query == null) {
            authors = bookService.list();
        } else {
            authors = bookService.findByTitle(query);
        }
        return authors.stream()
                .map(booksMapper::entityToDTO)
                .toList();

    }

    //trouver un livre par son id
    @GetMapping("/books/{id}")
    public BookDTO book(@PathVariable Long id) {
        Book book;
        try {
            book = bookService.get(id);
            return booksMapper.entityToDTO(book);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public BookDTO newBook(Long authorId, BookDTO book) {
        return null;
    }

    public BookDTO updateBook(Long authorId, BookDTO book) {
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est mauvaise
        return null;
    }

    public void deleteBook(Long id) {

    }

    public void addAuthor(Long authorId, AuthorDTO author) {

    }
}
