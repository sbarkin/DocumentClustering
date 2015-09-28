package article;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.bag.TreeBag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import xmlDocument.XMLDocument;

public class ArticleSet {
	
	private ArrayList<Article> articles;
	
	private TreeMap<String, ArrayList<Article>> sourceMap;
	private TreeMap<String, ArrayList<Article>> docIdMap;
	private TreeBag<String> wordBag;
	
	public Article getArticle(int idx) {
		if (idx < 0 || idx > articles.size()) {
			System.out.printf("Invalid article # requested: %d\n",  idx);
			return null;
		}
		else
			return articles.get(idx);	
	}

	
	int numArticles() {
		return articles.size();
	}
	
	
	
	public void addArticle(Article newArticle) {
		articles.add(newArticle);
	}
	

	
	/* can be used to build a tree map based on source or docid */
	private TreeMap<String, ArrayList<Article>> 
	buildTreeMapFromArticles(String tagname)
	{
		assert tagname.equals("source") || tagname.equals("docid");
				

		TreeMap<String, ArrayList<Article>> treeMap = 
				new TreeMap<String, ArrayList<Article>>();


		for (int idx = 0; idx < numArticles(); ++idx) {
			Article thisArticle = articles.get(idx);
			String keyString;
			
			if (tagname.equals("source"))
				keyString = thisArticle.getSource();
			else 
				keyString = thisArticle.getDocId();

			/* see if there are any existing articles with this tagname */

			ArrayList<Article> theseArticles = treeMap.get(keyString);
			if (theseArticles == null) {
				theseArticles = new ArrayList<Article>();
				theseArticles.add(thisArticle);  /* 1st article in list */
				treeMap.put(keyString, theseArticles); /* 1st article for source */
			}
			else
				theseArticles.add(thisArticle);/* add new article to existing list */

		}
		
		return treeMap;
	}
	
	private TreeBag<String>
	buildWordBagFromArticles() {

		TreeBag<String> wordBag = new TreeBag<String>();

		for (int idx = 0; idx < numArticles(); ++idx) {
			Article thisArticle = articles.get(idx);

			List<String> words = thisArticle.getTextWords();
			for (int wordIdx = 0; wordIdx < words.size(); ++wordIdx) {
				String thisWord = words.get(wordIdx);
				wordBag.add(thisWord,1);
			}
		}

		return wordBag;
	}
	
	
	
	

	public void displaySourceCounts() {
		
		Set<Map.Entry<String, ArrayList<Article>>> 
					sourceSet =  sourceMap.entrySet();
		
		for (Map.Entry<String, ArrayList<Article>> thisEntry : sourceSet) 	
			if (thisEntry.getValue().size() >= 30)
				System.out.printf("Source %s,  number of articles: %d\n",
						thisEntry.getKey(), thisEntry.getValue().size());

	}
	
	public void
	showWordCounts() {
		for (String word : wordBag.uniqueSet()) {
			int count = wordBag.getCount(word);
			if (count > 1000)
				System.out.printf("Word: %s   Number of occurences: %d\n",
						word, wordBag.getCount(word));
		}
	}
	

	public void docIdLookupLoop() {
		
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.print("Enter doc ID (or 'exit'): ");
			String docId = scanner.nextLine();
			if (docId.equals("exit")) {
				
				return;
			}
				
				
			ArrayList<Article> articles = docIdMap.get(docId);
			if (articles == null)
				System.out.printf("No articles found for document ID: %s\n", 
						docId);
			else {
				System.out.printf("# of Articles found: %d\n",  articles.size());
				
				
				for (int idx = 0; idx < articles.size(); ++idx) {
					Article thisArticle = articles.get(idx);
					
					thisArticle.displaySummary();
					System.out.println();
					System.out.println();
				}

			}
		}
	}
	
	
	public void sourceLookupLoop() {

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.print("Enter source (or 'exit'): ");
			String source = scanner.nextLine();
			if (source.equals("exit")) {
				
				return;
			}

			ArrayList<Article> articles = sourceMap.get(source);
			if (articles == null)
				System.out.printf("No articles found for source: %s\n", source);
			else {
				System.out.printf("# of Articles found: %d\n",  articles.size());

				/*
					for (int idx = 0; idx < articles.size(); ++idx) {
						Article thisArticle = articles.get(idx);
						thisArticle.displaySummary();
						System.out.println();
						System.out.println();
					}
				 */
			}

		}
	}

	public void sourceWildcardLookupLoop() {

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.print("Enter source fragment (or 'exit'): ");
			String fragment = scanner.nextLine();
			if (fragment.equals("exit")) {
				return;
			}
				

			Set<Map.Entry<String, ArrayList<Article>>> 
			sourceSet =  sourceMap.entrySet();
			int numArticles = 0;

			for (Map.Entry<String, ArrayList<Article>> thisEntry : sourceSet) {
				String thisString = thisEntry.getKey();

				if (thisString.contains(fragment)) {
					ArrayList<Article> theseArticles = thisEntry.getValue();

					for (int idx = 0; idx < theseArticles.size(); ++idx) {
						Article thisArticle = theseArticles.get(idx);
						thisArticle.displaySummary();
						System.out.println();
						System.out.println();
					}
					numArticles += theseArticles.size();
				}
			}
			System.out.printf("Number of articles found: %d\n", numArticles);

		}
	}
	

	public ArticleSet(Document d) {

		System.out.println("Building article list from document");
		NodeList nodeList = d.getElementsByTagName("document");
		int numArticles = nodeList.getLength();

		articles = new ArrayList<Article>();

		System.out.printf("Number of articles to be read: %d\n", numArticles);

		for (int idx = 0; idx < numArticles; ++idx) {
			Element thisElement = (Element) nodeList.item(idx);

			String thisDocId = XMLDocument.getStringValue(thisElement, "docid");
			String thisSource = XMLDocument.getStringValue(thisElement, "source");
			String thisUrl = XMLDocument.getStringValue(thisElement, "url");
			String thisTitle = XMLDocument.getStringValue(thisElement, "title");
			String thisSummary = XMLDocument.getStringValue(thisElement, "summary");
			String thisText = XMLDocument.getStringValue(thisElement, "text");

			Article newArticle = new Article(thisDocId, thisSource, 
					thisUrl, thisTitle, thisSummary, thisText);

			articles.add(newArticle);

			/* newArticle.displayHeader(); */
		}
		System.out.printf("# of articles read from document: %d\n",
				numArticles);

		sourceMap = 
				buildTreeMapFromArticles("source");

		docIdMap = 
				buildTreeMapFromArticles( "docid");

		wordBag = buildWordBagFromArticles();
	}
}
