package namesayer.model;

import namesayer.Util;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.WatchService;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * Represents one of the recording folders.
 * The single source of truth lies here. Watches for filesystem changes, and
 * serves as a layer of indirection for all filesystem operations.
 */
public class RecordingStore {

    private final Path _path;
    private final CreationStore _creationStore;
    private final Recording.Type _type;
    private final ObservableMap<String, Recording> _recordings = FXCollections.observableHashMap();
    private Task<Void> _taskWatcher;

    private static final String QUALITY_FILENAME = "quality.dat";
    private static final Pattern FILENAME_PATTERN =
        Pattern.compile("\\A\\w+_(?<date>\\d+-\\d+-\\d+_\\d+-\\d+-\\d+)_(?<name>.*)\\.wav\\z");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("d-M-yyyy_HH-mm-ss");

    public RecordingStore(Path path, CreationStore creationStore, Recording.Type type) throws StoreUnavailableException {
        _path = path;
        _type = type;
        _creationStore = creationStore;

        // Ensure directory exists.
        try {
            Util.ensureFolderExists(_path);
        } catch (Util.HandledException e) {
            throw new StoreUnavailableException();
        }

        populateFromFilesystem();
        watchDirectory();
    }

    public static String getDateStringNow() {
        return DATE_FORMAT.format(new Date());
    }

    /**
     * Rewrite the quality data into the file now that some of the recording's qualities
     * have changed.
     */
    private void invalidateQualities() {
        assert Platform.isFxApplicationThread();

        Task<Void> qualityWriter = new Task<Void>() {
            @Override
            protected Void call() throws IOException {
                saveQualities();
                return null;
            }

            @Override
            protected void failed() {
                Util.showException(getException(), "Error saving file quality",
                    "Sorry, we are having difficulty saving information about the recording qualities.\n" +
                    "You may continue using this app, but if this keeps happening, try restarting the app.");
            }
        };
        Thread th = new Thread(qualityWriter);
        th.start();
    }

    /**
     * Initialize our store with the files on the directory.
     */
    private void populateFromFilesystem() {
        assert Platform.isFxApplicationThread();

        Task<Void> qualityReloader = new Task<Void>() {
            @Override
            protected Void call() throws IOException {
                reloadQualities();
                return null;
            }

            @Override
            protected void failed() {
                Util.showException(getException(), "Error loading information about recording qualities",
                    "Sorry, we are having difficulty reading information about the recording qualities.\n" +
                    "You may continue using this app, but if this keeps happening, try restarting the app.");
            }
        };

        Task<Void> populator = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Files.list(_path).forEach(p -> {
                    Platform.runLater(() -> {
                        addByFilename(p.getFileName().toString());
                    });
                });

                // Last file not guaranteed to be added yet.
                // Need to runLater to put it at the end of event stack.
                scheduleReloadQualities();

                return null;
            }

            @Override
            protected void failed() {
                Util.showException(getException(), "Error loading the recordings",
                    "Sorry, we could not load the recordings properly.\n" +
                    "You may continue using this app, but if things don't work, try restarting the app.");
            }

            private void scheduleReloadQualities() {
                Platform.runLater(() -> {
                    Thread th = new Thread(qualityReloader);
                    th.start();
                });
            }
        };
        Thread th = new Thread(populator);
        th.start();
    }

    /**
     * All metadata for the recording are found in the filename.
     * Parse it to initialize our recording objects.
     */
    private void addByFilename(String filename) {
        assert Platform.isFxApplicationThread();

        Matcher matcher = FILENAME_PATTERN.matcher(filename);

        // Ignore invalid files.
        if (!matcher.find()) return;

        // Try parsing date.
        Date date;
        try {
            date = DATE_FORMAT.parse(matcher.group("date"));
        } catch (ParseException e) {
            // Ignore invalid files.
            return;
        }

        // Create.

        String name = matcher.group("name").toLowerCase();
        Creation creation = _creationStore.get(name);
        if (creation == null) {
            creation = _creationStore.add(name);
        }
        Path path = _path.resolve(filename);

        Recording recording = new Recording(creation, date, path, _type);

        // We want our recordings to report back to the RecordingStore whenever their quality
        // changes, so we can save that change into our designated file.
        recording.qualityProperty().addListener(o -> invalidateQualities());

        _recordings.put(filename, recording);
    }

    /**
     * Remove recording object from our store. Note: does not remove from the filesystem.
     */
    private void removeByFilename(String filename) {
        assert Platform.isFxApplicationThread();

        Recording recording = _recordings.get(filename);

        // Silently ignore recordings that don't already exist.
        if (recording == null) return;

        recording.getCreation().removeRecording(recording);
        _recordings.remove(filename);
    }

    private synchronized void saveQualities() throws IOException {
        assert !Platform.isFxApplicationThread();

        List<String> qualityData = new ArrayList<>();
        for (String filename : _recordings.keySet()) {
            qualityData.add(filename + "\t" + _recordings.get(filename).getQuality());
        }
        Files.write(_path.resolve(QUALITY_FILENAME), qualityData,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE);
    }

    private synchronized void reloadQualities() throws IOException {
        assert !Platform.isFxApplicationThread();

        Path qualityPath = _path.resolve(QUALITY_FILENAME);
        if (Files.notExists(qualityPath)) {
            return;
        }

        Files.lines(qualityPath)
            .map(line -> line.split("\t"))
            .forEach(entry -> {

                // Ignore invalid lines
                if (entry.length < 2) return;

                String filename = entry[0];
                String qualityStr = entry[1];

                // Ignore data associated with non-existent recordings.
                if (!_recordings.containsKey(filename)) return;

                // Apply.
                final Recording.Quality quality;
                try {
                    quality = Recording.Quality.valueOf(entry[1]);
                } catch (IllegalArgumentException e) {
                    // Ignore invalid quality entries.
                    return;
                }
                Platform.runLater(() -> _recordings.get(filename).setQuality(quality));

            });
    }

    /**
     * Synchronise our store with the directory.
     */
    private void watchDirectory() {
        assert Platform.isFxApplicationThread();

        // Note: Our data flow goes:
        //   View --> MainScene --> Filesystem --> RecordingStore --> Recording.

        try {
            _taskWatcher = new Task<Void>() {

                private WatchService _watcher;

                {
                    _watcher = _path.getFileSystem().newWatchService();
                    _path.register(_watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                }

                @Override
                protected Void call() throws Exception {
                    WatchKey key;
                    while ((key = _watcher.take()) != null) {
                        if (isCancelled()) {
                            break;
                        }
                        for (WatchEvent<?> event : key.pollEvents()) {
                            handleEvent(event);
                        }
                        boolean isValid = key.reset();
                        if (!isValid || isCancelled()) break;
                    }
                    _watcher.close();
                    return null;
                }

                @Override
                protected void succeeded() {
                    // Try restarting this task if it was stopped for some reason.
                    Platform.runLater(() -> watchDirectory());
                }

                @Override
                protected void failed() {
                    Util.showException(getException(), "Error watching the filesystem",
                        "Sorry, we are having difficulty keeping track with the recordings in your folders.\n" +
                        "You may continue using this app, but new changes will not show up until you restart the app.");
                }

                private void handleEvent(WatchEvent<?> event) throws IOException {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

                        String filename = ((Path) event.context()).toString();
                        if (filename == QUALITY_FILENAME) return;
                        Platform.runLater(() -> addByFilename(filename));

                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {

                        String filename = ((Path) event.context()).toString();
                        if (filename == QUALITY_FILENAME) {
                            // Fight back the deletion by recreating the file.
                            saveQualities();
                        } else {
                            Platform.runLater(() -> removeByFilename(filename));
                        }

                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                        String filename = ((Path) event.context()).toString();
                        if (filename.equals(QUALITY_FILENAME)) {
                            reloadQualities();
                        }

                    }

                }

            };

            Thread th = new Thread(_taskWatcher);

            // Allow program to exit even if this thread is still alive.
            th.setDaemon(true);

            th.start();
        } catch (IOException e) {
            Util.showException(e, "Error watching the filesystem",
                "Sorry, we are having difficulty keeping track with the recordings in your folders.\n" +
                "You may continue using this app, but new changes will not show up until you restart the app.");
        }
    }

    public void stopWatcher() {
        _taskWatcher.cancel();
    }

    /**
     * Thrown by the constructor when the recording store path is not valid.
     */
    @SuppressWarnings("serial")
    public static class StoreUnavailableException extends Exception {
    }

}
