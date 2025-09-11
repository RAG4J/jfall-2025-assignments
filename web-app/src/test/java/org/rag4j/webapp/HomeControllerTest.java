package org.rag4j.webapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HomeControllerTest {
    @Test
    @DisplayName("chatPage returns home view name")
    void chatPageReturnsHomeViewName() {
        HomeController controller = new HomeController();
        String viewName = controller.chatPage();
        assertThat(viewName).isEqualTo("home");
    }
}

