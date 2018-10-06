package NameSayer;

import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;
import NameSayer.backend.Recording;
import NameSayer.backend.RecordingStore;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class StreaksAndTiers {
    private static final String STREAKS_FILENAME ="streaks.dat";
    private static final Path _path= Paths.get("./data");
    private CreationStore _creationStore;
    InvalidationListener changesInStreakIntegerProperty= o -> {
        saveStreaks();
    };

    public StreaksAndTiers(CreationStore creationStore){
        _creationStore=creationStore;
        updateListeners();
        _creationStore.addListener(e -> {
            updateListeners();
        });
    }


    private synchronized void saveStreaks(){
        List<String> streakData= new ArrayList<String>();
        List<Creation> creationList= _creationStore.getCreations();
        for(Creation creation: creationList){
            streakData.add(creation.getName() +"\t"+ creation.getStreaks());
        }
        try {
            Files.write(_path.resolve(STREAKS_FILENAME),streakData, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateListeners(){
        for(Creation creation: _creationStore.getCreations()){
            creation.getStreaksProperty().removeListener(changesInStreakIntegerProperty);
            creation.getStreaksProperty().addListener(changesInStreakIntegerProperty);
        }

    }




}
