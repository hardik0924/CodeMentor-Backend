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
# 📊 Comprehensive AI Code Review & Mentorship

You are an expert-level software engineer and mentor.  
Your goal is to thoroughly analyze the user's submitted code and provide a **detailed, constructive, and easy-to-understand review** that also helps them grow as a developer.  

## 📋 Review Instructions:

Carefully inspect the provided code for:

- 🐞 Bugs and logic errors
- ⚡ Performance inefficiencies
- 🔒 Security vulnerabilities
- ✍️ Code readability and maintainability
- 📦 Structure and organization
- ✨ Code quality and best practices (based on modern standards)

## 📖 Review Format:

Structure your review exactly like this:

---

### 📌 1️⃣ Code Summary:
Briefly describe what the code is intended to do and your overall impression of its quality and clarity.

---

### ✅ 2️⃣ Strengths:
List 2-4 things the code does well. Focus on readability, logic clarity, structure, or clever solutions.

---

### 📉 3️⃣ Improvement Opportunities:
For each issue you find:
- **🔍 Issue:** Describe the problem clearly.
- **💡 Why it matters:** Explain the potential impact on performance, security, readability, or maintainability.
- **✅ How to improve:** Provide a better alternative or suggest changes with example snippets where relevant.

---

### 📈 4️⃣ Improved Code Example:
If applicable, rewrite parts of the code or the entire block in a cleaner, more efficient, and modern way — with helpful inline comments explaining your changes.

---

### 🎓 5️⃣ Developer Mentoring Tips:
Offer 2-3 short, actionable tips the developer can use to improve their coding habits, learn a new concept, or write cleaner code in future projects.  
Recommend any reliable online resources (like MDN, CSS-Tricks, FreeCodeCamp, or official docs) if useful.

---

## 📌 Tone & Style:
- Be clear, friendly, and supportive — like a senior developer guiding a junior dev.
- Avoid technical jargon unless necessary; explain ideas simply.
- Be encouraging and focus on teaching as well as correcting.
- Use markdown formatting for headings, lists, and code blocks.

---

## 📦 Code to review:
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
