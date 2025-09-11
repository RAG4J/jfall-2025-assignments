package org.rag4j.agent.mcp.controller;

import org.rag4j.agent.mcp.FavouritesService;
import org.rag4j.agent.mcp.model.FavouriteTalk;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/favourites")
public class FavouritesController {

    private final FavouritesService favouritesService;

    public FavouritesController(FavouritesService favouritesService) {
        this.favouritesService = favouritesService;
    }

    @GetMapping
    public String favourites(@RequestParam(required = false) String userId,
                           @RequestParam(required = false) String speakerFilter,
                           Model model) {
        // Use web-only service method that supports showing all users' favourites
        List<FavouritesService.FavouriteTalkWithUser> favourites = 
                favouritesService.listFavouritesForWeb(userId, speakerFilter);
        
        model.addAttribute("favourites", favourites);
        model.addAttribute("userId", userId != null ? userId : "");
        model.addAttribute("speakerFilter", speakerFilter != null ? speakerFilter : "");
        model.addAttribute("showingAllUsers", userId == null || userId.trim().isEmpty());
        return "favourites";
    }

    @GetMapping("/add")
    public String showAddForm(@RequestParam(required = false, defaultValue = "user") String userId, Model model) {
        FavouriteTalkForm form = new FavouriteTalkForm();
        form.setUserId(userId);
        model.addAttribute("favouriteTalk", form);
        return "add-favourite";
    }

    @PostMapping("/add")
    public String addFavourite(@ModelAttribute FavouriteTalkForm form,
                             RedirectAttributes redirectAttributes) {
        try {
            // Use the userId from the form, defaulting to "user" if empty
            String userId = (form.getUserId() != null && !form.getUserId().trim().isEmpty()) 
                          ? form.getUserId().trim() : "user";
            
            String[] speakers = form.getSpeakers().split(",");
            // Trim whitespace from speakers
            for (int i = 0; i < speakers.length; i++) {
                speakers[i] = speakers[i].trim();
            }
            
            FavouriteTalk favouriteTalk = new FavouriteTalk(form.getTitle(), speakers);
            String result = favouritesService.addFavourite(userId, favouriteTalk);
            redirectAttributes.addFlashAttribute("successMessage", result);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding favourite: " + e.getMessage());
        }
        
        // Use the userId from the form for redirect
        String redirectUserId = (form.getUserId() != null && !form.getUserId().trim().isEmpty()) 
                              ? form.getUserId().trim() : "user";
        redirectAttributes.addAttribute("userId", redirectUserId);
        return "redirect:/favourites";
    }

    // DTO class for form binding
    public static class FavouriteTalkForm {
        private String title;
        private String speakers; // Comma-separated speakers
        private String userId;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSpeakers() {
            return speakers;
        }

        public void setSpeakers(String speakers) {
            this.speakers = speakers;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}