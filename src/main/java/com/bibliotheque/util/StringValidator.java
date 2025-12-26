package com.bibliotheque.util;

import com.bibliotheque.exception.ValidationException;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static String validerISBN(String isbn) throws ValidationException {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new ValidationException("ISBN", "L'ISBN ne peut pas être vide");
        }
        String clean = isbn.replaceAll("[\\s-]", "");
        if (!clean.matches("\\d{13}")) {
            throw new ValidationException("ISBN", "L'ISBN doit contenir exactement 13 chiffres");
        }
        return formatISBN(clean);
    }

    public static String formatISBN(String isbn) {
        String clean = isbn.replaceAll("[\\s-]", "");
        if (clean.length() == 13) {
            return String.format("%s-%s-%s-%s-%s",
                    clean.substring(0, 3),
                    clean.substring(3, 4),
                    clean.substring(4, 8),
                    clean.substring(8, 12),
                    clean.substring(12, 13));
        }
        return isbn;
    }

    public static String validerEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email", "L'email ne peut pas être vide");
        }
        String t = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(t).matches()) {
            throw new ValidationException("Email", "Format d'email invalide");
        }
        return t;
    }

    public static String formaterNomPropre(String nom) {
        if (nom == null || nom.trim().isEmpty()) return "";
        String trimmed = nom.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    public static String formaterNomComplet(String prenom, String nom) {
        String p = formaterNomPropre(prenom);
        String n = (nom == null) ? "" : nom.trim().toUpperCase();
        return p + (n.isEmpty() ? "" : " " + n);
    }

    public static boolean contientIgnoreCase(String text, String recherche) {
        if (text == null || recherche == null) return false;
        return normaliserTexte(text).contains(normaliserTexte(recherche));
    }

    private static String normaliserTexte(String text) {
        String lower = text.toLowerCase();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}


