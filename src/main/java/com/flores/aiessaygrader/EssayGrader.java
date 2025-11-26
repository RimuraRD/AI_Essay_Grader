package com.flores.aiessaygrader;

import java.util.*;
import java.text.DecimalFormat;

public class EssayGrader {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("===== AI ESSAY GRADER =====");
        System.out.print("Enter the essay topic: ");
        String topic = scanner.nextLine().toLowerCase();

        System.out.println(
                "\nPaste your essay below (press Ctrl+Z then Enter when done on Windows, Ctrl+D on Mac/Linux):");

        StringBuilder essayInput = new StringBuilder();
        while (scanner.hasNextLine()) {
            essayInput.append(scanner.nextLine()).append("\n");
        }

        String essay = essayInput.toString().trim();

        // Normalize spaces
        essay = essay.replaceAll("\\s+", " ");

        // Split into sentences
        String[] sentences = essay.split("(?<=[.!?])\\s+");
        String[] words = essay.split("\\s+");

        int wordCount = words.length;

        // ---------------------
        // 1. Grammar Checks
        // ---------------------

        int missingCapital = 0;
        int repeatedPunctuation = 0;
        int longSentences = 0;
        int shortSentences = 0;

        for (String s : sentences) {
            String trimmed = s.trim();
            if (trimmed.length() > 0 && Character.isLowerCase(trimmed.charAt(0))) {
                missingCapital++;
            }
            if (trimmed.contains("!!") || trimmed.contains("??") || trimmed.contains("...")) {
                repeatedPunctuation++;
            }

            int wc = trimmed.split("\\s+").length;
            if (wc > 30)
                longSentences++;
            if (wc < 5)
                shortSentences++;
        }

        // ---------------------
        // 2. Vocabulary Strength
        // ---------------------

        String[] advancedVocabulary = {
                "however", "although", "therefore", "consequently",
                "furthermore", "significant", "demonstrate", "illustrates",
                "complexity", "moreover"
        };

        int advancedWords = 0;
        for (String w : words) {
            String lw = w.toLowerCase().replaceAll("[^a-z]", "");
            for (String adv : advancedVocabulary) {
                if (adv.equals(lw)) {
                    advancedWords++;
                }
            }
        }

        // ---------------------
        // 3. Readability Score (Flesch)
        // ---------------------

        double syllables = countSyllables(essay);
        double readability = 206.835 - (1.015 * ((double) wordCount / sentences.length))
                - (84.6 * (syllables / wordCount));

        // ---------------------
        // 4. Topic Relevance
        // ---------------------

        int topicMatches = 0;
        for (String w : words) {
            if (w.toLowerCase().contains(topic))
                topicMatches++;
        }

        // ---------------------
        // 5. Sentence Variety
        // ---------------------

        Map<String, Integer> starters = new HashMap<>();
        for (String s : sentences) {
            String[] sw = s.trim().split("\\s+");
            if (sw.length > 0) {
                String start = sw[0].toLowerCase();
                starters.put(start, starters.getOrDefault(start, 0) + 1);
            }
        }

        int maxStart = starters.values().stream().max(Integer::compare).orElse(0);
        double dominantSentenceStartRatio = (double) maxStart / sentences.length;

        // ---------------------
        // 6. Repetition / Plagiarism
        // ---------------------

        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            String lw = w.toLowerCase().replaceAll("[^a-z]", "");
            if (lw.length() > 0) {
                freq.put(lw, freq.getOrDefault(lw, 0) + 1);
            }
        }

        int repeatedWords = 0;
        for (int c : freq.values()) {
            if (c >= 4)
                repeatedWords += c;
        }

        double repetitionRatio = (double) repeatedWords / wordCount;

        // ---------------------
        // 7. Tone Detection
        // ---------------------

        String tone;
        if (essay.matches(".*(I think|I believe|in my opinion).*")) {
            tone = "Informal / Opinionated";
        } else if (essay.matches(".*(therefore|consequently|thus|moreover).*")) {
            tone = "Formal / Academic";
        } else {
            tone = "Neutral";
        }

        // ---------------------
        // 8. Summary Generation
        // ---------------------

        String summary = generateSummary(freq);

        // =====================================================
        // STRICT SCORING SYSTEM
        // =====================================================

        int wordCountScore;
        if (wordCount < 80)
            wordCountScore = 0;
        else if (wordCount < 150)
            wordCountScore = 8;
        else if (wordCount < 250)
            wordCountScore = 12;
        else
            wordCountScore = 15;

        int grammarScore = 20;
        grammarScore -= missingCapital * 2;
        grammarScore -= repeatedPunctuation * 2;
        grammarScore -= longSentences;
        grammarScore -= shortSentences;
        if (grammarScore < 0)
            grammarScore = 0;

        int vocabScore;
        if (advancedWords < 3)
            vocabScore = 5;
        else if (advancedWords < 6)
            vocabScore = 10;
        else
            vocabScore = 15;

        int readabilityScore;
        if (readability >= 90)
            readabilityScore = 5;
        else if (readability >= 70)
            readabilityScore = 8;
        else if (readability >= 60)
            readabilityScore = 12;
        else if (readability >= 50)
            readabilityScore = 17;
        else
            readabilityScore = 20;

        int topicScore = (topicMatches >= 5) ? 10 : 0;

        int sentenceVarietyScore = 10;
        if (dominantSentenceStartRatio > 0.40)
            sentenceVarietyScore -= 5;

        int plagiarismScore = (repetitionRatio > 0.10) ? 0 : 10;

        int toneScore = 5;

        double totalScore = wordCountScore + grammarScore + vocabScore + readabilityScore
                + topicScore + sentenceVarietyScore + plagiarismScore + toneScore;

        DecimalFormat df = new DecimalFormat("#.##");

        // ---------------------
        // OUTPUT RESULTS
        // ---------------------

        System.out.println("\n========== RESULTS ==========");
        System.out.println("Word Count: " + wordCount);
        System.out.println("Readability: " + df.format(readability));
        System.out.println("Tone Detected: " + tone);
        System.out.println("\nSentence Variety Ratio: " + df.format(dominantSentenceStartRatio));
        System.out.println("Repetition Ratio: " + df.format(repetitionRatio));

        System.out.println("\n------- SCORE BREAKDOWN -------");
        System.out.println("Word Count Score: " + wordCountScore + "/15");
        System.out.println("Grammar Score: " + grammarScore + "/20");
        System.out.println("Vocabulary Score: " + vocabScore + "/15");
        System.out.println("Readability Score: " + readabilityScore + "/20");
        System.out.println("Topic Relevance: " + topicScore + "/10");
        System.out.println("Sentence Variety: " + sentenceVarietyScore + "/10");
        System.out.println("Repetition / Plagiarism: " + plagiarismScore + "/10");
        System.out.println("Tone Score: " + toneScore + "/5");

        System.out.println("\nFINAL SCORE: " + df.format(totalScore) + "/100");

        System.out.println("\n------- AUTO SUMMARY -------");
        System.out.println(summary);
    }

    // =====================================================
    // SYLLABLE COUNTER
    // =====================================================
    public static int countSyllables(String text) {
        int syllables = 0;
        String[] words = text.toLowerCase().split("\\s+");

        for (String word : words) {
            word = word.replaceAll("[^a-z]", "");
            if (word.isEmpty())
                continue;

            int count = 0;
            boolean prevVowel = false;
            for (char c : word.toCharArray()) {
                boolean vowel = "aeiou".indexOf(c) != -1;
                if (vowel && !prevVowel)
                    count++;
                prevVowel = vowel;
            }
            if (word.endsWith("e"))
                count--;
            if (count < 1)
                count = 1;

            syllables += count;
        }
        return syllables;
    }

    // =====================================================
    // SUMMARY GENERATOR
    // =====================================================
    public static String generateSummary(Map<String, Integer> freq) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(freq.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());

        StringBuilder sb = new StringBuilder("Summary: The essay discusses ");

        int limit = Math.min(5, list.size());
        for (int i = 0; i < limit; i++) {
            sb.append(list.get(i).getKey());
            if (i < limit - 1)
                sb.append(", ");
        }
        sb.append(".");

        return sb.toString();
    }
}
