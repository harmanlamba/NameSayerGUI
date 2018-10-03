package NameSayer.backend;


import javafx.stage.FileChooser;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserTextFile {
    ArrayList<String> _finalListOfNames = new ArrayList<String>();

    public List readFile() {
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
                    for (String name : line.split(" ")) {
                        if (!name.equals("")) {
                            _finalListOfNames.add(name);
                        }
                    }
                    _finalListOfNames.add("\n");
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
