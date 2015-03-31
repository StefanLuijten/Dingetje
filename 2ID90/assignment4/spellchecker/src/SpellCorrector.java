
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class SpellCorrector {

    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;
    // FINAL for finetuning.
    final private double LAMBDA = 2.0;
    // FINAL representing that we think that 95% of the words are typed correctly.
    final private double NO_ERROR = 0.95;
    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();

    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) {
        this.cr = cr;
        this.cmr = cmr;
    }

    public String correctPhrase(String phrase) {
        if (phrase == null || phrase.length() == 0) {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
        String finalSuggestion = "";
        String[] words = phrase.split(" ");
        // All candidate corrections
        HashSet<String> candidates;
        // Set of candidate words and chance that they belong in the sentence.
        HashMap<String, Double> wordChance;
        double chance = 0.0;
        // for all words in the sentence
        for (int i = 0; i < words.length; i++) {
            wordChance = new HashMap<>();
            // get candidates for the word
            candidates = getCandidateWords(words[i]);
            // for all candidates calculate chance
            for (String candidate : candidates) {
                // check if it is not the first or last word in the sentence
                if (i != 0 && i != words.length - 1) {
                    //calculate probability that the candidate word comes after the previous word. 
                    double bothOccurBefore = cr.getSmoothedCount(words[i - 1] + " " + candidate);
                    double lastOccursBefore = cr.getSmoothedCount(words[i - 1]);
                    double conditProbBefore = bothOccurBefore / lastOccursBefore;

                    //calculate probability that the candidate word is followed by the next word already present in the sentence.
                    double bothOccurAfter = cr.getSmoothedCount(candidate + " " + words[i + 1]);
                    double lastOccursAfter = cr.getSmoothedCount(candidate);
                    double conditProbAfter = bothOccurAfter / lastOccursAfter;

                    // calculate the probability that this is the right word using the levhenstein distance max 1 with the confision matrix
                    // the two metrics above and the smoothed count for the unigram.
                    chance = calculateChannelModelProbability(candidate, words[i]) * Math.max(conditProbAfter , conditProbBefore)* cr.getSmoothedCount(candidate)/LAMBDA;

                    // last word which checks only the bigram with the word before
                } else if (i == words.length - 1) {
                    double bothOccurBefore = cr.getSmoothedCount(words[i - 1] + " " + candidate);
                    double lastOccursBefore = cr.getSmoothedCount(words[i - 1]);
                    double conditProbBefore = bothOccurBefore / lastOccursBefore;

                    chance = calculateChannelModelProbability(candidate, words[i]) * conditProbBefore * cr.getSmoothedCount(candidate) /LAMBDA;

                    // first word which checks only the bigram with the word after it.
                } else {
                    double bothOccur = cr.getSmoothedCount(candidate + " " + words[i + 1]);
                    double lastOccurs = cr.getSmoothedCount(candidate);
                    double conditProb = bothOccur / lastOccurs;
                    chance = calculateChannelModelProbability(candidate, words[i]) * conditProb * cr.getSmoothedCount(candidate) /LAMBDA;
                }
                // put candidates with the probability into a hashmap
                wordChance.put(candidate, chance);
            }

            // get candidate with highest probability of corectness.
            Entry<String, Double> bestCandidate = null;
            for (Entry<String, Double> candidate : wordChance.entrySet()) {
                if (bestCandidate == null || candidate.getValue() > bestCandidate.getValue()) {
                    bestCandidate = candidate;
                }
            }
            // add to the final sentence.
            finalSuggestion = finalSuggestion + " " + bestCandidate.getKey();
        }

        return finalSuggestion.trim();
    }

    public double calculateChannelModelProbability(String suggested, String incorrect) {
        // if equal than give the NO_ERROR value.
        if (suggested.equals(incorrect)) {
            return NO_ERROR;
        } else {
            String change = findChange(suggested, incorrect);
            String[] input = change.split("\\|");
            String error = input[0];
            String correct = input[1];
            // gets number of times the mistake has been made. 
            int confCount = cmr.getConfusionCount(error, correct);
            // gets number of times the substring occurs in the vocabulary.
            int totalCount = cr.retrieveSubStringCountVocabulary(correct);
            //avoid Infinity
            if (totalCount == 0) {
                totalCount = 1;
            }
            return (double) confCount / (double) totalCount;
        }
    }

    /**
     * Finds the change that is made between the incorrect and suggested string.
     *
     * @param suggested
     * @param incorrect
     * @return the change in format incorrect|correct
     */
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

    /**
     * Finds the change that is made using a delete action.
     *
     * @param suggested
     * @param incorrect
     * @return the change in format incorrect|correct
     */
    private String findChangeDeletion(char[] suggested, char[] incorrect) {
        String change = " | ";
        // for the whole character array
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

    /**
     * Finds the change that is made using an insertion action.
     *
     * @param suggested
     * @param incorrect
     * @return the change in format incorrect|correct
     */
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

    /**
     * Finds the change that is made using a transposition or substitution.
     *
     * @param suggested
     * @param incorrect
     * @return the change in format incorrect|correct
     */
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

    /**
     * Searches all candidate words that are within a Damauru-Levhenstein
     * distance of 1 and are present in the vocabulary.
     *
     * @param word
     * @return all candidates
     */
    public HashSet<String> getCandidateWords(String word) {
        HashSet<String> ListOfWords = new HashSet<String>();
        // All candidates found by deletion of a character
        ListOfWords.addAll(getCandidateWordsDeletion(word));
        // All candidates found by insertion of a character
        ListOfWords.addAll(getCandidateWordsInsertion(word));
        // All candidates found by substitution of a character
        ListOfWords.addAll(getCandidateWordsSubstitution(word));
        // All candidates found by transposition of a character
        ListOfWords.addAll(getCandidateWordsTransposition(word));
        //
        return cr.inVocabulary(ListOfWords);
    }

    /**
     * Gets all candidate words that can be found using insertion. Using
     * insertion of the whole ALPHABET array before and after each character.
     *
     * @param word
     * @return all candidate words.
     */
    private HashSet<String> getCandidateWordsInsertion(String word) {
        HashSet<String> ListOfWordsInsertion = new HashSet<String>();

        // create new result array. 
        char[] result = new char[word.length() + 1];
        // copy the original word to an array.
        char[] charArray = word.toCharArray();
        // for all characters
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
                // Set to string and add to HashSet.
                String candidateWord = new String(result);
                ListOfWordsInsertion.add(candidateWord);
            }
        }
        return ListOfWordsInsertion;
    }

    /**
     * Gets all candidate words that can be found using deletion. Using deletion
     * of all characters.
     *
     * @param word
     * @return all candidate words.
     */
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

    /**
     * Gets all candidate words that can be found using transposition. Using
     * transposition of all adjacent character.
     *
     * @param word
     * @return all candidates.
     */
    private HashSet<String> getCandidateWordsTransposition(String word) {
        HashSet<String> ListOfWordsTransposition = new HashSet<String>();

        char[] charArray = word.toCharArray();
        char[] result = charArray.clone();
        // for all characters
        for (int i = 0; i < charArray.length - 1; i++) {
            char original = charArray[i];
            // transpose with the next one char in the word including the char itself.
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

    /**
     * Gets all candidate words that can be found using substitution. Substitution is done on
     * all chars with character of the ALPHABET variable.
     * @param word
     * @return all candidates.
     */
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
