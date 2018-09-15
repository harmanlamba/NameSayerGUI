package NameSayer.backend;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileSystem;
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
import java.util.HashSet;

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
    private final ObservableMap<String,Recording> _recordings = FXCollections.observableHashMap();
    private Task<Void> _taskWatcher;

    private static final String QUALITY_FILENAME = "quality.dat";
    private static final Pattern FILENAME_PATTERN =
        Pattern.compile("\\A\\w+_(?<date>\\d+-\\d+-\\d+_\\d+-\\d+-\\d+)_(?<name>.*)\\z");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("d-M-yyyy_HH-mm-ss");

    public RecordingStore(Path path, CreationStore creationStore, Recording.Type type) {
        super();
        _path = path;
        _type = type;
        _creationStore = creationStore;

        try {
            if (Files.notExists(_path)) {
                Files.createDirectories(_path);
            }
            if (!Files.isDirectory(_path)) {
              Files.delete(_path);
              Files.createDirectories(_path);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO
        }

        populateFromFilesystem();
        watchDirectory();
    }

    public void invalidateQualities() {
        assert Platform.isFxApplicationThread();

        Task<Void> qualityWriter = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                saveQualities();
                return null;
            }
        };
        Thread th = new Thread(qualityWriter);
        th.start();
    }

    private void populateFromFilesystem() {
        try {
            Files.list(_path).forEach(p -> addByFilename(p.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
            // TODO
        }
    }

    private void addByFilename(String filename) {
        assert Platform.isFxApplicationThread();

        Matcher matcher = FILENAME_PATTERN.matcher(filename);

        // Ignore invalid files.
        if (!matcher.find()) return;

        Date date;
        try {
            date = DATE_FORMAT.parse(matcher.group("date"));
        } catch (ParseException e) {
            e.printStackTrace();
            // Ignore invalid files.
            return;
        }

        String name = matcher.group("name");
        Creation creation = _creationStore.get(name);
        if (creation == null) {
            creation = _creationStore.add(name);
        }
        Path path = _path.resolve(filename);
        Recording recording = new Recording(creation, date, path, _type);
        recording.qualityProperty().addListener(o -> invalidateQualities());
        _recordings.put(filename, recording);
    }

    private void removeByFilename(String filename) {
        assert Platform.isFxApplicationThread();

        // Silently ignore recordings that don't already exist.
        _recordings.remove(filename);
    }

    private synchronized void saveQualities() {
        assert !Platform.isFxApplicationThread();

        List<String> qualityData = new ArrayList<>();
        for (String filename : _recordings.keySet()) {
            qualityData.add(filename + "\t" + _recordings.get(filename).getQuality());
        }
        try {
            Files.write(_path.resolve(QUALITY_FILENAME), qualityData,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO
        }
    }

    private synchronized void reloadQualities() {
        assert !Platform.isFxApplicationThread();

        try {
            Files.lines(_path.resolve(QUALITY_FILENAME))
                .map(line -> line.split("\t"))
                .forEach(entry -> {
                    if (entry.length < 2) return;

                    String filename = entry[0];
                    String qualityStr = entry[1];

                    if (!_recordings.containsKey(filename)) return;

                    Recording.Quality quality;
                    try {
                        quality = Recording.Quality.valueOf(entry[1]);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        // Ignore invalid quality entries.
                        // TODO log.
                        return;
                    }
                    _recordings.get(filename).setQuality(quality);
                });
        } catch (IOException e) {
            e.printStackTrace();
            // TODO
        }
    }

    private void watchDirectory() {
        assert Platform.isFxApplicationThread();

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
                        for (WatchEvent<?> event : key.pollEvents())
                        {
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
                    // TODO
                }

                private void handleEvent(WatchEvent<?> event) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

                        String filename = ((Path)event.context()).toString();
                        if (filename == QUALITY_FILENAME) return;
                        Platform.runLater(() -> addByFilename(filename));

                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {

                        String filename = ((Path)event.context()).toString();
                        if (filename == QUALITY_FILENAME) {
                            // Fight back the deletion by recreating the file.
                            saveQualities();
                        } else {
                            Platform.runLater(() -> removeByFilename(filename));
                        }

                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                        String filename = ((Path)event.context()).toString();
                        if (filename == QUALITY_FILENAME) {
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
            e.printStackTrace();
            // TODO
        }
    }

}
