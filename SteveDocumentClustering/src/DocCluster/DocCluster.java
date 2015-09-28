package DocCluster;

import xmlDocument.XMLDocument;
import article.ArticleSet;
import article.ArticleMinHash;
import article.Article;


import org.w3c.dom.Document;


import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.IOException;

import java.util.Scanner;


public class DocCluster {
	
	static ArticleSet articleSet;
	static ArticleMinHash articleMinHash;
	static int n;
	static Scanner scanner;
	
	public DocCluster() {
	
	}
	
	
	public static void jaccardLoop() {
	
		if (articleMinHash == null) 
			articleMinHash =
		        new ArticleMinHash(articleSet, n);
		for (;;) {
			System.out.print("Enter first article # or -1 to exit: ");
			int idx1 = Integer.parseInt(scanner.nextLine());
			if (idx1 == -1)
				break;
			System.out.print("Enter second article #: ");
			int idx2 = Integer.parseInt(scanner.nextLine());
			articleMinHash.showSimilarity(idx1, idx2);
			
		} 
	}

	public static void closestMatchLoop() {
		
		if (articleMinHash == null) 
			articleMinHash =
		        new ArticleMinHash(articleSet, n);
		for (;;) {
			System.out.print("Enter article number or -1 to exit: ");
			int idx1 = Integer.parseInt(scanner.nextLine());
			if (idx1 == -1)
				break;
			Article thisArticle = articleSet.getArticle(idx1);
			System.out.println("Requested article:");
			thisArticle.displaySummary();
			int bestMatchIdx = articleMinHash.closestMatch(idx1);
			System.out.printf("Closest match is article #%d\n",  bestMatchIdx);
			Article bestArticle = articleSet.getArticle(bestMatchIdx);
			bestArticle.displaySummary();
			articleMinHash.showSimilarity(idx1, bestMatchIdx);
		}
			
	}
	public static void main(String args[])
			throws ParserConfigurationException, SAXException, IOException
	{
		
		/* read arguments */
		String filename = args[0];
		
		if (args.length > 1)
			n = Integer.parseInt(args[1]);  /* # of words per N-Gram */
		else
			n = 2;  /* default */
		


		Document myDoc = XMLDocument.buildDocumentFromFile(filename);
		
		XMLDocument.displayDocumentInfo(myDoc);  /* intro info only */
		
		
		
		articleSet = new ArticleSet(myDoc);

		articleMinHash = null;
		
		/* allow user to lookup articles by source and see # of articles
		 * for each, or see full article for a given doc ID
		 */
		
		int choice;
		scanner = new Scanner(System.in);
		do {
		
			System.out.println("1 for source lookup");
			System.out.println("2 for docid lookup");
			System.out.println("3 for source wildcard lookup");
			System.out.println("4 to show word list");
			System.out.println("5 to show n-grams");
			System.out.println("6 to show source counts");
			System.out.println("7 to compare articles");
			System.out.println("8 to display article min-hash signatures");
			System.out.println("9 to find closest match for article");
			System.out.println("10 to exit");
			System.out.print("Enter choice (1-10): ");
			
			choice = Integer.parseInt(scanner.nextLine());
			
			switch (choice) {
			case 1: 
				articleSet.sourceLookupLoop();
				break;
			case 2: 
				articleSet.docIdLookupLoop();
				break;
			case 3:
				articleSet.sourceWildcardLookupLoop();
				break;
			case 4:
				articleSet.showWordCounts();
				break;
			case 5:
				if (articleMinHash == null) 
					articleMinHash =
				        new ArticleMinHash(articleSet, n);
		
				articleMinHash.displayNGrams();
				break;
			case 6:
				articleSet.displaySourceCounts();
				break;
			case 7:
				jaccardLoop();
				break;
			case 8:
				if (articleMinHash == null) 
					articleMinHash =
				        new ArticleMinHash(articleSet, n);
				articleMinHash.displaySignatures();
				
			case 9:
				closestMatchLoop();
				break;
				
			default:
				break;
			}

		} while (choice >= 1 && choice <= 9);
		scanner.close();	
		System.exit(1);
	}
	
		
	
}
