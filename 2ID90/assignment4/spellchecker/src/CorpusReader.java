
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CorpusReader {

    final static String CNTFILE_LOC = "samplecnt.txt";
    final static String VOCFILE_LOC = "samplevoc.txt";

    private HashMap<String, Integer> ngrams;
    private Set<String> vocabulary;
    private ArrayList<Integer> freqCount = new ArrayList();

    public CorpusReader() throws IOException {
        readNGrams();
        readVocabulary();
        createSmoothedCount(ngrams);
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

//    private void createSmoothedCount(HashMap<String, Integer> nGrams) {
//        Integer count = 0;
//        Object[] smoothNGrams = nGrams.entrySet().toArray();
//        Arrays.sort(smoothNGrams, new Comparator() {
//            public int compare(Object s1, Object s2) {
//                return ((Map.Entry<String, Integer>) s1).getValue().compareTo(
//                        ((Map.Entry<String, Integer>) s2).getValue());
//            }
//        });
//        
//        
//        for (Object e : smoothNGrams) {
//            while(((Map.Entry<String, Integer>) e).getValue() == 1){
//                count++;
//            }
//            System.out.println(((Map.Entry<String, Integer>) e).getValue());
//            
//        }
//    }
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

    public double getSmoothedCount(String nGram) {
        if (nGram == null || nGram.length() == 0) {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }

        for (Integer freq : ngrams.values()) {

        }
        double smoothedCount = 0.0;

        /**
         * OK *
         */
        return smoothedCount;
    }
}
