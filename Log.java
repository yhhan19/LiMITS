import java.io.File;
import java.io.FileWriter;

public class Log {

    private String fileName;
    private File file;

    public Log(String fileName) throws Exception {
        this.fileName = "log/" + fileName;
        File file = new File(this.fileName);
        file.delete();
        file.createNewFile();
    }

    public void write(String output) throws Exception {
        FileWriter fw = new FileWriter(fileName, true);
        fw.write(output);
        fw.flush();
        fw.close();
        System.out.print(output);
    }
}
