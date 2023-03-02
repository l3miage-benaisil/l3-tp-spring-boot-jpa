package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.data.domain.Book.Language;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.service.AuthorService;

import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.Collection;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;
    // import des services et mapper d'auteur
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
    public BookDTO newBook(@PathVariable Long authorId, @RequestBody BookDTO book, HttpServletResponse response) {

        Book newBook = booksMapper.dtoToEntity(book);

        try {
            Author author = authorService.get(authorId);
            newBook.addAuthor(author);
        } catch (EntityNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response.setStatus(404);
        }

        // editeur non vide et titre non vide
        if (book.publisher() == null || book.title() == null) {
            response.setStatus(400);
            return null;
        }

        // check if book language is a valid Language
        if (book.language() != null) {
            try {
                //Language.valueOf(book.language().toUpperCase());
                newBook.setLanguage(Language.valueOf(book.language().toUpperCase()));
            } catch (Exception e) {
                response.setStatus(400);
                return null;
            }
            
        }

        
        if (Long.toString(book.isbn()) == null || Long.toString(book.isbn()).length() < 3) {
            response.setStatus(400);
            return null;
        }

        if(Short.toString(book.year()) == null || book.year() > Year.now().getValue()) {
            response.setStatus(400);
            return null;
        }

        newBook.setPublisher(book.publisher());
        newBook.setTitle(book.title());

        newBook.setIsbn(book.isbn());
        newBook.setYear(book.year());

        try {
            bookService.save(authorId, newBook);
            response.setStatus(201);
            return booksMapper.entityToDTO(newBook);

        } catch (EntityNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response.setStatus(404);
            return null;
        }

    }

    // mettre à jour un livre
    @PutMapping("/books/{id}")
    public BookDTO updateBook(@PathVariable Long id, @RequestBody BookDTO book, HttpServletResponse response) {
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est
        // mauvaise

        Book newBook = booksMapper.dtoToEntity(book);

        if (book.id() == id) {
            try {
                Book existingBook = bookService.get(id);
                existingBook.setTitle(newBook.getTitle());
                existingBook.setIsbn(newBook.getIsbn());
                existingBook.setPublisher(newBook.getPublisher());
                existingBook.setLanguage(Language.valueOf(newBook.getLanguage().toString().toUpperCase()));
                existingBook.setYear(newBook.getYear());

                response.setStatus(200);
                return booksMapper.entityToDTO(existingBook);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response.setStatus(400);
            return null;

        }
        return null;

    }

    // supprimer un livre
    @DeleteMapping("/books/{id}")
    public void deleteBook(@PathVariable Long id, HttpServletResponse response) {

        try {
            bookService.delete(id);
            response.setStatus(204);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PutMapping("/books/{bookId}/authors")
    public void addAuthor(@PathVariable Long bookId, @RequestBody AuthorDTO author, HttpServletResponse response) {
        Book existingbook;

        try {
            existingbook = bookService.get(bookId);
            Author myAuthor = authorService.get(author.id());

            existingbook.addAuthor(myAuthor);
            bookService.save(bookId, existingbook);

            // response.setStatus(201);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(404);
        }

    }
}
