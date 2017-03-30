package backup;

public class MetaDataFile implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	public final String name;
	public final String path;
	public final String id;
	public final int repDeg;
	
	public MetaDataFile(String name, String path, String id, int repDeg) {
		this.name = name;
		this.path = path;
		this.id = id;
		this.repDeg = repDeg;
	}
	
}
