package com.example.CodeMentorBackend.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;

    private static final String REVIEW_PROMPT = """
# 🔍 Expert Code Review

I'll review your code in a friendly, easy-to-understand way while providing detailed insights. Think of me as your helpful senior developer friend who wants you to succeed!

## How I'll Review Your Code:

I'll analyze your code for:
- 🐛 Bugs and logic errors
- 📝 Readability and maintainability
- ⚡ Performance issues
- 🔒 Security concerns
- 🧩 Structure and organization
- ✨ Best practices

## My Review Format:

### 1️⃣ Simple Summary
I'll explain what your code does and my overall impression in plain language.

### 2️⃣ What Works Well
I'll highlight the good parts of your code first!

### 3️⃣ Suggestions for Improvement
For each suggestion:
- 📌 **Issue**: What could be improved
- 🔎 **Why it matters**: Why this change would help (in simple terms)
- ✅ **How to fix it**: Clear examples showing better alternatives

### 4️⃣ Improved Version
I'll provide a cleaner version of your code with helpful comments.

### 5️⃣ Learning Resources
Simple tips and resources to help you grow as a developer.

---

## My Style:
- I'll use **clear, simple language** without jargon
- I'll break complex ideas into **easy-to-understand points**
- I'll use **examples** to explain concepts
- I'll be **encouraging** while suggesting improvements
- I'll focus on **practical tips** you can apply right away

---

Please review this code:
""";

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String reviewCode(String code) {
        JSONObject payload = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject contentObj = new JSONObject();
        JSONArray parts = new JSONArray();

        parts.put(new JSONObject().put("text", REVIEW_PROMPT + "\n\n---\n\n```" + code + "\n```"));
        contentObj.put("parts", parts);
        contents.put(contentObj);
        payload.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = geminiApiUrl + "?key=" + geminiApiKey;

        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JSONObject responseBody = new JSONObject(response.getBody());
            JSONArray candidates = responseBody.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                if (content != null) {
                    JSONArray reviewParts = content.optJSONArray("parts");
                    if (reviewParts != null && reviewParts.length() > 0) {
                        return reviewParts.getJSONObject(0).optString("text", "No review generated.");
                    }
                }
            }
            return "No review generated.";
        } catch (RestClientException ex) {
            ex.printStackTrace();
            return "Failed to connect to Gemini API.";
        }
    }
}
