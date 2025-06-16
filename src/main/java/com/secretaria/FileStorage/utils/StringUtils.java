package com.secretaria.FileStorage.utils;

import java.text.Normalizer;

public class StringUtils {
    public static String normalize(String input) {
        if (input == null) return null;

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", ""); // remove acentos
        normalized = normalized.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit} ]", "");  // remove símbolos (opcional)
        return normalized.toLowerCase().trim();
    }
}


