
import java.util.HashSet;

public class SpellCorrector {

    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;

    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();

    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) {
        this.cr = cr;
        this.cmr = cmr;
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
        for (int i = 0; i < charArray.length; i++) {
            char original = charArray[i];
            // transpose with the n other chars in the word including the char itself.
            for (int k = 0; k < charArray.length; k++) {
                // clone the original word array;
                result = charArray.clone();
                // do all transpositons.
                result[i] = charArray[k];
                result[k] = original;
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
