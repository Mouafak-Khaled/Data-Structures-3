package code;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HashBasedWordCO {

	HashMapDH<String, HashCounter<String>> coMat;
	HashSet<String> words;
	int windowSize;

	String[] ignoreList;
	int minWordLen;

	public HashBasedWordCO() {
		this(5);
	}

	public HashBasedWordCO(int winSize) {
		setWindowSize(winSize);
		setIgnoredWords(new String[0]);
		setMinimumWordLength(0);
	}

	public void setIgnoredWords(String[] ignoredWords) {
		ignoreList = ignoredWords;

	}

	public void setWindowSize(int winSize) {
		windowSize = winSize;
	}

	public void setMinimumWordLength(int minLen) {
		minWordLen = minLen;
	}

	public List<String> splitSentences(String text) {
		HashSet<Character> lineEnders = new HashSet<Character>();
		lineEnders.put('.');
		lineEnders.put('?');
		lineEnders.put('!');
		return splitSentences(text, lineEnders);
	}

	public List<String> splitSentences(String text, HashSet<Character> lineEnders) {
		ArrayList<String> sentences = new ArrayList<String>();
		int i, preI = 0, n = text.length();
		for (i = 0; i < n; i++) {
			char c = text.charAt(i);
			if (lineEnders.contains(c)) {
				if (i > 0 && i < n - 1) {

					if (Character.isDigit(text.charAt(i - 1)) && Character.isDigit(text.charAt(i + 1))) {
						continue;
					} else {
						sentences.add(text.substring(preI, i).trim().replaceAll("[^A-Za-z0-9 ]+", "").toLowerCase());
						preI = i + 1;
					}
				}
			}
		}
		if (preI < i)
			sentences.add(text.substring(preI, i).trim().replaceAll("[^A-Za-z0-9 ]+", "").toLowerCase());
		return sentences;
	}

	public boolean isIgnoredWord(String str) {

		for (int i = 0; i < ignoreList.length; i++) {

			if (ignoreList[i].equals(str))
				return true;
		}

		return false;
	}

	// Returns the set of unique words, apart from the given ignored ones, given in
	// the text with word length constraints
	public HashSet<String> getUniqueWords(String text) {
		HashSet<String> uWords = new HashSet<String>();
		List<String> sentences = splitSentences(text);

		for (String sentence : sentences) {
			for (String word : sentence.split("\\s+")) {
				// TODO: Fill here
				// Make sure to NOT include any ignored words and words that are shorter than
				// the minWordLen
				if (word.length() >= minWordLen && !isIgnoredWord(word))
					uWords.put(word.toLowerCase());

			}
		}
		return uWords;
	}

	public void fillCoMat(String text) {
		fillCoMat(text, null);
	}

	/*
	 * Co-occurence definition is based on how close each word is to each other in a
	 * given sentence. Sentences are split automatically for you in this assignment
	 * so you just need to count how many times two words appear within the same
	 * window. The windows are formed by putting their center to a word and taking w
	 * number of words on each side. For example given [A B C D E F], a window with
	 * w = 1 centered on C is, [B C D]. If the edges of the window are at the limit,
	 * we just truncate. E.g. for A, the same sized window is [A B] and for F it is
	 * [E F]
	 * 
	 * The below function should calculate the co-occurence values by filling the
	 * coMat object. coMat is a hashmap of String and HashCounters. The counters
	 * count the number of times the words co-occur together with the String key
	 * representing the main word. Note that coMAt needs to be symmetric
	 * 
	 * For example the sentence "once twice thrice thrice twice thrice" with w = 1
	 * should yield: once-> [once->0,twice->1,thrice->0] twice->
	 * [once->1,twice->0,thrice->3] thrice->[once->0,twice->3,thrice->2]
	 * 
	 */
	public void fillCoMat(String text, String[] keyWords) {
		if (text == null)
			return;
		coMat = new HashMapDH<String, HashCounter<String>>();

		if (keyWords != null) {
			if (keyWords.length != 0) {
				words = new HashSet<String>();
				for (String word : keyWords)
					words.put(word);
			}
		} else
			words = getUniqueWords(text);

		// Further bit of help for setting up
		for (String word : words.keySet())
			coMat.put(word, new HashCounter<String>());

		List<String> sentences = splitSentences(text);

		for (String sentence : sentences) {

			String[] newWords = sentence.split("\\s+");

			for (int i = 0; i < newWords.length; i++) {

				for (int k = i + 1; k <= i + windowSize; k++) {

					if (k >= newWords.length)
						break;

					if (coMat.get(newWords[i]) != null && coMat.get(newWords[k]) != null) {
						if ((newWords[i].length() >= minWordLen && !isIgnoredWord(newWords[i]))
								&& (newWords[k].length() >= minWordLen && !isIgnoredWord(newWords[k]))) {
							
							coMat.get(newWords[i]).increment(newWords[k]);
							coMat.get(newWords[k]).increment(newWords[i]);
							
						}
					}
				}
			}

		}

	}

	public int getCoOccurrenceValue(String word1, String word2) {
		if (coMat.get(word1) == null)
			return 0;
		else
			return coMat.get(word1).getCount(word2);
	}

	public void printMatrix() {
		if (coMat == null)
			System.out.format("Empty co-occurrence matrix!");
		System.out.format("%10s", " ");
		for (String word : words.keySet()) {
			System.out.format("%10s", word);// .print(word + " ");//
		}
		System.out.println("");

		for (String word1 : words.keySet()) {
			System.out.format("%10s", word1);// print(word1+":");
			for (String word2 : words.keySet()) {
				String tmp = "%10s";// "%" + word2.length() + "s ";
				System.out.format(tmp, Integer.toString(coMat.get(word1).getCount(word2)));// print(coMatrix[index1][index2].toString()
																							// + " ");
			}
			System.out.println("");
		}
	}

	public void printMatrixCSV(String fileName) {
		if (coMat == null) {
			System.out.format("Empty co-occurrence matrix!");
			return;
		}

		FileWriter fileWrite = null;
		try {
			fileWrite = new FileWriter(fileName);

			int i = 0;
			for (String word : words.keySet()) {
				if (i++ > 0)
					fileWrite.append(",");
				fileWrite.append(word);
			}
			fileWrite.append("\n");

			for (String word1 : words.keySet()) {
				i = 0;
				fileWrite.append(word1 + ",");
				for (String word2 : words.keySet()) {
					if (i++ > 0)
						fileWrite.append(",");
					fileWrite.append(Integer.toString(coMat.get(word1).getCount(word2)));
				}
				fileWrite.append("\n");
			}

			fileWrite.close();
		} catch (IOException e) {
			System.out.println("Error Writing CSV file: " + fileName);
			System.exit(0);
		}
	}

}
