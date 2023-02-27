package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.data.domain.Book.Language;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.service.AuthorService;

import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;
    //import des services et mapper d'auteur
    private final AuthorService authorService;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper, AuthorService authorService) {
        this.bookService = bookService;
        this.booksMapper = booksMapper;
        this.authorService = authorService;
    }

    // liste des livres existants
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

    // trouver un livre par son id
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

    // creer un nouveau livre
    @PostMapping("/authors/{authorId}/books")
    public BookDTO newBook(@PathVariable Long authorId, @RequestBody BookDTO book) {

        Book newBook = booksMapper.dtoToEntity(book);

        try {
            Author author = authorService.get(authorId);
            newBook.addAuthor(author);

        } catch (EntityNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // créer un nouvel auteur
        newBook.setIsbn(book.isbn());
        newBook.setPublisher(book.publisher());
        newBook.setLanguage(Language.valueOf(book.language().toUpperCase()));
        newBook.setYear(book.year());

        try {
            bookService.save(authorId, newBook);
        } catch (EntityNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return booksMapper.entityToDTO(newBook);
    }


    // mettre à jour un livre
    @PutMapping("/authors/{authorId}/books/{id}")
    public BookDTO updateBook(Long authorId, BookDTO book) {
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est
        // mauvaise

        //mise à jour d'un auteur existant par son id
       
        return null;
    }

    public void deleteBook(Long id) {

    }

    public void addAuthor(Long authorId, AuthorDTO author) {

    }
}
