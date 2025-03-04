package article;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;



import helper.RandomHashFunction;

public class ArticleMinHash {
	
	/* populated by buildNGramsFromArticles which is called by constructor */

	/* mapping of nGrams to the article(s) they are part of */
	private TreeMap<NGram, NGramOccur> nGramMap;
	
	/* These three members are populated by constructor, first two
	 * are based directly on arguments provided
	 */

	private ArticleSet articleSet;
	private int numArticles;  
	private int n;  /* size of each NGram in this MinHash */

	final int NUM_HASH_FUNCTIONS = 100;  
	final int NUM_BANDS = 10;
	final int NUM_ROWS_PER_BAND = 10;
	
	private RandomHashFunction[] randomHashFunctions;
	
	/* for each hash function and article, what is the minimum ngram
	 * present in the article
	 */
	private int minNGram[][];
	
	/* Map each cluster ID to a vector of articles belonging to this cluster */
	private TreeMap<Integer, Vector<Integer>> clusterMap;
	
	/* Map each article to its cluster, -1 means not yet assigned */
	private int cluster[]; /* cluster ID for each article */
	public int numClusters;  /* number of clusters found */
	
	/* candidate pairs of articles based on band hash functions
	 * sufficiently similar to be inserted into graph and
	 * define clusters accordingly
	 */
	private TreeSet<ArticlePair> candidatePairs;
	
	/* similarity graph */
	ArticleSetGraph graph;
	
	private class NGramOccur {
		NGram ngram;   /* repeats the key */
	
		/* indexes of articles containing 1+ instances of this ngram */
		
		TreeSet<Integer> articleNums;  
		int uniqueArticles;	
		int numAppears;  /* number of appearances through all articles */
		int idx;  /* 0...size of NGramMap-1 */
		
		NGramOccur(NGram ngram) {
			this.ngram = ngram;
			articleNums = new TreeSet<Integer>();  /* empty set */
			uniqueArticles = 0;
			numAppears = 0;
			idx = -1;  /* not yet set */
		}
		
		void setIdx(int idx) {
			assert idx >=0 && idx < nGramMap.size();
			this.idx = idx;
		}
		
		int getIdx() {
			assert idx != -1;
			return idx;
		}
		
		int getHashIdx(int hashFunctionIdx) {
			assert hashFunctionIdx >=0 && hashFunctionIdx < NUM_HASH_FUNCTIONS;
			
			int myIdx = getIdx();
			RandomHashFunction thisRandomHashFunction =
					randomHashFunctions[hashFunctionIdx];
					
			return (thisRandomHashFunction.getHash(myIdx));
		}	
			
		boolean inArticle(int articleIdx) {
			assert articleIdx >=0 && articleIdx < numArticles;
			
			boolean found = articleNums.contains(articleIdx);
			return found;
		}
		
		void addAppearance(int articleIdx) {
			assert articleIdx >=0 && articleIdx < numArticles;
			
	
			if (!articleNums.contains(articleIdx)) {
				uniqueArticles++;
				articleNums.add(articleIdx);
			}
			numAppears++;
		}
	}
	
	public boolean clustersDefined() {
		if (cluster == null)
			return false;
		else
			return true;
	}
	
	public void findClusters() {
		findCandidateArticlePairs();  /* creates candidatePairs */
		/* for (ArticlePair thisArticlePair : candidatePairs) {
			thisArticlePair.display();
			articleMinHash.showSimilarity(thisArticlePair);

		} */
		constructSimilarityGraph();  /* candidatePairs -> graph */
		
		
		 /* Change implementation of cluster assignment for each article
		  * (currently an array) so that we can display clusters
		  *  (or allow querying for a single cluster)
		  */
		labelClusters();
		displayClusters();
	}
	
	private void labelClusters() {
		System.out.println("Labeling clusters.");
		cluster = new int[numArticles];
		clusterMap = new TreeMap<Integer, Vector<Integer>>();
		Integer articleIdx;
		for (articleIdx = 0; articleIdx < numArticles; ++articleIdx)
			cluster[articleIdx] = -1; /* no cluster yet for this article */
		
		Integer currentCluster = 0;
		for (articleIdx = 0; articleIdx < numArticles; ++articleIdx) {
			if (cluster[articleIdx] == -1) {
				
				/* depth first search through graph starting at this article */
				if (graph.containsArticle(articleIdx)) 
					/* Depth first search */
					labelCluster(articleIdx, currentCluster); 
			
				
				else {
					cluster[articleIdx] = currentCluster; /* singleton */
					Vector<Integer> articleVector = new Vector<Integer>();
					articleVector.add(articleIdx);  /* vector with 1 entry */
					clusterMap.put(currentCluster, articleVector );
				}
				++currentCluster;	
			}
		}
		System.out.printf("# of clusters found: (%d)\n", currentCluster);
		numClusters = currentCluster;
				
	}
	
	private void labelCluster(Integer articleIdx, int thisCluster) {
		cluster[articleIdx] = thisCluster; /* label this article */
		
		Vector<Integer> articleVector = clusterMap.get(thisCluster);
		if (articleVector == null) {
			articleVector = new Vector<Integer>();
			articleVector.add(articleIdx);  
			clusterMap.put(thisCluster, articleVector );
		}
		else
			articleVector.add(articleIdx);  
		

		/* visit and label all connected (sufficiently similar) articles */
		for (Integer similarArticleIdx : graph.similarArticles(articleIdx)) 
			if (cluster[similarArticleIdx] == -1)
				labelCluster(similarArticleIdx, thisCluster);
		
	}

	private void displayClusters() {
		for (int cluster : clusterMap.keySet()) {
			if (clusterMap.get(cluster).size() > 1)
				displayClusterBrief(cluster);
		}		
	}
	
	private void displayClusterBrief(int cluster) {
		if (!clusterMap.containsKey(cluster)) 
			System.out.printf("Invalid cluster #%d\n",  cluster);
		else {
			Vector<Integer> articlesInCluster = clusterMap.get(cluster);
			System.out.printf("Cluster #%d contains (%d) articles: ",
					cluster, articlesInCluster.size());
			for (int articleIdx : articlesInCluster) {
				System.out.print(articleIdx + " ");
			}
				
			System.out.println();

		}
	}
	
	public void displayClusterDetailed(int cluster) {
		if (!clusterMap.containsKey(cluster)) 
			System.out.printf("Invalid cluster #%d\n",  cluster);
		else {
			Vector<Integer> articlesInCluster = clusterMap.get(cluster);
			System.out.printf("Cluster #%d contains (%d) articles\n",
					cluster, articlesInCluster.size());
			for (int articleIdx : articlesInCluster) {
				Article thisArticle = articleSet.getArticle(articleIdx);
				thisArticle.displaySummary();
			}

		}
	}
	private void constructSimilarityGraph() {
		assert candidatePairs != null;
		assert graph == null;
		
		System.out.println("Constructing similarity graph from article pairs.");
		graph = new ArticleSetGraph();
	
		for (ArticlePair thisArticlePair: candidatePairs) {
			int idx1 = thisArticlePair.idx1;
			int idx2 = thisArticlePair.idx2;
			graph.addArticlePair(idx1, idx2, jaccardSignatureSimilarity(idx1, idx2));	
		}
		System.out.println("Similarity graph complete");
		System.out.printf("# of graph vertices (%d),   # of edges (%d)\n",
				graph.numArticles(), graph.numEdges());
	}
	/* make list of article pairs for which similarity will be calculated
	 * across full signature, based on exact match in at least one band
	 * Requires minNGram matrix to have been previously calculated.
	 * No side effects, returns list of unique article pairs to be 
	 * considered as candidate pairs
	 */
	public void findCandidateArticlePairs() {
		
		System.out.println("Finding candidate article pairs");
		candidatePairs = new TreeSet<ArticlePair>();
		
		for (int bandIdx = 0; bandIdx < NUM_BANDS; ++bandIdx) {
			/* bandMap is reused for each band */
			System.out.printf("Starting band #%d\n", bandIdx);
			
			TreeMap<BandSignature, Vector<Integer>> thisBandMap;
			
			thisBandMap = generateBandMap(bandIdx);
			
			addToCandidatePairsFromBandMap(thisBandMap, candidatePairs);
			
		}
		System.out.printf("Candidate pairs generated: %d\n",
				candidatePairs.size());
		/* candidatePairs is now fully populated */
	}

	private TreeMap<BandSignature, Vector<Integer>> 
	generateBandMap(int bandIdx) {

		TreeMap<BandSignature, Vector<Integer>> thisBandMap = 
				new TreeMap<BandSignature, Vector<Integer>>();
		/* System.out.println("Hashing articles to band map"); */
		for (int articleIdx = 0; articleIdx < numArticles; ++articleIdx) {
			Vector<Integer> thisBandVector = new Vector<Integer>();
			for (int rowidx = 0; rowidx < NUM_ROWS_PER_BAND; ++rowidx) 
				thisBandVector.add(
						minNGram[bandIdx * NUM_ROWS_PER_BAND + rowidx][articleIdx]);
			BandSignature thisBandSignature = new BandSignature(thisBandVector);
			if (thisBandMap.containsKey(thisBandSignature)) {
				Vector<Integer> articlesSoFar = thisBandMap.get(thisBandSignature);
				articlesSoFar.add(articleIdx);
			}
			else {
				Vector<Integer> articlesSoFar = new Vector<Integer>();
				articlesSoFar.add(articleIdx);
				thisBandMap.put(thisBandSignature,  articlesSoFar);
			}

		}
		return thisBandMap;
	}
	
	private void
	addToCandidatePairsFromBandMap(
			TreeMap<BandSignature, Vector<Integer>>thisBandMap, 
			TreeSet<ArticlePair> candidatePairs) {
		/* generate candidate pairs for this band based on article pairs
		 * belonging to same bandMap entry (same signature in band)
		 * Each entry in for loop is a vector of integers 
		 *
		 */
		/* System.out.println("Enumerating pairs based on band map"); */
		for (Vector<Integer> articleVector : thisBandMap.values()) {
			for (int idx1 = 0; idx1 < articleVector.size(); ++idx1) {
				for (int idx2 = idx1+1; idx2 < articleVector.size(); ++idx2) {
					int articleIdx1 = articleVector.get(idx1);
					int articleIdx2 = articleVector.get(idx2);
					ArticlePair thisPair =
							new ArticlePair(articleIdx1, articleIdx2);

					candidatePairs.add(thisPair);
				}
			}
		}
	}
	
	private void calculateSignatures() {
		
		System.out.println("Calculating article signatures.");
		minNGram = new int[NUM_HASH_FUNCTIONS][numArticles];
		for (int hashFunctionIdx = 0;
				hashFunctionIdx < NUM_HASH_FUNCTIONS; ++hashFunctionIdx) {
			
			for (int idx = 0; idx < numArticles; ++idx) 
				minNGram[hashFunctionIdx][idx] = Integer.MAX_VALUE;

			for (NGramOccur nGramOccur : nGramMap.values()) {
				/* get index for this n-gram.  Later, get specified hash index */

				int nGramHashIdx = nGramOccur.getHashIdx(hashFunctionIdx);

				/* scan through articles containing this nGram */
				for (int articleIdx: nGramOccur.articleNums) 
					if (nGramHashIdx < minNGram[hashFunctionIdx][articleIdx])
						minNGram[hashFunctionIdx][articleIdx] = nGramHashIdx;
			}

		}
		System.out.println("Signatures complete.");
	}



	public void displaySignatures() {
		
		for (int articleIdx = 0; articleIdx < numArticles; ++articleIdx) {	
			System.out.printf("Article #%d:  Minhash values: (", articleIdx);
			for (int idx = 0; idx < NUM_HASH_FUNCTIONS; ++idx) {
				System.out.print(minNGram[idx][articleIdx]);
				if (idx != NUM_HASH_FUNCTIONS-1)
					System.out.print(", ");
			}
			System.out.println(")");
		}
		
	}
	
	
	

	
	

			
		
	public ArticleMinHash(ArticleSet articleSet, int n) {
		this.articleSet = articleSet;
		this.numArticles = articleSet.numArticles();  /* stored for convenience */	
		this.n = n;	
		tallyNGramsFromArticles();
		int numNgrams = nGramMap.size();  
		
		randomHashFunctions =
				new RandomHashFunction[NUM_HASH_FUNCTIONS];
		
		/* generate NUM_HASH_FUNCTIONS different random hash functions */
		for (int idx = 0; idx < NUM_HASH_FUNCTIONS; ++idx) 
			randomHashFunctions[idx] = new RandomHashFunction(numNgrams);
		
		/* apply random hash functions */
		calculateSignatures();
		numClusters = -1;  /* clusters not found yet */
		
	}
	
	/* calculate Jaccard similarity (0.0-1.0) for articles #idx1 & #idx2 */
	public double jaccardSimilarity(int idx1, int idx2) {
		
		double numer = 0.0, denom = 0.0;
		for (NGramOccur thisNGramOccur : nGramMap.values()) {
			
			boolean inArticle1 = thisNGramOccur.inArticle(idx1);
			boolean inArticle2 = thisNGramOccur.inArticle(idx2);
			
			if (inArticle1 || inArticle2) {
				denom += 1.0;
				if (inArticle1 && inArticle2)
					numer += 1.0;
			}
			
		}
		if (denom == 0.0)
			return 0.0;
		else
			return (numer / denom);
	}

	
	/* calculate Jaccard similarity (0.0-1.0) for 
	 * hash signatures of articles #idx1 & #idx2
	 *  */
	public double jaccardSignatureSimilarity(int idx1, int idx2) {
		
		double numer = 0.0, denom = (double) NUM_HASH_FUNCTIONS;
		for (int hashFunctionIdx = 0; 
				hashFunctionIdx < NUM_HASH_FUNCTIONS; ++hashFunctionIdx) {
			if (minNGram[hashFunctionIdx][idx1]
					== minNGram[hashFunctionIdx][idx2])
				numer += 1.0;
		}
		return numer / denom;
	}
	
	public int closestMatch(int articleIdx) {
		double maxSimilarity = -1.0;
		int bestMatch = -1;
		for (int idx = 0; idx < numArticles; ++idx) {
			if (idx != articleIdx) {
				double thisSimilarity = jaccardSignatureSimilarity(idx, articleIdx);
				if (thisSimilarity > maxSimilarity) {
					maxSimilarity = thisSimilarity;
					bestMatch = idx;
				}
			}
		}
		return bestMatch;
	}
	
	public void showSimilarity(ArticlePair pair) {
		showSimilarity(pair.idx1, pair.idx2);
	}
	
	public void showSimilarity(int idx1, int idx2) {
		double signatureSimilarity = 
				jaccardSignatureSimilarity(idx1, idx2);

		double similarity = jaccardSimilarity(idx1, idx2);


		System.out.printf("Article Similarity = %f    Signature Similarity = %f\n", 
				similarity, signatureSimilarity);
	}
	
	private void
	tallyNGramsFromArticles() {
		
		
		nGramMap = new TreeMap<NGram, NGramOccur>();
		
		System.out.println("Tallying n-grams from articles");
		
		int totRawNgrams = 0;
		for (int articleIdx = 0; articleIdx < numArticles; ++articleIdx) {
			Article thisArticle = articleSet.getArticle(articleIdx);

			
			ArrayList<NGram> theseNgrams = thisArticle.getNGrams(n);
			
			for (int idx = 0; idx < theseNgrams.size(); ++idx) {
				NGram thisNgram = theseNgrams.get(idx);
				
				NGramOccur thisNGramOccur = nGramMap.get(thisNgram);
				
				if (thisNGramOccur == null)  {
					thisNGramOccur = new NGramOccur(thisNgram);
					nGramMap.put(thisNgram, thisNGramOccur);
				}
				
				thisNGramOccur.addAppearance(articleIdx);

 
			}
			totRawNgrams += theseNgrams.size();
			if (articleIdx % 1000 == 0) {
				System.out.printf("Processed %d articles, %d raw n-grams, %d unique n-grams found so far\n",
						articleIdx, totRawNgrams, nGramMap.size());
			}
		}
		System.out.printf("N-Gram tallying complete.  Total unique n-grams: %d\n",
				nGramMap.size());
		
		/* Now that all n-grams have been identified, number them */
		System.out.println("Numbering the n-grams");
		int nGramIdx = 0;
		for (NGramOccur thisNGramOccur : nGramMap.values()) {
			thisNGramOccur.setIdx(nGramIdx);
			nGramIdx++;
		}
		System.out.println("Numbering complete.");
		
		
	}
	
	
	
	
	public void
	displayNGrams() {
		
		int idx = 0;
		for (NGramOccur nGramOccur : nGramMap.values()) {
		
			if (nGramOccur.numAppears >= 500) 
				System.out.printf("N-Gram #%d: (%s)   # of appearances: %d   # of articles: %d\n",
					idx, nGramOccur.ngram.toString(),
					nGramOccur.numAppears, nGramOccur.uniqueArticles);
			idx++;
		}
		
	}
	
	
	
	
}
