package backup;

import java.util.Comparator;

public class LowDiffRepSorter implements Comparator<MetaDataChunk>{

	public int compare(MetaDataChunk one, MetaDataChunk another){
		int returnVal = 0;
		
		int diffOne = one.currRep - one.desiredRepDeg;
		int diffAnother = another.currRep - another.desiredRepDeg;

		if (diffOne < diffAnother) {
			returnVal =  -1;
		} else if (diffOne > diffAnother) {
			returnVal =  1;
		} else if (diffOne == diffAnother) {
			returnVal =  0;
		}
		return returnVal;

	}
}
