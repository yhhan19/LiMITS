package limits.io;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Writer {

    public static final int 
        DISPLAY_SCALE = 9;

    public static final NumberFormat 
        DOUBLE_FORMAT = new DecimalFormat("0.00000000E0"), 
        DOUBLE_FORMAT_ = new DecimalFormat("0.000000"), 
        BIGDEC_FORMAT = new DecimalFormat("0.0E0");

    private String fileName;
    private File file;
    private FileWriter writer;

    public Writer(String folderName, String fileName) {
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
