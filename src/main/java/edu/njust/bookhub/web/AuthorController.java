package edu.njust.bookhub.web;

import edu.njust.bookhub.model.Author;
import edu.njust.bookhub.service.AuthorService;
import edu.njust.bookhub.service.CatalogService;
import edu.njust.bookhub.web.form.AuthorForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorService authors;
    private final CatalogService catalog;

    public AuthorController(AuthorService authors, CatalogService catalog) {
        this.authors = authors;
        this.catalog = catalog;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("authors", authors.list(q));
        model.addAttribute("q", q);
        return "authors/list";
    }

    /** Author profile together with every book they wrote. */
    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        model.addAttribute("author", authors.get(id));
        model.addAttribute("books", catalog.booksByAuthor(id));
        return "authors/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new AuthorForm());
        return "authors/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") AuthorForm form, BindingResult errors,
                         RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return "authors/form";
        }
        int id = authors.create(form.toAuthor(null));
        flash.addFlashAttribute("success", "Author " + form.getFullName().trim() + " added.");
        return "redirect:/authors/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("form", AuthorForm.from(authors.get(id)));
        model.addAttribute("authorId", id);
        return "authors/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable int id, @Valid @ModelAttribute("form") AuthorForm form, BindingResult errors,
                         Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            model.addAttribute("authorId", id);
            return "authors/form";
        }
        authors.update(form.toAuthor(id));
        flash.addFlashAttribute("success", "Author details saved.");
        return "redirect:/authors/" + id;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id, RedirectAttributes flash) {
        Author author = authors.get(id);
        authors.delete(id);
        flash.addFlashAttribute("success", "Author " + author.getFullName() + " was deleted.");
        return "redirect:/authors";
    }
}
