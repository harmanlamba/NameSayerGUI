package NameSayer;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;

import NameSayer.backend.Recording;

/**
 * Making an abstract class in order to have the template method for the method called ready, which essentially is how
 * the media will be played back. This class mainly deals with creating the file to be used in the concatenation command,
 * actually concatenating the files, and lastly removing the silence from the files.
 */
public abstract class ConcatAndSilence {
    private static final String DEFAULT_TEMP_FOLDER = "tempPlayback";

    public abstract void ready(String filePath);

    public ConcatAndSilence(List<Recording> recordings) {
        this(recordings, DEFAULT_TEMP_FOLDER);
    }

    public ConcatAndSilence(List<Recording> recordings, String folder) {
        Path _path = Paths.get("./data/" + folder);
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
        }


        Task<Void> task = new Task<Void>() {
            private final List<Recording> _recordings = new ArrayList<>(recordings);

            @Override
            protected Void call() throws Exception {

                List<String> concatData = new ArrayList<String>();

                //Setting up the correct format for the file, and iterating through all the selected recordings, and adding
                //them to the concatData arrayList to then have it be written into a file.
                for (Recording counter : _recordings) {
                    concatData.add("file '../../" + counter.getPath() + "'");
                }

                Path path = Paths.get("./data/" + folder + "/concat.txt");
                try {
                    Files.write(path, concatData,
                        StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO
                }
                //Setting up the ProcessBuilders to execute the bash commands which concatenate and silence the recordings
                //appropriately
                ProcessBuilder concatBuilder = new ProcessBuilder("/bin/bash", "-c",
                    "ffmpeg -f concat -safe 0 -y " +
                        "-i ./data/" + folder + "/concat.txt " +
                        "-c copy ./data/" + folder + "/playBack.wav");

                ProcessBuilder silenceRemoverBuilder = new ProcessBuilder("/bin/bash", "-c",
                    "ffmpeg -hide_banner -y " +
                        "-i ./data/" + folder + "/playBack.wav -af " +
                        "silenceremove=1:0:-50dB:1:5:-50dB:0 " +
                        "./data/" + folder + "/playBackSilenced.wav");
                try {
                    Process process = concatBuilder.start();
                    process.waitFor(); //ensuring that concatenation happens before silencing
                    Process processSilence = silenceRemoverBuilder.start();
                    processSilence.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    ready("./data/" + folder + "/playBackSilenced.wav");
                });
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }
}
