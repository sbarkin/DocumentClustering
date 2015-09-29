package article;



public class ArticlePair implements Comparable<ArticlePair> {
	Integer idx1, idx2;  /* indexes into ArticleSet */
	
	ArticlePair(int idx1, int idx2) {
		this.idx1 = idx1;
		this.idx2 = idx2;
	}
	
	
	public void display() {
		System.out.printf("Article pair: (%d,  %d)\n", idx1, idx2);
	}
	@Override
	public boolean equals(Object other) {
		assert other instanceof ArticlePair;
		if (compareTo((ArticlePair) other) == 0)
			return true;
		else
			return false;
	}
	
	public int compareTo(ArticlePair other) {
		int compVal1 = this.idx1.compareTo(other.idx1);
		if (compVal1 != 0)
			return compVal1;
		int compVal2 = this.idx2.compareTo(other.idx2);	
		return compVal2;
	}

}
