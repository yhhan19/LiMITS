package limits.data;

import java.util.Vector;

public class Datasets {

    private Dataset[] data;
    private final String[] params;
    private final String folderName;
    private static final Dataset simData = new Dataset();

    public Datasets(String folderName, String[] params) {
        this.folderName = folderName;
        this.params = params;
        data = new Dataset[params.length];
    }

    private String getWord(int i, int j) {
        String[] param_ = params[i].split("x");
        return param_[j];
    }

    private int getIndex(String folderName) {
        if (folderName.equals("SIM")) return params.length;
        for (int i = 0; i < params.length; i ++) {
            if (folderName.equals(getWord(i, 0))) 
                return i;
        }
        return -1;
    }

    public Dataset getDataset(String folderName) {
        int i = getIndex(folderName);
        if (i == -1) return null;
        if (i == params.length) return simData;
        if (data[i] == null) {
            synchronized (this) {
                if (data[i] == null) {
                    data[i] = new Dataset(this.folderName, params[i]);
                }
            }
        }
        return data[i];
    }
}
