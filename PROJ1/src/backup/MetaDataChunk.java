package backup;

public class MetaDataChunk {

	public final String fileId;
    public final int chunkNo;
    public final int desiredRepDeg;
    
    public final int id;
    public static int last_id = 0;

    public MetaDataChunk(String fileId, int chunkNo, int desiredDeg) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.desiredRepDeg = desiredDeg;
        this.id = MetaDataChunk.last_id++;
    }
    
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetaDataChunk other = (MetaDataChunk) obj;
		if (chunkNo != other.chunkNo)
			return false;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		return true;
	}
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chunkNo;
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
		return result;
	}
    
    
    /**
     * Return a string with at least 65 characters (64 from fileId and at least 1 from chunk no)
     */
    @Override
   	public String toString() {
   		return this.fileId + String.format("%03d", this.chunkNo);
   	}
}