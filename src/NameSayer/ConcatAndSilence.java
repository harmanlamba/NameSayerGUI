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
    private static final String DEAFULT_TEMP_CREATIONS_FOLDER="data/tempCreations/";

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
            protected Void call(){

                List<String> concatData = new ArrayList<String>();
                List<Path> creationsPaths=new ArrayList<>();

                //Ensuring that the tempCreations folder exist
                Path _path = Paths.get("./data/tempCreations");
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

                //Setting up the correct format for the file, and iterating through all the selected recordings, and adding
                //them to the concatData arrayList to then have it be written into a file.
                for (Recording counter : _recordings) {
                    //concatData.add("file '../../" + counter.getPath() + "'");
                    concatData.add("file '../../"+DEAFULT_TEMP_CREATIONS_FOLDER + counter.getPath().getFileName() + "'");
                    System.out.println("ConcatData: "+concatData);
                    creationsPaths.add(counter.getPath());
                }

                Path path = Paths.get("./data/" + folder + "/concat.txt");
                try {
                    Files.write(path, concatData,
                        StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO
                }
                //Normalising the audio files before concatenating them
                for(Path counter:creationsPaths){
                    //Setting up the process builder
                    String cmd="ffmpeg -i "+"./"+counter.toString()+" -af_loudnorm=I=-16:TP=-1.5:LRA=11:measured_I=-27.2:measured_TP=-14.4:measured_LRA=0.1:measured_thresh=-37.7:offset=-0.7:linear=true"+" ./data/tempCreations/"+counter.getFileName();
                    System.out.println(cmd);
                    ProcessBuilder nomalizeBuilder= new ProcessBuilder("/bin/bash","-c", cmd);
                    try {
                        Process processNormalize = nomalizeBuilder.start();
                        processNormalize.waitFor();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

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
                ProcessBuilder deleteTempCreations= new ProcessBuilder("/bin/bash","-c",
                    "rm -r ./data/tempCreations");
                try {
                    Process process = concatBuilder.start();
                    process.waitFor(); //ensuring that concatenation happens before silencing
                    Process processSilence = silenceRemoverBuilder.start();
                    processSilence.waitFor();
                    //Process deleteTempCreationsProcess= deleteTempCreations.start();
                    //deleteTempCreationsProcess.waitFor();
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
