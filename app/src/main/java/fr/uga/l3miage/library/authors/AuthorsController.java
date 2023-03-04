package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.BookService;

import fr.uga.l3miage.library.service.AuthorService;
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

import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AuthorsController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;
    private final BooksMapper booksMapper;
    private final BookService bookService;

    @Autowired
    public AuthorsController(AuthorService authorService, AuthorMapper authorMapper, BooksMapper booksMapper,
            BookService bookService) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
        this.booksMapper = booksMapper;
        this.bookService = bookService;
    }

    // liste des auteurs existants
    @GetMapping("/authors")
    public Collection<AuthorDTO> authors(@RequestParam(value = "q", required = false) String query) {
        Collection<Author> authors;
        if (query == null) {
            authors = authorService.list();
        } else {
            authors = authorService.searchByName(query);
        }
        return authors.stream()
                .map(authorMapper::entityToDTO)
                .toList();
    }

    // chercher un auteur par son id
    @GetMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AuthorDTO author(@PathVariable Long id) {

        Author author;
        try {
            if (id < 0) {
                id = id * -1;
            }
            author = authorService.get(id);
            return authorMapper.entityToDTO(author);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "the author was not found");
        }
    }

    // créer un nouvel auteur
    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDTO newAuthor(@RequestBody AuthorDTO author) {

        // créer un nouvel auteur
        Author newAuthor = authorMapper.dtoToEntity(author);

        if (newAuthor.getFullName().toString().trim() == "") {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else {
            newAuthor.setFullName(author.fullName());
            authorService.save(newAuthor);
            return authorMapper.entityToDTO(newAuthor);

        }
    }

    // mettre à jour un auteur existant par son id
    @PutMapping("/authors/{id}")
    public AuthorDTO updateAuthor(@RequestBody AuthorDTO author, @PathVariable Long id) {
        // mise à jour d'un auteur existant par son id
        try {
            Author existingAuthor = authorService.get(id);
            existingAuthor.setFullName(author.fullName());
            return authorMapper.entityToDTO(existingAuthor);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "the author was not found");
        }

    }

    // supprimer un auteur par son id
    @DeleteMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable("id") Long id) {
        try {
            Author aut = authorService.get(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {

            for (Book book : authorService.get(id).getBooks()) {
                if (book.getAuthors().size() > 1) {
                    bookService.delete(book.getId());
                }
            }

            this.authorService.delete(id);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    // liste des livres d'un auteur par son id et un paramètre de recherche q
    // (optionnel) pour filtrer par titre
    @GetMapping("/authors/{authorId}/books")
    public Collection<BookDTO> books(@PathVariable Long authorId,
            @RequestParam(value = "q", required = false) String q) {

        Collection<BookDTO> res = Collections.emptyList();

        Author author;
        if (q == null) {

            try {
                author = authorService.get(authorId);
                authorMapper.entityToDTO(author);
                if (author.getBooks() == null) {
                    return Collections.emptyList();
                } else {
                    res = author.getBooks().stream()
                            .map(booksMapper::entityToDTO)
                            .toList();
                }

            } catch (EntityNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {

            try {
                author = authorService.get(authorId);
                authorMapper.entityToDTO(author);
                if (author.getBooks() == null) {
                    res = Collections.emptyList();
                } else {
                    res = author.getBooks().stream()
                            .filter(book -> book.getTitle().contains(q))
                            .map(booksMapper::entityToDTO)
                            .toList();
                }

            } catch (EntityNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return res;
    }

}
