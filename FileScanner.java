import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

public class FileScanner {

    public Vector<String> getFiles(String folderName) throws Exception {
        File folder = new File(folderName);
        Vector<String> list = new Vector<String>();
        for (final File fileEntry : folder.listFiles()) {
            String fileName = folderName + "/" + fileEntry.getName();
            if (fileEntry.isDirectory()) {
                list.addAll(getFiles(fileName));
            }
            else {
                list.add(fileName);
            }
        }
        return list;
    }

    public Vector<String> getLines(String fileName, int start) throws Exception {
        File file = new File(fileName);
        Vector<String> input = new Vector<String>();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String s = null;
        for (int i = 0; (s = br.readLine()) != null; i ++) 
            if (i >= start) input.add(s);
        return input;
    }
}
