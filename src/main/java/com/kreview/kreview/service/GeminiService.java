package com.kreview.kreview.service;

import com.kreview.kreview.AnalysisResult;
import com.kreview.kreview.Clause;
import com.kreview.kreview.Contract;
import com.kreview.kreview.ContractType;
import com.kreview.kreview.PartyRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

@Service
public class GeminiService {

  private final WebClient webClient;
  private final String apiKey;

  public GeminiService(@Value("${gemini.api.key}") String apiKey) {
    this.apiKey = apiKey;
    this.webClient = WebClient.builder()
        .baseUrl("https://generativelanguage.googleapis.com")
        .build();
  }

  public String askGemini(String prompt) {
    ObjectMapper mapper = new ObjectMapper();
    String requestBody;
    try {
      requestBody = mapper.writeValueAsString(
          java.util.Map.of(
              "contents", java.util.List.of(
                  java.util.Map.of(
                      "parts", java.util.List.of(
                          java.util.Map.of("text", prompt)
                      )
                  )
              )
          )
      );
    } catch (Exception e) {
      return "Error building request: " + e.getMessage();
    }

    int maxRetries = 3;
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        String response = webClient.post()
            .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(30))
            .block();

        String text = extractText(response);
        System.out.println("=== GEMINI RESPONSE (attempt " + attempt + ") ===");
        System.out.println(text);
        System.out.println("=== END GEMINI ===");
        return text;
      } catch (Exception e) {
        System.out.println("Attempt " + attempt + " failed: " + e.getMessage());
        if (attempt < maxRetries) {
          try {
            Thread.sleep(2000 * attempt);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
          }
        } else {
          throw e;
        }
      }
    }
    return "Error: all retries failed";
  }

  private String extractText(String response) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response);
      return root.path("candidates")
          .get(0)
          .path("content")
          .path("parts")
          .get(0)
          .path("text")
          .asText();
    } catch (Exception e) {
      return "Error parsing response: " + e.getMessage();
    }
  }

  private String cleanJsonResponse(String response) {
    String cleaned = response.trim();
    if (cleaned.startsWith("```json")) {
      cleaned = cleaned.substring(7);
    } else if (cleaned.startsWith("```")) {
      cleaned = cleaned.substring(3);
    }
    if (cleaned.endsWith("```")) {
      cleaned = cleaned.substring(0, cleaned.length() - 3);
    }
    return cleaned.trim();
  }

  public AnalysisResult parseAnalysisResponse(String jsonResponse, Contract contract) {
    ObjectMapper mapper = new ObjectMapper();
    String cleaned = cleanJsonResponse(jsonResponse);

    try {
      JsonNode root = mapper.readTree(cleaned);

      List<Clause> clauses = new ArrayList<>();
      for (JsonNode clauseNode : root.path("clauses")) {
        Clause clause = new Clause();
        clause.setType(clauseNode.path("type").asText("Unknown"));
        clause.setText(clauseNode.path("text").asText(""));

        String risk = clauseNode.path("riskLevel").asText("MEDIUM").toUpperCase();
        if (!risk.equals("LOW") && !risk.equals("MEDIUM") && !risk.equals("HIGH")) {
          risk = "MEDIUM";
        }
        clause.setRiskLevel(risk);
        clauses.add(clause);
      }

      AnalysisResult result = new AnalysisResult();
      result.setContract(contract);
      result.setClauses(clauses);
      result.setSummary(root.path("summary").asText("No summary available."));

      int score = root.path("overallRiskScore").asInt(5);
      result.setOverallRiskScore(Math.max(1, Math.min(10, score)));

      return result;
    } catch (Exception e) {
      System.out.println("=== PARSE FAILED ===");
      System.out.println("Error: " + e.getMessage());
      System.out.println("Raw JSON was: " + cleaned);
      System.out.println("=== END PARSE ERROR ===");
      throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
    }
  }

  public String analyzeContractWithAI(String contractText, ContractType contractType,
                                       PartyRole partyRole, String partyRoleCustomLabel) {
    String perspective = buildPerspectiveInstruction(contractType, partyRole, partyRoleCustomLabel);

    String prompt = """
        You are a contract analysis expert. %sAnalyze the following contract text and extract all important clauses.

        For each clause, identify:
        1. type: the category (e.g., Confidentiality, Termination, Governing Law, Liability, Payment, Duration, Intellectual Property, Non-Compete, Indemnification, Dispute Resolution)
        2. text: the exact sentence from the contract
        3. riskLevel: LOW, MEDIUM, or HIGH based on how potentially risky the clause is for the receiving party

        Also provide:
        - summary: a 1-2 sentence plain-language summary of the entire contract
        - overallRiskScore: a number from 1-10 representing overall contract risk

        Respond ONLY with valid JSON in exactly this format, no other text:
        {
            "clauses": [
                {"type": "...", "text": "...", "riskLevel": "..."}
            ],
            "summary": "...",
            "overallRiskScore": 0
        }

        Contract text:
        %s
        """.formatted(perspective, contractText);

    return askGemini(prompt);
  }

  private String buildPerspectiveInstruction(ContractType contractType, PartyRole partyRole,
                                              String partyRoleCustomLabel) {
    if (contractType == null) {
      if (partyRole == null) {
        return "";
      }
      String partyDescription = switch (partyRole) {
        case PARTY_A -> "the first party named or introduced in the contract";
        case PARTY_B -> "the second party named or introduced in the contract";
        case PARTY_C -> "the third party named or introduced in the contract";
        case OTHER -> "the party referred to as \"" + partyRoleCustomLabel + "\" in the contract";
        default -> throw new IllegalStateException(
            "Party role " + partyRole + " is not valid for a generic contract");
      };
      return "This is a general contract with no specific type assigned, so this analysis is "
          + "necessarily less targeted than a Lease, Employment, or Sales analysis would be. "
          + "Analyze it from the perspective of " + partyDescription
          + ", highlighting the risks and obligations most relevant to that party. ";
    }

    return switch (contractType) {
      case LEASE -> "This is a Lease agreement. " + leasePerspective(partyRole);
      case EMPLOYMENT -> "This is an Employment contract. " + employmentPerspective(partyRole);
      case SALES -> "This is a Sales contract. " + salesPerspective(partyRole);
    };
  }

  private String leasePerspective(PartyRole partyRole) {
    return switch (partyRole) {
      case TENANT -> "Analyze it specifically as the Tenant, focusing on rent increases, "
          + "security deposit terms, maintenance/repair responsibilities, early termination "
          + "penalties, and any hidden fees. ";
      case LANDLORD -> "Analyze it specifically as the Landlord, focusing on payment default "
          + "remedies, property damage liability, subletting restrictions, and "
          + "eviction/termination rights. ";
      default -> throw new IllegalStateException(
          "Party role " + partyRole + " is not valid for a LEASE contract");
    };
  }

  private String employmentPerspective(PartyRole partyRole) {
    return switch (partyRole) {
      case EMPLOYEE -> "Analyze it specifically as the Employee, focusing on compensation and "
          + "benefits clarity, termination conditions, non-compete and confidentiality "
          + "restrictions, and intellectual property assignment terms. ";
      case EMPLOYER -> "Analyze it specifically as the Employer, focusing on enforceability of "
          + "restrictive covenants, liability exposure, termination-for-cause protections, and "
          + "confidentiality/IP protections. ";
      default -> throw new IllegalStateException(
          "Party role " + partyRole + " is not valid for an EMPLOYMENT contract");
    };
  }

  private String salesPerspective(PartyRole partyRole) {
    return switch (partyRole) {
      case BUYER -> "Analyze it specifically as the Buyer, focusing on warranty and guarantee "
          + "terms, delivery obligations, return/refund conditions, and liability for defects. ";
      case SELLER -> "Analyze it specifically as the Seller, focusing on payment terms and "
          + "default remedies, limitation of liability, and delivery/risk-of-loss allocation. ";
      default -> throw new IllegalStateException(
          "Party role " + partyRole + " is not valid for a SALES contract");
    };
  }
}