package org.datpham.foodlink.service.impl;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.datpham.foodlink.service.GenaiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GenaiServiceImpl implements GenaiService {

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    @Override
    public String testGenai(String inputArg) {
        if (!StringUtils.hasText(geminiApiKey)) {
            throw new IllegalStateException("Missing GEMINI_API_KEY in environment");
        }

        String prompt = StringUtils.hasText(inputArg) ? inputArg : "How does AI work?";
        Client client = Client.builder().apiKey(geminiApiKey).build();

        GenerateContentResponse response =
                client.models.generateContent("gemini-2.5-flash", prompt, null);

        return response.text();
    }
}
