package NameSayer.backend;


import javafx.stage.FileChooser;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserTextFile {

    public static List readFile() {
        ArrayList<List<String>> _finalListOfNames = new ArrayList<List<String>>();
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter= new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fc.getExtensionFilters().addAll(extensionFilter);
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(selectedFile));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println("Line: "+line);
                    line=line.replaceAll("[\\-]"," ");
                    _finalListOfNames.add(Arrays.asList(line.toLowerCase().split(" ")));
                    System.out.println("Debugging Dump: " + _finalListOfNames);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return _finalListOfNames;
    }


}
