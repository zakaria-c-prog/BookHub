package edu.njust.bookhub.web;

import edu.njust.bookhub.model.Book;
import edu.njust.bookhub.model.Provider;
import edu.njust.bookhub.service.BusinessRuleException;
import edu.njust.bookhub.service.CatalogService;
import edu.njust.bookhub.service.ProviderService;
import edu.njust.bookhub.web.form.ProviderForm;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/providers")
public class ProviderController {

    private final ProviderService providers;
    private final CatalogService catalog;

    public ProviderController(ProviderService providers, CatalogService catalog) {
        this.providers = providers;
        this.catalog = catalog;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("providers", providers.list());
        return "providers/list";
    }

    /** Provider details plus the list of books it supplies and their stock value. */
    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        List<Book> books = catalog.booksByProvider(id);
        BigDecimal stockValue = books.stream()
                .map(b -> b.getPrice().multiply(BigDecimal.valueOf(b.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("provider", providers.get(id));
        model.addAttribute("books", books);
        model.addAttribute("stockValue", stockValue);
        return "providers/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ProviderForm());
        return "providers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ProviderForm form, BindingResult errors,
                         RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return "providers/form";
        }
        try {
            int id = providers.create(form.toProvider(null));
            flash.addFlashAttribute("success", "Provider " + form.getCompanyName().trim() + " added.");
            return "redirect:/providers/" + id;
        } catch (BusinessRuleException e) {
            errors.rejectValue(e.getField(), "rule", e.getMessage());
            return "providers/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("form", ProviderForm.from(providers.get(id)));
        model.addAttribute("providerId", id);
        return "providers/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable int id, @Valid @ModelAttribute("form") ProviderForm form,
                         BindingResult errors, Model model, RedirectAttributes flash) {
        model.addAttribute("providerId", id);
        if (errors.hasErrors()) {
            return "providers/form";
        }
        try {
            providers.update(form.toProvider(id));
            flash.addFlashAttribute("success", "Provider details saved.");
            return "redirect:/providers/" + id;
        } catch (BusinessRuleException e) {
            errors.rejectValue(e.getField(), "rule", e.getMessage());
            return "providers/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id, RedirectAttributes flash) {
        Provider provider = providers.get(id);
        providers.delete(id);
        flash.addFlashAttribute("success", "Provider " + provider.getCompanyName()
                + " was deleted; its books are kept without a provider.");
        return "redirect:/providers";
    }
}
