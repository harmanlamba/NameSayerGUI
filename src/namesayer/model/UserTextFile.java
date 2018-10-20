package namesayer.model;

import javafx.stage.Stage;
import namesayer.Util;

import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserTextFile {

    private FileChooser fc;
    /*
    This method is in charge of reading the UserText input file and making a list of lists that the TagInput then uses
    to populate the TagInput.
     */
    public static List<List<String>> readFile(Stage ownerStage) {
        ArrayList<List<String>> _finalListOfNames = new ArrayList<List<String>>();
        FileChooser fc = new FileChooser();
        //Making it so the user can only select a .txt file
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fc.getExtensionFilters().addAll(extensionFilter);
        //Showing the dialog box to choose
        File selectedFile = fc.showOpenDialog(ownerStage);
        if (selectedFile != null) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(selectedFile));
                String line;
                while ((line = br.readLine()) != null) {
                    //Adding a list into the list, after the reggex ensures that the hypen is treated as a space
                    List<String> parsedList = CreationsListEntry.parseNamesIntoList(line);
                    if(!parsedList.isEmpty()){
                        _finalListOfNames.add(parsedList);
                    }
                }
            } catch (FileNotFoundException e) {
                Util.showException(e, "Error uploading file - file not found",
                    "The file containing the list of names you want to upload could not be found.\n" +
                    "Please try again.");
            } catch (IOException e) {
                Util.showException(e, "Error uploading file",
                    "Sorry, but we're having difficulty reading the file containing the list of names.\n" +
                    "Please try again.");
            }
        }
        return _finalListOfNames;
    }


}
