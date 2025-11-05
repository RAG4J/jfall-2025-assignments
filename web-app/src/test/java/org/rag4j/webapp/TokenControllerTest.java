package org.rag4j.webapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rag4j.tokens.TokenService;
import org.rag4j.webapp.config.ConfigurationMismatchHandler;
import org.rag4j.tokens.model.TokenStatus;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;

class TokenControllerTest {
    @Test
    @DisplayName("homePage adds info when token is missing")
    void homePageAddsInfoWhenTokenIsMissing() {
        ConfigurationMismatchHandler mismatchHandler = Mockito.mock(ConfigurationMismatchHandler.class);
        TokenService tokenService = Mockito.mock(TokenService.class);
        Mockito.when(tokenService.getTokenStatus()).thenReturn(
            new TokenStatus(false, null, 0, "No token cached")
        );
        TokenController controller = new TokenController("http://proxy", mismatchHandler, tokenService);
        Model model = Mockito.mock(Model.class);
        String view = controller.homePage(model, null);
        Mockito.verify(model).addAttribute(Mockito.eq("info"), Mockito.anyString());
        assertThat(view).isEqualTo("token");
    }

    @Test
    @DisplayName("homePage does not add error when token is present")
    void homePageDoesNotAddErrorWhenTokenIsPresent() {
        ConfigurationMismatchHandler mismatchHandler = Mockito.mock(ConfigurationMismatchHandler.class);
        TokenService tokenService = Mockito.mock(TokenService.class);
        Mockito.when(tokenService.getTokenStatus()).thenReturn(
            new TokenStatus(true, "test-user", 60, null)
        );
        TokenController controller = new TokenController("http://proxy", mismatchHandler, tokenService);
        Model model = Mockito.mock(Model.class);
        String view = controller.homePage(model, null);
        Mockito.verify(model, Mockito.never()).addAttribute(Mockito.eq("error"), Mockito.any());
        assertThat(view).isEqualTo("token");
    }

    @Test
    @DisplayName("handleFetchToken returns error when userId is null")
    void handleFetchTokenReturnsErrorWhenUserIdIsNull() {
        ConfigurationMismatchHandler mismatchHandler = Mockito.mock(ConfigurationMismatchHandler.class);
        TokenService tokenService = Mockito.mock(TokenService.class);
        TokenController controller = new TokenController("http://proxy", mismatchHandler, tokenService);
        Model model = Mockito.mock(Model.class);
        String view = controller.handleFetchToken(null, "password", model);
        Mockito.verify(model).addAttribute("error", "You need to provide a username.");
        assertThat(view).isEqualTo("token");
    }

    @Test
    @DisplayName("handleFetchToken returns error when userId is empty")
    void handleFetchTokenReturnsErrorWhenUserIdIsEmpty() {
        ConfigurationMismatchHandler mismatchHandler = Mockito.mock(ConfigurationMismatchHandler.class);
        TokenService tokenService = Mockito.mock(TokenService.class);
        TokenController controller = new TokenController("http://proxy", mismatchHandler, tokenService);
        Model model = Mockito.mock(Model.class);
        String view = controller.handleFetchToken("   ", "password", model);
        Mockito.verify(model).addAttribute("error", "You need to provide a username.");
        assertThat(view).isEqualTo("token");
    }
}

