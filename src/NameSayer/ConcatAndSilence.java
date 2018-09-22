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

                ProcessBuilder concatBuilder = new ProcessBuilder("/bin/bash", "-c",
                        "ffmpeg -f concat -safe 0 -y " +
                        "-i ./data/" + folder + "/concat.txt " +
                        "-c copy ./data/" + folder + "/playBack.wav");

                ProcessBuilder silenceRemoverBuilder = new ProcessBuilder("/bin/bash", "-c",
                        "ffmpeg -hide_banner -y " +
                        "-i ./data/" + folder + "/playBack.wav -af " +
                        "silenceremove=1:0:-50dB:1:5:-50dB:0:peak " +
                        "./data/" + folder + "/playBackSilenced.wav");
                try {
                    Process process = concatBuilder.start();
                    int exitCodeConcat = process.waitFor();
                    Process processSilence = silenceRemoverBuilder.start();
                    int exitCodeSilence = processSilence.waitFor();
                    System.out.println(exitCodeConcat);
                    System.out.println("Exit Code for Silencing: " + exitCodeSilence);
                    BufferedReader stderr = new BufferedReader(new InputStreamReader(processSilence.getErrorStream()));
                    String line = "";
                    while ((line = stderr.readLine()) != null) {
                        //System.out.println(line + "\n");
                    }
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