import java.util.Vector;

public class Dataset {

    private Vector<Vector<String>> data;

    public Dataset(String folderName, int startLine) {
        System.out.println("loading: " + folderName);
        Vector<String> fileNames = Reader.getFiles(folderName);
        data = new Vector<Vector<String>>();
        for (int i = 0; i < fileNames.size(); i ++) {
            Vector<String> lines = Reader.getLines(fileNames.get(i), startLine);
            data.add(lines);
        }
        System.out.println("loaded: " + folderName);
    }

    public Vector<String> get(int i) {
        return data.get(i);
    }

    public int size() {
        return data.size();
    }
}