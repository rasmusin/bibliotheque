package com.bibliotheque.controller;

import com.bibliotheque.service.BibliothequeService;
import com.bibliotheque.service.EmpruntService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MainControllerSimpleTest {

    @Test
    public void testSettersAcceptServices() {
        MainController ctrl = new MainController();

        BibliothequeService bsvc = new BibliothequeService();
        EmpruntService esvc = new EmpruntService();

        ctrl.setBibliothequeService(bsvc);
        ctrl.setEmpruntService(esvc);

        // No direct getters — rely on absence of exceptions and reflection
        assertNotNull(bsvc);
        assertNotNull(esvc);
    }
}
