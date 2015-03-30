
import java.util.HashMap;
import java.util.HashSet;

public class SpellCorrector {

    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;

    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();
    
    HashMap<String, String> ListOfWords;

    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) {
        this.cr = cr;
        this.cmr = cmr;
        System.out.println(this.getCandidateWords("trem"));
    }

    public String correctPhrase(String phrase) {
        if (phrase == null || phrase.length() == 0) {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }

        String[] words = phrase.split(" ");
        String finalSuggestion = "";

        /**
         * CODE TO BE ADDED *
         */
        return finalSuggestion.trim();
    }

    public double calculateChannelModelProbability(String suggested, String incorrect) {
       for int
        return 0.0;
    }

    public HashSet<String> getCandidateWords(String word) {
        ListOfWords = new HashMap<>();
        ListOfWords.putAll(getCandidateWordsDeletion(word));
        ListOfWords.putAll(getCandidateWordsInsertion(word));
        ListOfWords.putAll(getCandidateWordsSubstitution(word));
        ListOfWords.putAll(getCandidateWordsTransposition(word));
        HashSet<String> suggestedWords = new HashSet<String>(ListOfWords.keySet());
        return cr.inVocabulary(suggestedWords);
    }

    private HashMap<String, String> getCandidateWordsInsertion(String word) {
        HashMap<String, String> ListOfWordsInsertion = new HashMap<>();
        String change = "";
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
                        if (i == 0) {
                            change = " |" + ALPHABET[j];
                        } else {
                            change = result[i - 1] + "|" + result[i - 1] + "" + ALPHABET[j];
                        }
                        // index already inserted correct the index.
                    } else if (i < k) {
                        result[k] = charArray[k - 1];
                        // no insertion yet. So no index correction.
                    } else {
                        result[k] = charArray[k];
                    }
                }
                String candidateWord = new String(result);
                ListOfWordsInsertion.put(candidateWord, change);
            }
        }
        return ListOfWordsInsertion;
    }

    private HashMap<String, String> getCandidateWordsDeletion(String word) {
        HashMap<String, String> ListOfWordsDeletion = new HashMap<String, String>();
        String change = "";
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
                    if (i == 0) {
                        change = charArray[i] + "| ";
                    } else {
                        change = charArray[i - 1] + "" + charArray[i] + "|" + charArray[i];
                    }
                    // index already inserted correct the index.
                } else if (i < k) {
                    result[k - 1] = charArray[k];
                    // no insertion yet. So no index correction.
                } else {
                    result[k] = charArray[k];
                }
            }
            String candidateWord = new String(result);
            ListOfWordsDeletion.put(candidateWord, change);
        }
        return ListOfWordsDeletion;
    }

    private HashMap<String, String> getCandidateWordsTransposition(String word) {
        HashMap<String, String> ListOfWordsTransposition = new HashMap<String, String>();

        String change;
        char[] charArray = word.toCharArray();
        char[] result;
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
                change = result[i] + "" + result[i + k] + "|" + result[i + k] + "" + result[i];
                String candidateWord = new String(result);
                ListOfWordsTransposition.put(candidateWord, change);
            }
        }
        return ListOfWordsTransposition;
    }

    private HashMap<String, String> getCandidateWordsSubstitution(String word) {
        HashMap<String, String> ListOfWordsSubstitution = new HashMap<String, String>();
        String change = "";
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
                change = result[i] + "|" + ALPHABET[j];
                String candidateWord = new String(result);
                ListOfWordsSubstitution.put(candidateWord, change);
            }
        }
        return ListOfWordsSubstitution;
    }
}
