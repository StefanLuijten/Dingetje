
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class SpellCorrector {

    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;

    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();

    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) {
        this.cr = cr;
        this.cmr = cmr;

        // System.out.println(this.calculateChannelModelProbability("ha\'t", "hat"));
        System.out.println("correctPhrase:" + correctPhrase("the garden at hame"));
    }

    public String correctPhrase(String phrase) {
        if (phrase == null || phrase.length() == 0) {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
        String finalSuggestion = "";
        String[] words = phrase.split(" ");
        HashSet<String> candidates;
        HashMap<String, Double> wordChance;
        // for all words in the sentence
        for (String w : words) {
            wordChance = new HashMap<>();
            // get candidates for the word
            candidates = getCandidateWords(w);
            // for all candidates calculate change
            for (String candidate : candidates) {
                System.out.println(candidate);
                double chance = calculateChannelModelProbability(candidate, w) * cr.getSmoothedCount(candidate);
                System.out.println("Chance:"+ chance);
                System.out.println("ChannelModel:" + calculateChannelModelProbability(candidate, w));
                System.out.println("SmoothedCount:" + cr.getSmoothedCount(candidate));
                wordChance.put(candidate, chance);
            }

            Entry<String, Double> bestCandidate = null;

            for (Entry<String, Double> candidate : wordChance.entrySet()) {
                if (bestCandidate == null || candidate.getValue() > bestCandidate.getValue()) {
                    bestCandidate = candidate;
                }
            }
            finalSuggestion = finalSuggestion + " " + bestCandidate.getKey();
        }

        return finalSuggestion.trim();
    }

    public double calculateChannelModelProbability(String suggested, String incorrect) {
       
        if (suggested.equals(incorrect)) {
            return 0.80;
        } else {
            String change = findChange(suggested, incorrect);
            String[] input = change.split("\\|");
            String error = input[0];
            String correct = input[1];
            int confCount = cmr.getConfusionCount(error, correct);
            int totalCount = cr.retrieveSubStringCountVocabulary(correct);
            return (double) confCount / (double) totalCount;
        }
    }

    private String findChange(String suggested, String incorrect) {
        char[] suggestedArray = suggested.toCharArray();
        char[] incorrectArray = incorrect.toCharArray();

        // There is a deletion.
        if (suggestedArray.length < incorrectArray.length) {
            return findChangeDeletion(suggestedArray, incorrectArray);
            // There is a insertion.
        } else if (suggestedArray.length > incorrectArray.length) {
            return findChangeInsertion(suggestedArray, incorrectArray);
            // length is equal thus transposition/substitution or no change.
        } else {
            return findChangeSubstitutionTransposition(suggestedArray, incorrectArray);
        }
    }

    private String findChangeDeletion(char[] suggested, char[] incorrect) {
        String change = " | ";
        for (int i = 0; i < incorrect.length; i++) {
            // Catch the boundary if last index is removed. (Does not exist for correct array)
            if (i == incorrect.length - 1) {
                change = incorrect[i - 1] + "" + incorrect[i] + "|" + incorrect[i - 1];
                break;
            } else {
                // If characters are not equal to eachother and there has to be a deletion.
                if (suggested[i] != incorrect[i]) {
                    if (i == 0) {
                        change = incorrect[i] + "| ";
                        break;
                    } else {
                        change = incorrect[i - 1] + "" + incorrect[i] + "|" + incorrect[i - 1];
                        break;
                    }
                }
            }
        }
        return change;
    }

    private String findChangeInsertion(char[] suggested, char[] incorrect) {
        String change = " | ";
        for (int i = 0; i < suggested.length; i++) {
            // Catch the boundary if last index is the insert. (Does not exist for the incorrect array)
            if (i == suggested.length - 1) {
                change = suggested[i - 1] + "|" + suggested[i - 1] + "" + suggested[i];
                break;
            } else {
                // If characters are not equal to eachother and there has to be a insertion.
                if (incorrect[i] != suggested[i]) {
                    if (i == 0) {
                        change = " |" + suggested[i];
                        break;
                    } else {
                        change = suggested[i - 1] + "|" + suggested[i - 1] + "" + suggested[i];
                        break;
                    }
                }
            }
        }
        return change;
    }

    private String findChangeSubstitutionTransposition(char[] suggested, char[] incorrect) {
        String change = " | ";

        for (int i = 0; i < suggested.length; i++) {
            // when values not equal check if it is a transposition.
            if (suggested[i] != incorrect[i]) {
                // If last character it cannot be transposition since this is seen at the first switched character.
                if (i != suggested.length - 1) {
                    // If transposition
                    if ((suggested[i + 1] == incorrect[i]) && (suggested[i] == incorrect[i + 1])) {
                        change = incorrect[i] + "" + incorrect[i + 1] + "|" + suggested[i] + "" + suggested[i + 1];
                        break;
                    }
                }
                // substitution
                change = incorrect[i] + "|" + suggested[i];
                break;
            }
        }
        return change;
    }

    public HashSet<String> getCandidateWords(String word) {
        HashSet<String> ListOfWords = new HashSet<String>();
        ListOfWords.addAll(getCandidateWordsDeletion(word));
        ListOfWords.addAll(getCandidateWordsInsertion(word));
        ListOfWords.addAll(getCandidateWordsSubstitution(word));
        ListOfWords.addAll(getCandidateWordsTransposition(word));
        return cr.inVocabulary(ListOfWords);
    }

    private HashSet<String> getCandidateWordsInsertion(String word) {
        HashSet<String> ListOfWordsInsertion = new HashSet<String>();
        /*
         Try all insertions between each character and before/after the word.
         */
        char[] result = new char[word.length() + 1];
        char[] charArray = word.toCharArray();
        // for all characters + after the word
        for (int i = 0; i < result.length; i++) {
            //for the whole alphabet
            for (int j = 0; j < ALPHABET.length; j++) {
                // complete the word
                for (int k = 0; k < result.length; k++) {
                    // index of the insertion
                    if (i == k) {
                        result[i] = ALPHABET[j];
                        // index already inserted correct the index.
                    } else if (i < k) {
                        result[k] = charArray[k - 1];
                        // no insertion yet. So no index correction.
                    } else {
                        result[k] = charArray[k];
                    }
                }
                String candidateWord = new String(result);
                ListOfWordsInsertion.add(candidateWord);
            }
        }
        return ListOfWordsInsertion;
    }

    private HashSet<String> getCandidateWordsDeletion(String word) {
        HashSet<String> ListOfWordsDeletion = new HashSet<String>();
        /*
         Try all deletions possible.
         */
        char[] result = new char[word.length() - 1];
        char[] charArray = word.toCharArray();
        // for all characters
        for (int i = 0; i < charArray.length; i++) {
            // complete the word
            for (int k = 0; k < result.length + 1; k++) {
                // index of the deletion
                if (i == k) {
                    // index already inserted correct the index.
                } else if (i < k) {
                    result[k - 1] = charArray[k];
                    // no insertion yet. So no index correction.
                } else {
                    result[k] = charArray[k];
                }
            }
            String candidateWord = new String(result);
            ListOfWordsDeletion.add(candidateWord);
        }
        return ListOfWordsDeletion;
    }

    private HashSet<String> getCandidateWordsTransposition(String word) {
        HashSet<String> ListOfWordsTransposition = new HashSet<String>();

        char[] charArray = word.toCharArray();
        char[] result = charArray.clone();
        // for all characters
        for (int i = 0; i < charArray.length - 1; i++) {
            char original = charArray[i];
            // transpose with the n other chars in the word including the char itself.
            for (int k = 1; k < 2; k++) {
                // clone the original word array;
                result = charArray.clone();
                // do all transpositons.
                result[i] = charArray[i + k];
                result[i + k] = original;
                String candidateWord = new String(result);
                ListOfWordsTransposition.add(candidateWord);
            }
        }
        return ListOfWordsTransposition;
    }

    private HashSet<String> getCandidateWordsSubstitution(String word) {
        HashSet<String> ListOfWordsSubstitution = new HashSet<String>();
        /*
         Try all subtitution for each character.
         */
        char[] charArray = word.toCharArray();
        char[] result;
        // for all characters
        for (int i = 0; i < charArray.length; i++) {
            //for the whole alphabet
            for (int j = 0; j < ALPHABET.length; j++) {
                result = charArray.clone();
                result[i] = ALPHABET[j];

                String candidateWord = new String(result);
                ListOfWordsSubstitution.add(candidateWord);
            }
        }
        return ListOfWordsSubstitution;
    }
}
