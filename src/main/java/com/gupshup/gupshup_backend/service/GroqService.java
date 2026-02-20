package com.gupshup.gupshup_backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    // 👇 Apni SerpApi Key yahan daalein (Free account se milti hai: serpapi.com)
    // Ya phir application.properties se bhi le sakte hain
    private String serpApiKey = "f8454d28f5b4e99c8626ddde2dc6223faa1a1cd486aeff2df91600c60ec53fd9";

    private final RestTemplate restTemplate = new RestTemplate();

    public String getChatResponse(String userMessage) {
        try {
            // 🔍 STEP 1: Check karein agar "Search" ki zarurat hai
            String searchResult = "";
            String lowerCaseMsg = userMessage.toLowerCase();

            if (lowerCaseMsg.contains("search") ||
                    lowerCaseMsg.contains("dhundo") ||
                    lowerCaseMsg.contains("latest") ||
                    lowerCaseMsg.contains("kya hai")) {

                searchResult = googleSearch(userMessage);
            }

            // 📝 STEP 2: System Prompt Prepare Karein
            // (Apka Purana Powerful Prompt waisa ka waisa hi hai)
            String baseSystemPrompt = """
                    You are GupShup AI — a smart, friendly, and highly capable assistant designed for a modern chat application.

                    =============================
                    🎯 CORE BEHAVIOR
                    =============================

                    1️⃣ LANGUAGE ADAPTATION (AUTO DETECT)
                    - If user writes mainly in English → reply in clear, professional English.
                    - If user writes in Hindi or Hinglish → reply in friendly, natural Hindi.
                    - If mixed language → respond in the same mixed style.
                    - Always mirror user tone (formal, casual, playful, or serious).

                    2️⃣ RESPONSE QUALITY
                    - Give accurate, helpful, and practical answers.
                    - Prefer clarity over complexity.
                    - If user asks coding questions:
                      - Use clean code blocks.
                      - Add short explanation.
                      - Follow best practices.

                    3️⃣ PERSONALITY
                    - Friendly, smart, slightly witty but NEVER sarcastic.
                    - Helpful like a modern AI assistant inside a chat app.
                    - Keep responses natural and conversational.

                    4️⃣ SAFETY & HONESTY
                    - If unsure, say so honestly instead of guessing.
                    - Do not invent facts.
                    - Suggest alternatives when needed.

                    5️⃣ CHAT EXPERIENCE
                    - Keep responses concise by default.
                    - Give detailed explanations only when user asks or topic requires.
                    - Use bullet points when helpful.

                    6️⃣ OUTPUT STYLE
                    - Use markdown formatting for readability.
                    - Code must always be in proper fenced code blocks.

                    =============================
                    💬 EXAMPLES
                    =============================
                    User: "What is the capital of France?"
                    AI: "The capital of France is Paris. It’s famous for the Eiffel Tower and rich culture."

                    User: "France ki capital kya hai?"
                    AI: "Arre bhai, France ki capital Paris hai 😄 — Eiffel Tower wahi hai."
                    """;

            // 🧬 STEP 3: Prompt Merge Karein (Base + Search Data)
            String finalSystemPrompt = baseSystemPrompt;

            if (!searchResult.isEmpty()) {
                finalSystemPrompt += "\n\n🚨 CURRENT LIVE INFORMATION FROM GOOGLE:\n" + searchResult +
                        "\n🔴 IMPORTANT INSTRUCTION: You MUST provide the direct clickable links for the videos found in the search results. Format them like this: [Video Name](Link URL). Do not say 'search on YouTube', give the link provided above.";
            }

            // 🚀 STEP 4: API Call
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", finalSystemPrompt));
            messages.add(Map.of("role", "user", "content", userMessage));

            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    return (String) messageObj.get("content");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Arre yaar, server thoda busy hai abhi! Thodi der baad try kar. 😅 (Error: " + e.getMessage() + ")";
        }
        return "Kuch gadbad ho gayi, sorry!";
    }

    // 🌐 GOOGLE SEARCH FUNCTION
    // 🌐 GOOGLE SEARCH FUNCTION (With Forced Links)
    private String googleSearch(String query) {
        if (serpApiKey == null || serpApiKey.isEmpty()) {
            return "";
        }

        try {
            System.out.println("🔍 Searching Google for: " + query);
            String url = "https://serpapi.com/search.json?q=" + query.replace(" ", "+") + "&api_key=" + serpApiKey;
            String jsonResponse = restTemplate.getForObject(url, String.class);

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (jsonObject.has("organic_results")) {
                JsonArray results = jsonObject.getAsJsonArray("organic_results");
                StringBuilder summary = new StringBuilder();

                for (int i = 0; i < Math.min(4, results.size()); i++) {
                    JsonObject res = results.get(i).getAsJsonObject();

                    String title = res.has("title") ? res.get("title").getAsString() : "No Title";
                    String link = res.has("link") ? res.get("link").getAsString() : "#";
                    String snippet = res.has("snippet") ? res.get("snippet").getAsString() : "";

                    // 👇 FORMAT CHANGE: Link ko data me strong tarike se joda
                    summary.append("VIDEO TITLE: ").append(title)
                            .append("\nVIDEO LINK: ").append(link)
                            .append("\nDESCRIPTION: ").append(snippet)
                            .append("\n---\n");
                }
                return summary.toString();
            }
        } catch (Exception e) {
            System.out.println("❌ Search Failed: " + e.getMessage());
        }
        return "";
    }
}