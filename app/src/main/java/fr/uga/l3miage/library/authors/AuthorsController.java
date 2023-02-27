package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.DeleteAuthorException;
import fr.uga.l3miage.library.service.EntityNotFoundException;

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

import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AuthorsController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;
    private final BooksMapper booksMapper;

    @Autowired
    public AuthorsController(AuthorService authorService, AuthorMapper authorMapper, BooksMapper booksMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
        this.booksMapper = booksMapper;
    }

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

    @GetMapping("/authors/{id}")
    public AuthorDTO author(@PathVariable Long id) {

        Author author;
        try {
            author = authorService.get(id);
            return authorMapper.entityToDTO(author);

        } catch (EntityNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/authors")
    public AuthorDTO newAuthor(@RequestBody AuthorDTO author) {

        // create a new author
        Author newAuthor = authorMapper.dtoToEntity(author);
        newAuthor.setFullName(author.fullName());
        authorService.save(newAuthor);

        return authorMapper.entityToDTO(newAuthor);
    }

    @PutMapping("/authors/{id}")
    public AuthorDTO updateAuthor(@RequestBody AuthorDTO author, @PathVariable Long id) {
        // attention AuthorDTO.id() doit être égale à id, sinon la requête utilisateur
        // est mauvaise

        // update an existing author
        try {
            Author existingAuthor = authorService.get(id);
            existingAuthor.setFullName(author.fullName());
            return authorMapper.entityToDTO(existingAuthor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @DeleteMapping("/authors/{id}")
    public void deleteAuthor(@PathVariable Long id) {
        try {
            authorService.delete(id);
        } catch (DeleteAuthorException | EntityNotFoundException e) {
            e.printStackTrace();
        }

    }

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
                    System.out.println("author.getBooks() is null");
                    return Collections.emptyList();
                } else {
                    res= author.getBooks().stream()
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
                    System.out.println("author.getBooks() is null");
                    res= Collections.emptyList();
                } else {
                    res= author.getBooks().stream()
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
