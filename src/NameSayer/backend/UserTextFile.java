package NameSayer.backend;


import javafx.stage.FileChooser;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserTextFile {
    /*
    This method is in charge of reading the UserText input file and making a list of lists that the TagInput then uses
    to populate the TagInput.
     */
    public static List readFile() {
        ArrayList<List<String>> _finalListOfNames = new ArrayList<List<String>>();
        FileChooser fc = new FileChooser();
        //Making it so the user can only select a .txt file
        FileChooser.ExtensionFilter extensionFilter= new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fc.getExtensionFilters().addAll(extensionFilter);
        //Showing the dialog box to choose
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(selectedFile));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println("Line: "+line);
                    line=line.replaceAll("[\\-]"," ");
                    //Adding a list into the list, after the reggex ensures that the hypen is treated as a space
                    _finalListOfNames.add(Arrays.asList(line.toLowerCase().split(" ")));
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
