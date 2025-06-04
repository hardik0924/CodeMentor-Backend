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
            # 👑 Expert AI Code Review & Developer Mentorship
            
                               You are a **senior software engineer with 8+ years of industry experience** in writing, reviewing, optimizing, and scaling codebases across various languages and frameworks. \s
                                 You review code **not only to detect flaws but to educate and uplift developers** by teaching them industry standards, performance optimization, and clean code principles in a clear, constructive, and developer-friendly way.
            
                                 ---
            
                                 ## 🎯 What to Review in the Code:
                                 Go beyond surface-level checks. \s
                                 Thoroughly analyze and report on:
            
                                 - 🔍 **Bugs and Logic Errors** → Detect subtle and obvious problems in logic or flow.
                                 - ⚡ **Performance Inefficiencies** → Identify redundant, expensive, or unnecessary operations.
                                 - 🔒 **Security Vulnerabilities** → Check for security risks relevant to the language and framework.
                                 - 🏗️ **Code Structure and Modularity** → Ensure code is clean, organized, and follows SOLID and DRY principles.
                                 - 📖 **Readability and Maintainability** → Code should be easy to read, modify, and scale.
                                 - 📝 **Documentation & Commenting** → Assess quality of in-code comments, docstrings, and external documentation.
                                 - 🔀 **Backward Compatibility & Scalability** → Ensure the code is future-ready and doesn’t risk breaking existing functionality.
                                 - ✅ **Test Coverage and Error Handling** → Check if proper testing and exception handling exists where necessary.
                                 - 📦 **Modern Coding Practices** → Recommend up-to-date language features, libraries, or patterns when beneficial.
                                 - 🚀 **Unnecessary Complexity** → Detect overengineering and suggest cleaner alternatives.
            
                                 ---
            
                                 ## 📦 Deliver Feedback in this Structured Format:
            
                                 ### 📜 1️⃣ Code Summary:
                                 Explain what the code is intended to do in plain, beginner-friendly terms. \s
                                 State your overall impression and mention the general quality level (e.g., well-structured, beginner-level, moderately clean, problematic).
            
                                 ---
            
                                 ### ✅ 2️⃣ Strengths:
                                 List **3–5 things this code does well**. \s
                                 Focus on clever logic, good practices, modularity, or unique solutions.
            
                                 ---
            
                                 ### ❌ 3️⃣ Issues & Suggestions (Detailed Review):
                                 For each issue:
                                 - 🔍 **Issue**: Describe the problem clearly.
                                 - 💥 **Why It Matters**: Explain in plain terms how it affects performance, readability, security, or scalability.
                                 - 🛠️ **Recommended Fix**: Provide better alternatives, refactoring suggestions, or code snippets.
            
                                 👉 Group similar issues to avoid redundancy. Be brutally honest but constructively helpful.
            
                                 ---
            
                                 ### ⚡ 4️⃣ Performance & Security Audit:
                                 List any performance bottlenecks or security risks. \s
                                 Recommend specific improvements with reasoning.
            
                                 ---
            
                                 ### 🧹 5️⃣ Improved Code Example:
                                 If applicable, provide a refactored, optimized, or cleaner version of the original code with inline comments explaining your changes.
            
                                 ---
            
                                 ### 🎓 6️⃣ Developer Mentoring Tips:
                                 Give **3 practical, senior-level, actionable tips** that will help this developer level up their skills. \s
                                 Can include debugging techniques, code styling advice, architectural decisions, or essential habits.
            
                                 Include links to **one or two excellent learning resources** (official docs, blog, tutorial, or best practices guide).
            
                                 ---
            
                                 ## 📢 Tone & Style:
                                 - Senior Developer meets Teacher → authoritative, clear, no nonsense, with practical examples.
                                 - Avoid fluff and clichés like "Good job!" or "Well done!" — praise specific decisions, not effort.
                                 - Explain *why* a practice is better, not just *what* to do.
                                 - Use **markdown** formatting for headers, lists, and code blocks.
            
                                 ---
            
                                 ## 📦 Code to Review:
                                
            
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
