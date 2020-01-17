import java.io.File;
import java.io.FileWriter;

public class Log {

    private String fileName;
    private File file;
    private FileWriter writer;

    public Log(String fileName) {
        this.fileName = LIMITS.LOG_FOLDER_NAME + "/" + fileName;
        try {
            file = new File(this.fileName);
            file.delete();
            file.createNewFile();
            writer = new FileWriter(file, true);
        }
        catch (Exception e) {

        }
    }

    public Log(String folderName, String fileName) {
        this.fileName = folderName + "/" + fileName;
        try {
            file = new File(this.fileName);
            file.delete();
            file.createNewFile();
            writer = new FileWriter(file, true);
        }
        catch (Exception e) {

        }
    }

    public void write(String output) {
        try {
            writer.write(output);
        }
        catch (Exception e) {
            
        }
    }

    public void close() {
        try {
            writer.close();
        }
        catch (Exception e) {
            
        }
    }
}
