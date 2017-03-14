package backup;

public class Chunk {
    public final String fileId;
    public final int chunkNo;
    public final int desiredRepDeg;

    public Chunk(String fileId, int chunkNo, int desiredDeg) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.desiredRepDeg = desiredDeg;
    }
    
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chunk other = (Chunk) obj;
		if (chunkNo != other.chunkNo)
			return false;
		if (desiredRepDeg != other.desiredRepDeg)
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
		result = prime * result + desiredRepDeg;
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
		return result;
	}
}