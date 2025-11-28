package com.litellm.sdk.token;

import java.util.*;

public class Tokenizer {
    private Tokenizer() {
    }

    public static List<Integer> encode(String model, String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        int tokenCount = estimateTokenCount(model, text);
        List<Integer> tokens = new ArrayList<>(tokenCount);
        for (int i = 0; i < tokenCount; i++) {
            tokens.add(i);
        }
        return tokens;
    }

    public static String decode(String model, List<Integer> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return "";
        }

        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                decoded.append(" ");
            }
            decoded.append("token").append(tokens.get(i));
        }
        return decoded.toString();
    }

    public static int tokenCounter(String model, String text) {
        return estimateTokenCount(model, text);
    }

    public static int tokenCounter(String model, List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }

        int totalTokens = 0;
        for (String message : messages) {
            totalTokens += estimateTokenCount(model, message);
        }
        return totalTokens;
    }

    private static int estimateTokenCount(String model, String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int tokens = text.length() / 4;
        tokens = Math.max(1, tokens);
        return tokens;
    }
}
