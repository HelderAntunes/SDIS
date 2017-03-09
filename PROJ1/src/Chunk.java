/**
 * Created by helder on 09-03-2017.
 */
public class Chunk {
    public String fileId;
    public int chunkNo;

    public Chunk(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
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
