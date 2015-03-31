import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CorpusReader {

    final static String CNTFILE_LOC = "samplecnt.txt";
    final static String VOCFILE_LOC = "samplevoc.txt";

    private HashMap<String, Integer> ngrams;
    private Set<String> vocabulary;
    private ArrayList<Integer> freqCount = new ArrayList();
    private ArrayList<IntPair> consecZeroes = new ArrayList();

    //IntPair for use in SGT
    // Class used to keep track of pairs of Ints. This was meant to be used 
    // in the Simple Good Turing Implementation to keep track of series of
    // zeroes
    class IntPair {

        final int x;
        final int y;

        IntPair(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
   
    public CorpusReader() throws IOException {
        readNGrams();
        readVocabulary();
        createSmoothedCount(ngrams);
        findZeroes();
    }

    /**
     * Returns the n-gram count of <NGram> in the file
     *
     *
     * @param nGram : space-separated list of words, e.g. "adopted by him"
     * @return 0 if <NGram> cannot be found, otherwise count of <NGram> in file
     */
    public int getNGramCount(String nGram) throws NumberFormatException {
        if (nGram == null || nGram.length() == 0) {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        Integer value = ngrams.get(nGram);
        return value == null ? 0 : value;
    }

    private void readNGrams() throws
            FileNotFoundException, IOException, NumberFormatException {
        ngrams = new HashMap<>();

        FileInputStream fis;
        fis = new FileInputStream(CNTFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        while (in.ready()) {
            String phrase = in.readLine().trim();
            String s1, s2;
            int j = phrase.indexOf(" ");

            s1 = phrase.substring(0, j);
            s2 = phrase.substring(j + 1, phrase.length());

            int count = 0;
            try {
                count = Integer.parseInt(s1);
                ngrams.put(s2, count);
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("NumberformatError: " + s1);
            }
        }
    }

    private void readVocabulary() throws FileNotFoundException, IOException {
        vocabulary = new HashSet<>();

        FileInputStream fis = new FileInputStream(VOCFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        while (in.ready()) {
            String line = in.readLine();
            vocabulary.add(line);
        }
    }

    /**
     * Returns the size of the number of unique words in the dataset
     *
     * @return the size of the number of unique words in the dataset
     */
    public int getVocabularySize() {
        return vocabulary.size();
    }

    /**
     * Returns the subset of words in set that are in the vocabulary
     *
     * @param set
     * @return
     */
    public HashSet<String> inVocabulary(Set<String> set) {
        HashSet<String> h = new HashSet<>(set);
        h.retainAll(vocabulary);
        return h;
    }

    public boolean inVocabulary(String word) {
        return vocabulary.contains(word);
    }

    /**
     * Creates an ArrayList (@freqCount) in which freqCount[r] holds the amount
     * of words which appear r times in the hashmap nGrams
     *
     * @param nGrams
     */
    private void createSmoothedCount(HashMap<String, Integer> nGrams) {
        for (int i = 0; i < 100000; i++) {
            freqCount.add(0);
        }
        for (Integer freq : nGrams.values()) {
            if (freqCount.get(freq) != 0) {
                freqCount.set(freq, freqCount.get(freq) + 1);
            } else {
                freqCount.set(freq, 1);
            }
        }
    }

    /**
     * Code intended to look for series of zeroes to be used in Simple Good
     * Turing
     */
    private void findZeroes() {
        boolean busy = false;
        int q = 0, t = 0;
        for (int i = 1; i < freqCount.size(); i++) {
            if (freqCount.get(i) == 0 && !busy) {
                busy = true;
                q = i;
            } else if (freqCount.get(i) != 0 && busy) {
                t = i;
                busy = false;
                if (t - q != 1) {
                    consecZeroes.add(new IntPair(q, t));
                }
            } else if (i == freqCount.size() - 1) {
                t = q + (consecZeroes.get(consecZeroes.size() - 1).y - consecZeroes.get(consecZeroes.size() - 1).x);
                if (t - q != 1) {
                    consecZeroes.add(new IntPair(q, t));
                }
            }
        }
    }

    public double getSmoothedCount(String nGram) {
        if (nGram == null || nGram.length() == 0) {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
 
        double smoothedCount = 0.0;
        if (ngrams.containsKey(nGram)) {
            int freq = ngrams.get(nGram);
            double q = 0, t = 0;
            if (freq >= 0.005 * freqCount.size()) {
                for (IntPair consec : consecZeroes) {
                    if (q == 0 || t == 0) {
                        if (freq == consec.y) {
                            q = consec.x - 1;
                        } else if (freq == consec.x - 1) {
                            t = consec.y;
                        }
                    }
                }
                smoothedCount = (freqCount.get(freq) / (0.4 * (t - q)));
            } else {
                smoothedCount = (((double) freq + 1) * ((double) freqCount.get(freq + 1) / ((double) freqCount.get(freq))));
                smoothedCount /= ngrams.size();
            }
        } else {
            smoothedCount = ((double) freqCount.get(1) / (double) ngrams.size());
        }

        return smoothedCount;
    }

    public Integer retrieveSubStringCountVocabulary(String subString) {
        int count = 0;
        Pattern pattern = Pattern.compile(subString);
        for (String s : vocabulary) {
            Matcher matcher = pattern.matcher(s);
            while (matcher.find()) {
                count++;
            }
        }
        return count;
    }

}
