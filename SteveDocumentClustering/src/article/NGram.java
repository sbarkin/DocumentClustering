package article;

import java.lang.Comparable;
import java.util.List;


public class NGram implements Comparable<NGram> {

	private List<String> words;
	int n;  /* # of words */
	
	@Override
	public boolean equals(Object other) {
		assert other instanceof NGram;
		if (compareTo((NGram) other) == 0)
			return true;
		else
			return false;
	}
		

	public int compareTo(NGram other) {

		for (int idx = 0; idx < n; ++idx) {
			int compval = getWord(idx).compareTo(other.getWord(idx));
			if (compval != 0)
				return compval;  /* -1 or 1 */
		}
		return 0;  /* all words equal and in same order */
	}
			
			
	
	NGram(List<String> words) {
		n = words.size();
		this.words = words;
	}
	
	public String getWord(int idx) {
		return (words.get(idx));
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int idx = 0; idx < n; ++idx) 
			s = s.append(words.get(idx) + " ");
		return s.toString();
	}
	
}
