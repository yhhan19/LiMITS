import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileScanner {

    public static Vector<String> getFiles(String folderName) throws Exception {
        File folder = new File(folderName);
        Vector<String> fileNames = new Vector<String>();
        for (final File fileEntry : folder.listFiles()) {
            String fileName = folderName + "/" + fileEntry.getName();
            if (fileEntry.isDirectory()) {
                fileNames.addAll(getFiles(fileName));
            }
            else {
                fileNames.add(fileName);
            }
        }
        return fileNames;
    }

    public static Vector<String> getLines(String fileName, int startLine) throws Exception {
        File file = new File(fileName);
        Vector<String> lines = new Vector<String>();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = null;
        for (int i = 0; (line = br.readLine()) != null; i ++) 
            if (i >= startLine) lines.add(line);
        return lines;
    }

    public static Vector<String> getWords(String input, String token) {
        Vector<String> output = new Vector<String>();
        StringTokenizer st = new StringTokenizer(input, token);
        while (st.hasMoreTokens())
            output.add(st.nextToken());
        return output;
    }
}
