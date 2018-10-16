package namesayer.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;

import namesayer.model.Recording;

/**
 * Making an abstract class in order to have the template method for the method called ready, which essentially is how
 * the media will be played back. This class mainly deals with creating the file to be used in the concatenation command,
 * actually concatenating the files, and lastly removing the silence from the files.
 */
public abstract class ConcatAndSilence {
    private static final String DEFAULT_TEMP_FOLDER = "tempPlayback";
    private static final String DEAFULT_TEMP_CREATIONS_FOLDER="data/tempCreations/";
    private static final double TARGET_VOLUME= -2;

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
                    //System.out.println("ConcatData: "+concatData);
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
                    //This command "greps" the max volume in order to later offset and have a normalisation effect
                    String maxVolumeCmd="ffmpeg -i "+"./"+counter.toString()+" -af volumedetect -vn -sn -dn -f null /dev/null 2>&1 | grep max_volume";
                    ProcessBuilder maxVolumeGrepBuilder= new ProcessBuilder("/bin/bash","-c", maxVolumeCmd);
                    try {
                        Process processVolumeGrep = maxVolumeGrepBuilder.start();
                        processVolumeGrep.waitFor();

                        //Getting the greped value
                        BufferedReader stdin= new BufferedReader(new InputStreamReader(processVolumeGrep.getInputStream()));
                        String stdLine= stdin.readLine();

                        //Applying REGGEX to only get the dB levels
                        String reggexLine=stdLine.replaceAll("\\D+","");
                        reggexLine=reggexLine.substring(reggexLine.length() -3);

                        //Converting it to a double
                        double maxVolume= Double.parseDouble(reggexLine);
                        BigDecimal unscaled = new BigDecimal(maxVolume);
                        BigDecimal scaled= unscaled.scaleByPowerOfTen(-1);
                        double maxVolumeScaled= scaled.doubleValue()*-1;

                        //Basic logic to get the change in volume necessary
                        double changeInVol=(TARGET_VOLUME-maxVolumeScaled);

                        //Applying the change in volume to each file
                        String normaliseCmd="ffmpeg -i "+"./"+counter.toString()+" -filter:a \"volume="+changeInVol+"dB"+"\""+" -y "+"./"+DEAFULT_TEMP_CREATIONS_FOLDER + counter.getFileName();
                        ProcessBuilder normaliseBuilder= new ProcessBuilder("/bin/bash","-c",normaliseCmd);
                        Process normaliseProcess= normaliseBuilder.start();
                        normaliseProcess.waitFor();
                        /*ProcessBuilder checkingTempProcess= new ProcessBuilder("/bin/bash","-c","ffmpeg -i "+"./"+DEAFULT_TEMP_CREATIONS_FOLDER+counter.getFileName()+" -af volumedetect -vn -sn -dn -f null /dev/null");
                        Process checkingTemp= checkingTempProcess.start();
                        checkingTemp.waitFor();
                        BufferedReader stdin2= new BufferedReader(new InputStreamReader(checkingTemp.getErrorStream()));
                        String line="";
                        while ((line = stdin2.readLine()) != null) {
                            System.out.println(line + "\n");
                        }*/
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
                    Process deleteTempCreationsProcess= deleteTempCreations.start();
                    deleteTempCreationsProcess.waitFor();
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
