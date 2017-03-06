import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by helder on 04-03-2017.
 */
public class FileSplit {

    public static void main(String[] args) throws IOException {
        mergeFiles("musicas/musica.000", "musica");
        //splitFile(new File("musica"), "musicas");
    }

    public static void splitFile(File f, String to) throws IOException {
        int partCounter = 0;

        int sizeOfFiles = 1024 * 1024; // 1MB
        byte[] buffer = new byte[sizeOfFiles];

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
            String name = f.getName();

            int tmp = 0;
            while ((tmp = bis.read(buffer)) > 0) {
                File newFile = new File(to, name + "."
                        + String.format("%03d", partCounter++));
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, tmp);//tmp is chunk size
                }
            }
        }
    }

    public static void mergeFiles(String oneOfFiles, String into) throws IOException{
        mergeFiles(new File(oneOfFiles), new File(into));
    }

    public static void mergeFiles(File oneOfFiles, File into)
            throws IOException {
        mergeFiles(listOfFilesToMerge(oneOfFiles), into);
    }

    public static List<File> listOfFilesToMerge(File oneOfFiles) {
        String tmpName = oneOfFiles.getName();//{name}.{number}
        String destFileName = tmpName.substring(0, tmpName.lastIndexOf('.'));

        File[] allFiles = oneOfFiles.getParentFile().listFiles();
        List<File> files = new ArrayList<>();

        for(File file: allFiles) {
           if (file.getName().matches(destFileName+".\\d\\d\\d")) {
                files.add(file);
           }
        }

        Collections.sort(files);
        return files;
    }

    public static void mergeFiles(List<File> files, File into) throws IOException {
        try (BufferedOutputStream mergingStream = new BufferedOutputStream(
                new FileOutputStream(into))) {
            for (File f : files) {
                Files.copy(f.toPath(), mergingStream);
            }
        }
    }

}
