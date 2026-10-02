package edu.njust.bookhub.web;

import edu.njust.bookhub.model.Book;
import edu.njust.bookhub.model.BookSearchCriteria;
import edu.njust.bookhub.service.AuthorService;
import edu.njust.bookhub.service.BusinessRuleException;
import edu.njust.bookhub.service.CatalogService;
import edu.njust.bookhub.service.ProviderService;
import edu.njust.bookhub.web.form.BookForm;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    /** Offered in the category picker in addition to categories already in use. */
    private static final List<String> SUGGESTED_CATEGORIES = List.of(
            "Computer Science", "Software Engineering", "Data Science", "Design",
            "Fiction", "History", "Business", "Science", "Children");

    private final CatalogService catalog;
    private final AuthorService authors;
    private final ProviderService providers;

    public BookController(CatalogService catalog, AuthorService authors, ProviderService providers) {
        this.catalog = catalog;
        this.authors = authors;
        this.providers = providers;
    }

    // ================================================================= lookups

    /** Catalogue page: combined search by keyword, author, provider, category, price range. */
    @GetMapping
    public String list(@ModelAttribute("criteria") BookSearchCriteria criteria, BindingResult badFilters, Model model) {
        // Unparseable filter values (e.g. "abc" as a price) are simply left out of the search
        model.addAttribute("books", catalog.search(criteria));
        model.addAttribute("providers", providers.list());
        model.addAttribute("categories", catalog.categories());
        model.addAttribute("sortOrders", BookSearchCriteria.SortOrder.values());
        return "books/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        model.addAttribute("book", catalog.getBook(id));
        return "books/detail";
    }

    @GetMapping("/low-stock")
    public String lowStock(Model model) {
        model.addAttribute("books", catalog.lowStockReport());
        model.addAttribute("threshold", Book.LOW_STOCK_THRESHOLD);
        return "books/low-stock";
    }

    // ================================================================= create / edit / delete

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return formView(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BookForm form, BindingResult errors,
                         Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return formView(model, null);
        }
        try {
            int id = catalog.addBook(form.toBook(null), form.getAuthorIds());
            flash.addFlashAttribute("success", "\"" + form.getTitle().trim() + "\" was added to the catalogue.");
            return "redirect:/books/" + id;
        } catch (BusinessRuleException e) {
            errors.rejectValue(e.getField(), "rule", e.getMessage());
            return formView(model, null);
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("form", BookForm.from(catalog.getBook(id)));
        return formView(model, id);
    }

    @PutMapping("/{id}")
    public String update(@PathVariable int id, @Valid @ModelAttribute("form") BookForm form, BindingResult errors,
                         Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return formView(model, id);
        }
        try {
            catalog.updateBook(form.toBook(id), form.getAuthorIds());
            flash.addFlashAttribute("success", "Changes saved.");
            return "redirect:/books/" + id;
        } catch (BusinessRuleException e) {
            errors.rejectValue(e.getField(), "rule", e.getMessage());
            return formView(model, id);
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id, RedirectAttributes flash) {
        String title = catalog.getBook(id).getTitle();
        catalog.deleteBook(id);
        flash.addFlashAttribute("success", "\"" + title + "\" was removed from the catalogue.");
        return "redirect:/books";
    }

    // ================================================================= stock & price

    @PostMapping("/{id}/stock")
    public String changeStock(@PathVariable int id, @RequestParam String action,
                              @RequestParam(defaultValue = "0") int copies, RedirectAttributes flash) {
        try {
            if ("sell".equals(action)) {
                catalog.recordSale(id, copies);
                flash.addFlashAttribute("success", "Recorded sale of " + copies + " cop" + (copies == 1 ? "y." : "ies."));
            } else {
                catalog.restock(id, copies);
                flash.addFlashAttribute("success", "Received " + copies + " new cop" + (copies == 1 ? "y." : "ies."));
            }
        } catch (BusinessRuleException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books/" + id;
    }

    @PostMapping("/{id}/price")
    public String changePrice(@PathVariable int id, @RequestParam String mode,
                              @RequestParam(required = false) BigDecimal value, RedirectAttributes flash) {
        try {
            if ("discount".equals(mode)) {
                int percent = value == null ? 0 : value.intValue();
                BigDecimal newPrice = catalog.applyDiscount(id, percent);
                flash.addFlashAttribute("success", percent + "% discount applied - new price ¥" + newPrice + ".");
            } else {
                catalog.setPrice(id, value);
                flash.addFlashAttribute("success", "Price updated.");
            }
        } catch (BusinessRuleException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books/" + id;
    }

    private String formView(Model model, Integer bookId) {
        List<String> categories = new ArrayList<>(SUGGESTED_CATEGORIES);
        catalog.categories().stream().filter(c -> !categories.contains(c)).forEach(categories::add);
        model.addAttribute("bookId", bookId);
        model.addAttribute("allAuthors", authors.list(null));
        model.addAttribute("providers", providers.list());
        model.addAttribute("categories", categories);
        return "books/form";
    }
}
