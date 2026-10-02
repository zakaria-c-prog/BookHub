package edu.njust.bookhub.web;

import edu.njust.bookhub.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Dashboard with catalogue figures, low-stock alerts and the newest titles. */
@Controller
public class HomeController {

    private final CatalogService catalog;

    public HomeController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("stats", catalog.stats());
        model.addAttribute("lowStock", catalog.lowStockReport());
        model.addAttribute("recent", catalog.recentlyAdded(5));
        return "index";
    }
}
