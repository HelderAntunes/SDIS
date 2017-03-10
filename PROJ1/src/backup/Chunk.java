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

    public boolean equals(Object other){
        boolean result;
        if((other == null) || (getClass() != other.getClass())){
            result = false;
        }
        else{
            Chunk otherChunk = (Chunk)other;
            result = this.fileId.equals(otherChunk.fileId) &&  this.chunkNo == otherChunk.chunkNo;
        }

        return result;
    }
}