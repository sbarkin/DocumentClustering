package article;

import java.util.Vector;

public class BandSignature implements Comparable<BandSignature> {
	Vector<Integer> minNgram;
	
	BandSignature(Vector<Integer> vector) {
		minNgram = vector;  /* signature in this band for a single article */
	}
	
	@Override
	public boolean equals(Object other) {
		assert other instanceof BandSignature;
		if (compareTo((BandSignature) other) == 0)
			return true;
		else
			return false;
	}
	
	public int compareTo(BandSignature other) {
		for (int idx = 0; idx < minNgram.size(); ++idx) {
			int compVal = 
			this.minNgram.get(idx).compareTo(other.minNgram.get(idx));
			if (compVal != 0)
				return compVal;
		}
		return 0;  /* equal */
	}

}