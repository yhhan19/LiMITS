package limits.data;

import java.text.NumberFormat;
import java.util.Vector;

import limits.io.*;

public class Results {

    protected static final NumberFormat f = Writer.DOUBLE_FORMAT_;

    private Result[] results;
    private String fileName, folderName;

    public Results(String folderName, String fileName) {
        this.folderName = folderName;
        this.fileName = fileName;
        Vector<Result> temp = new Vector<Result>();
        Vector<String> lines = Reader.getLines(folderName + "/" + fileName, 0);
        for (int i = lines.size() - 1, j = lines.size(); i >= 0; i --) {
            String[] words = lines.get(i).split(" ");
            if (words[0].equals("error:")) {
                double eps = Double.parseDouble(words[1]);
                int cases = Integer.parseInt(words[3]), size = Integer.parseInt(words[6]);
                String[] names = new String[j - i - 1];
                double[][] res = new double[j - i - 1][3];
                for (int k = i + 1; k < j; k ++) {
                    String[] words_ = lines.get(k).split(" ");
                    names[k - i - 1] = words_[0];
                    res[k - i - 1][0] = Double.parseDouble(words_[1]);
                    res[k - i - 1][1] = Double.parseDouble(words_[2]);
                    res[k - i - 1][2] = Double.parseDouble(words_[3]);
                }
                temp.add(new Result(names, res, eps, size, cases));
                j = i;
            }
        }
        results = new Result[temp.size()];
        for (int i = temp.size() - 1, j = 0; i >= 0; i --, j ++) 
            results[j] = temp.get(i);
    }

    public String toString(int x, int y) {
        String series = "{";
        switch (y) {
            case 0: 
                for (int i = 0; i < results.length; i ++) {
                    series += "{" + f.format(results[i].getError()) + ", " + f.format(1 / results[i].getData(x, 0)) + "}";
                    series += (i == results.length - 1) ? "}" : ", ";
                }
                break;
            case 1: 
                for (int i = 0; i < results.length; i ++) {
                    series += "{" + f.format(results[i].getError()) + ", " + f.format(results[i].getData(x, 1)) + "}";
                    series += (i == results.length - 1) ? "}" : ", ";
                }
                break;
            default: 
                for (int i = 0; i < results.length; i ++) {
                    series += "{" + f.format(results[i].getError()) + ", " + f.format(results[i].getData(x, 0) / results[i].getData(0, 0)) + "}";
                    series += (i == results.length - 1) ? "}" : ", ";
                }
        }
        return series;
    }

    public String toString(int y) {
        String series = "data" + y + " = {";
        for (int i = 0; i < results[0].getRaws(); i ++) {
            series += toString(i, y);
            series += (i == results[0].getRaws() - 1) ? "};\n" : ", ";
        }
        return series;
    }

    public void toCommands(String name, String range) {
        Writer commands = new Writer(folderName, fileName + ".nb");
        commands.write(toString(0) + toString(1) + toString(2));
        String plotCommands = "";
        plotCommands += "f2 = ListLinePlot[data2, DataRange->{" + range + "},Frame->{{True,False},{True,False}},PlotRange->All,PlotLabels->Placed[{\"RDP\",\"CIS\",\"CIW\",\"MIM\",\"MIV\"},{Automatic}],FrameLabel->{\"error (m)\",\"performance\"},LabelStyle->Directive[Bold,Medium]];\n";
        plotCommands += "f0 = ListLinePlot[data0, DataRange->{" + range + "},Frame->{{True,False},{True,False}},PlotRange->All,PlotLabels->Placed[{\"RDP\",\"CIS\",\"CIW\",\"MIM\",\"MIV\"},{Automatic}],FrameLabel->{\"error (m)\",\"compression ratio\"},LabelStyle->Directive[Bold,Medium]];\n";
        plotCommands += "f1 = ListLinePlot[data1, DataRange->{" + range +"},Frame->{{True,False},{True,False}},PlotRange->All,PlotLabels->{\"RDP\",\"CIS\",\"CIW\",\"MIM\",\"MIV\"},FrameLabel->{\"error (m)\",\"time/point (ms)\"},LabelStyle->Directive[Bold,Medium]];\n";
        plotCommands += "Export[\"" + name + "-relative-effectiveness.pdf\",f2];\n";
        plotCommands += "Export[\"" + name + "-absolute-effectiveness.pdf\",f0];\n";
        plotCommands += "Export[\"" + name + "-efficiency.pdf\",f1];\n";
        commands.write(plotCommands);
        commands.close();
    }
}
