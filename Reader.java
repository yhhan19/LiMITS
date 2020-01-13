import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.Vector;

public class Reader {

    public static Vector<String> getFiles(String folderName) {
        Vector<String> fileNames = new Vector<String>();
        try {
            File folder = new File(folderName);
            for (final File fileEntry : folder.listFiles()) {
                String fileName = folderName + "/" + fileEntry.getName();
                if (fileEntry.isDirectory()) {
                    fileNames.addAll(getFiles(fileName));
                }
                else {
                    fileNames.add(fileName);
                }
            }
        }
        catch (Exception e) {

        }
        return fileNames;
    }

    public static Vector<String> getLines(String fileName, int startLine) {
        File file = new File(fileName);
        Vector<String> lines = new Vector<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = null;
            for (int i = 0; (line = br.readLine()) != null; i ++) {
                if (i >= startLine) lines.add(line);
            }
        }
        catch (Exception e) {

        }
        return lines;
    }

    public static Vector<Vector<String>> getDataset(String folderName, int startLine) {
        System.out.println("loading: " + folderName);
        Vector<String> fileNames = getFiles(folderName);
        Vector<Vector<String>> data = new Vector<Vector<String>>();
        for (int i = 0; i < fileNames.size(); i ++) {
            Vector<String> lines = getLines(fileNames.get(i), startLine);
            data.add(lines);
        }
        System.out.println("loaded: " + folderName);
        return data;
    }

    public static Vector<String> getWords(String input, String token) {
        Vector<String> output = new Vector<String>();
        StringTokenizer st = new StringTokenizer(input, token);
        while (st.hasMoreTokens())
            output.add(st.nextToken());
        return output;
    }
}
