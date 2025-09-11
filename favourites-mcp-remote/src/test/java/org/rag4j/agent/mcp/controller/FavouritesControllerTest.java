package org.rag4j.agent.mcp.controller;

import org.junit.jupiter.api.Test;
import org.rag4j.agent.mcp.FavouritesService;
import org.rag4j.agent.mcp.model.FavouriteTalk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavouritesController.class)
class FavouritesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavouritesService favouritesService;

    @Test
    void testFavouritesPage() throws Exception {
        // Arrange
        List<FavouritesService.FavouriteTalkWithUser> mockFavourites = Arrays.asList(
                new FavouritesService.FavouriteTalkWithUser("Spring Boot in Action", new String[]{"Craig Walls"}, "testuser"),
                new FavouritesService.FavouriteTalkWithUser("Modern Java", new String[]{"Ken Kousen", "Venkat Subramaniam"}, "testuser")
        );
        when(favouritesService.listFavouritesForWeb(eq("testuser"), isNull()))
                .thenReturn(mockFavourites);

        // Act & Assert
        mockMvc.perform(get("/favourites").param("userId", "testuser"))
                .andExpect(status().isOk())
                .andExpect(view().name("favourites"))
                .andExpect(model().attribute("favourites", mockFavourites))
                .andExpect(model().attribute("userId", "testuser"));
    }

    @Test
    void testFavouritesPageWithFilter() throws Exception {
        // Arrange
        List<FavouritesService.FavouriteTalkWithUser> filteredFavourites = Arrays.asList(
                new FavouritesService.FavouriteTalkWithUser("Modern Java", new String[]{"Venkat Subramaniam"}, "testuser")
        );
        when(favouritesService.listFavouritesForWeb(eq("testuser"), eq("venkat")))
                .thenReturn(filteredFavourites);

        // Act & Assert
        mockMvc.perform(get("/favourites")
                        .param("userId", "testuser")
                        .param("speakerFilter", "venkat"))
                .andExpect(status().isOk())
                .andExpect(view().name("favourites"))
                .andExpect(model().attribute("favourites", filteredFavourites))
                .andExpect(model().attribute("speakerFilter", "venkat"));
    }

    @Test
    void testShowAddForm() throws Exception {
        mockMvc.perform(get("/favourites/add").param("userId", "testuser"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-favourite"))
                .andExpect(model().attributeExists("favouriteTalk"));
    }

    @Test
    void testAddFavourite() throws Exception {
        // Arrange
        when(favouritesService.addFavourite(eq("testuser"), any(FavouriteTalk.class)))
                .thenReturn("Favourite talk 'Test Talk' by John Doe added for user testuser");

        // Act & Assert
        mockMvc.perform(post("/favourites/add")
                        .param("userId", "testuser")
                        .param("title", "Test Talk")
                        .param("speakers", "John Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favourites?userId=testuser"));
    }

    @Test
    void testAddFavouriteWithEmptyUserId() throws Exception {
        // Arrange
        when(favouritesService.addFavourite(eq("user"), any(FavouriteTalk.class)))
                .thenReturn("Favourite talk 'Test Talk' by John Doe added for user user");

        // Act & Assert - empty userId should default to "user"
        mockMvc.perform(post("/favourites/add")
                        .param("userId", "")
                        .param("title", "Test Talk")
                        .param("speakers", "John Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favourites?userId=user"));
    }

    @Test
    void testFavouritesPageShowAllUsers() throws Exception {
        // Arrange
        List<FavouritesService.FavouriteTalkWithUser> allUsersFavourites = Arrays.asList(
                new FavouritesService.FavouriteTalkWithUser("Spring Boot", new String[]{"Craig Walls"}, "user1"),
                new FavouritesService.FavouriteTalkWithUser("Modern Java", new String[]{"Venkat"}, "user2")
        );
        when(favouritesService.listFavouritesForWeb(isNull(), isNull()))
                .thenReturn(allUsersFavourites);

        // Act & Assert - no userId parameter should show all users
        mockMvc.perform(get("/favourites"))
                .andExpect(status().isOk())
                .andExpect(view().name("favourites"))
                .andExpect(model().attribute("favourites", allUsersFavourites))
                .andExpect(model().attribute("showingAllUsers", true))
                .andExpect(model().attribute("userId", ""));
    }
}
