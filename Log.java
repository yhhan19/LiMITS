import java.io.File;
import java.io.FileWriter;

public class Log {

    private String fileName;
    private File file;

    public Log(String fileName) {
        this.fileName = "log/" + fileName;
        try {
            File file = new File(this.fileName);
            file.delete();
            file.createNewFile();
        }
        catch (Exception e) {

        }
    }

    public void write(String output, boolean toScreen) {
        try {
            FileWriter fw = new FileWriter(fileName, true);
            fw.write(output);
            fw.flush();
            fw.close();
        }
        catch (Exception e) {
            
        }
    }
}
