package NameSayer;

import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;
import NameSayer.backend.Recording;
import NameSayer.backend.RecordingStore;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class StreaksAndTiers {

    private static final String STREAKS_FILENAME = "streaks.dat";
    private static final Path _path = Paths.get("./data");

    private CreationStore _creationStore;
    private InvalidationListener changesInStreakIntegerProperty = o -> invalidateStreaks();

    public StreaksAndTiers(CreationStore creationStore) {
        _creationStore = creationStore;

        updateListeners();
        reloadStreaks();
        _creationStore.addListener(e -> {
            updateListeners();
            reloadStreaks();
        });
    }

    private void reloadStreaks() {
        assert Platform.isFxApplicationThread();

        Path streaksPath = _path.resolve(STREAKS_FILENAME);
        if (Files.notExists(streaksPath)) {
            return;
        }

        Task<Void> streaksReloader = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Files.lines(streaksPath)
                    .map(line -> line.split("\t"))
                    .forEach(entry -> {

                        // Ignore invalid lines.
                        if (entry.length < 2) return;

                        String name = entry[0];
                        String streaksString = entry[1];
                        final Creation creation = _creationStore.get(name);

                        // Ignore data associated with non-existent creations.
                        if (creation == null) return;

                        final int streaks;
                        try {
                            streaks = Integer.parseInt(streaksString);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            // Ignore invalid streak entries.
                            return;
                        }

                        Platform.runLater(() -> creation.setStreaks(streaks));

                    });

                return null;
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };
        Thread th = new Thread(streaksReloader);
        th.start();
    }

    private void invalidateStreaks() {
        assert Platform.isFxApplicationThread();

        Task<Void> streaksWriter = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                saveStreaks();
                return null;
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };
        Thread th = new Thread(streaksWriter);
        th.start();
    }

    private synchronized void saveStreaks() {
        assert !Platform.isFxApplicationThread();

        List<String> streakData = new ArrayList<String>();
        List<Creation> creationList = _creationStore.getCreations();
        for (Creation creation: creationList) {
            streakData.add(creation.getName() + "\t" + creation.getStreaks());
        }
        try {
            Files.write(_path.resolve(STREAKS_FILENAME), streakData, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateListeners() {
        for (Creation creation: _creationStore.getCreations()) {
            creation.streaksProperty().removeListener(changesInStreakIntegerProperty);
            creation.streaksProperty().addListener(changesInStreakIntegerProperty);
        }
    }

}
