package org.example.kaibutsu;

import org.example.kaibutsu.application.Kaibutsu;
import org.junit.jupiter.api.Test;

class KaibutsuTest {
    @Test
    void crawlQuote() {
        Kaibutsu.run("quote");
    }
}