package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.data.domain.Book.Language;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.authors.AuthorMapper;
import fr.uga.l3miage.library.service.AuthorService;

import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;
    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper, AuthorService authorService,
            AuthorMapper authorMapper) {
        this.bookService = bookService;
        this.booksMapper = booksMapper;
        this.authorService = authorService;
        this.authorMapper = authorMapper;
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
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO newBook(@PathVariable Long authorId, @RequestBody BookDTO book) {

        // vérifier la langue
        if (book.language() != null) {
            boolean correctLanguage = false;
            for (Language language : Language.values()) {
                if (language.name().equalsIgnoreCase(book.language())) {
                    correctLanguage = true;
                    break;
                }
            }
            if (!correctLanguage) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        if (Long.toString(book.isbn()) == null ||
                Long.toString(book.isbn()).length() < 3 ||
                Short.toString(book.year()) == null ||
                book.year() > Year.now().getValue() ||
                book.publisher() == null ||
                book.title() == null) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {

            Author auteur = authorService.get(authorId);
            Set<Author> authorSet = new HashSet<>();
            authorSet.add(auteur);
            Book newBook = booksMapper.dtoToEntity(book);
            newBook.setId(book.id());
            newBook.setAuthors(authorSet);
            bookService.save(authorId, newBook);
            return booksMapper.entityToDTO(newBook);

        } catch (EntityNotFoundException e) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

    }

    // mettre à jour un livre
    @PutMapping("/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO updateBook(@PathVariable Long id, @RequestBody BookDTO book) {
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
                return booksMapper.entityToDTO(existingBook);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return null;

    }

    // supprimer un livre
    @DeleteMapping("/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {

        try {
            bookService.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PutMapping("/books/{id}/authors")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO addAuthor(@PathVariable("id") Long bookId, @RequestBody AuthorDTO author) {

        try {
            Author auth = authorService.get(author.id());
            if (auth == null) {
                Author aut = authorMapper.dtoToEntity(author);
                auth = authorService.save(aut);
            }
            Book livre = bookService.get(bookId);
            bookService.save(auth.getId(), livre);
            return this.booksMapper.entityToDTO(livre);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

    }
}
