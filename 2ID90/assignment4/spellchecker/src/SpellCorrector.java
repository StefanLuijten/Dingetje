
import java.util.HashSet;

public class SpellCorrector {

    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;

    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();

    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) {
        this.cr = cr;
        this.cmr = cmr;
        getCandidateWords("test");
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
        /**
         * CODE TO BE ADDED *
         */

        return 0.0;
    }

    public HashSet<String> getCandidateWords(String word) {
        HashSet<String> ListOfWords = new HashSet<String>();

        /*
         Try all insertions between each character and before/after the word.
         */
        char[] result = new char[word.length() + 1];
        char[] charArray = word.toCharArray();
        // for all characters + after the word
        for (int i = 0; i < result.length; i++) {
            //for the whole alpahbet
            for (int j = 0; j < ALPHABET.length - 1; j++) {
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
                ListOfWords.add(candidateWord);
            }
        }
        return cr.inVocabulary(ListOfWords);
    }
}
