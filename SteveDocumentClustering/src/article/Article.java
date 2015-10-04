
package article;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Article {

	String docId;
	String source;
	String url;
	String title;
	String summary;
	String text;  /* long contents */


	public Article(String id, String src,
			String url, String title, String summary, String text) {
		this.docId = id;
		this.source = src;
		this.url = url;
		this.title = title;
		this.summary = summary;
		this.text = text;

	}
	
	
	
	
	
	
	public List<String> getTextWords() {
		String[] arrayOfWords = text.split("[\\s\\p{Punct}“”‘’\\']+");
		return Arrays.asList(arrayOfWords);
	}
	
	public ArrayList<NGram> getNGrams(int N) {
		List<String> listOfWords = getTextWords();
		ArrayList<NGram> listOfNGrams = new ArrayList<NGram>();
		for (int idx = 0; idx <= listOfWords.size() - N; ++idx) {
			/* extract words [idx, idx+N-1] */
			NGram newNGram =
					new NGram(listOfWords.subList(idx, idx + N)); 
					
			listOfNGrams.add(newNGram);	
		}
		return listOfNGrams;
	}

	public int getNumWords() {
		return getTextWords().size();
	}
	
	public String getDocId() {
		return docId;
	}

	public String getSource() {
		return source;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getText() {
		return text;
	}
	
	
	public void displayHeader() {

		System.out.printf("DocID = %s  Source = %s  Url = %s\n",
				docId, source, url);
		System.out.printf("   Title: %s\n\n", title);
		System.out.printf("Number of words: %d\n", getNumWords());
	}

	public void displaySummary() {

		displayHeader();
		System.out.println("Summary:");
		System.out.println(summary);

	}

	public void displayFull() {

		displaySummary();
		System.out.println("Full text:");
		System.out.println(text);
		System.out.println();
		System.out.println();
		

	}

}
