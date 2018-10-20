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
import namesayer.Util;

/**
 * Making an abstract class in order to have the template method for the method called ready, which essentially is how
 * the media will be played back. This class mainly deals with creating the file to be used in the concatenation command,
 * actually concatenating the files, and lastly removing the silence from the files.
 */
public abstract class AudioProcessor {
    private static final String DEFAULT_TEMP_FOLDER = "tempPlayback";
    private static final String TEMP_CREATIONS_FOLDER = "data/tempCreations/";
    private static final String ERROR_TITLE = "Error while preparing audio to playback";
    private static final double TARGET_VOLUME = -2;
    private static final int NORMALISED_SAMPLERATE = 48000;
    private static final int NORMALISED_NUM_CHANNEL = 1;
    private static final String NORMALISED_BITDEPTH = "s16";

    public abstract void ready(String filePath);

    public abstract void failed();

    public AudioProcessor(List<Recording> recordings) {
        this(recordings, DEFAULT_TEMP_FOLDER);
    }

    public AudioProcessor(List<Recording> recordings, String folder) {
        try {
            Util.ensureFolderExists(Paths.get("./data/" + folder));
        } catch (Util.HandledException e) {
            failed();
            return;
        }

        Task<Void> task = new Task<Void>() {
            private final List<Recording> _recordings = new ArrayList<>(recordings);

            @Override
            protected Void call() throws Util.HandledException {

                List<String> concatData = new ArrayList<String>();
                List<Path> creationsPaths = new ArrayList<>();

                // Ensuring that the tempCreations folder exist.
                Util.ensureFolderExists(Paths.get(TEMP_CREATIONS_FOLDER));

                // Setting up the correct format for the file, and iterating through all the selected recordings, and adding
                // them to the concatData arrayList to then have it be written into a file.
                for (Recording counter : _recordings) {
                    concatData.add("file '../../" + TEMP_CREATIONS_FOLDER + counter.getPath().getFileName() + "'");
                    creationsPaths.add(counter.getPath());
                }

                // Writing the concatenation data into its corresponding file
                Path path = Paths.get("./data/" + folder + "/concat.txt");
                try {
                    Files.write(path, concatData,
                        StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    Util.showException(e, ERROR_TITLE,
                        "Sorry, but we're having difficulty writing a required file: " + path + "\nPlease try again.");
                    throw new Util.HandledException();
                }

                // Normalising the audio files before concatenating them.
                for (Path counter : creationsPaths) {

                    // Setting up the process builder.
                    // This command "greps" the max volume in order to later offset and have a normalisation effect.
                    String maxVolumeCmd = "ffmpeg " +
                        "-i " + "./" + counter.toString() + " " +
                        "-af volumedetect " +
                        "-vn -sn -dn " +
                        "-f null " +
                        "/dev/null 2>&1 | grep max_volume";

                    ProcessBuilder maxVolumeGrepBuilder = new ProcessBuilder("/bin/bash", "-c", maxVolumeCmd);

                    try {
                        Process processVolumeGrep = maxVolumeGrepBuilder.start();
                        Util.awaitProcess(processVolumeGrep, ERROR_TITLE,
                            "Sorry, we failed to read some audio file information\n" +
                                "needed to combine the audio properly. Please try again.");

                        // Getting the greped value.
                        BufferedReader stdin = new BufferedReader(new InputStreamReader(processVolumeGrep.getInputStream()));
                        String stdLine = stdin.readLine();

                        // Applying REGGEX to only get the dB levels.
                        String reggexLine = stdLine.replaceAll("\\D+", "");
                        reggexLine = reggexLine.substring(reggexLine.length() - 3);

                        // Converting it to a double.
                        double maxVolume = Double.parseDouble(reggexLine);
                        BigDecimal unscaled = new BigDecimal(maxVolume);
                        BigDecimal scaled = unscaled.scaleByPowerOfTen(-1);
                        double maxVolumeScaled = scaled.doubleValue() * -1;

                        // Basic logic to get the change in volume necessary.
                        double changeInVol = (TARGET_VOLUME - maxVolumeScaled);

                        // Applying the change in volume to each file.
                        String normaliseCmd = "ffmpeg " +
                            "-i " + "./" + counter.toString() + " " +
                            "-ar " + NORMALISED_SAMPLERATE + " " +
                            "-ac " + NORMALISED_NUM_CHANNEL + " " +
                            "-sample_fmt " + NORMALISED_BITDEPTH + " " +
                            "-filter:a \"volume=" + changeInVol + "dB" + "\"" + " " +
                            "-y " +
                            "./" + TEMP_CREATIONS_FOLDER + counter.getFileName();

                        ProcessBuilder normaliseBuilder = new ProcessBuilder("/bin/bash", "-c", normaliseCmd);
                        Process normaliseProcess = normaliseBuilder.start();
                        Util.awaitProcess(normaliseProcess, ERROR_TITLE,
                            "Sorry, but we failed to normalise an audio:\n" + counter +
                                "\nPlease try again.");

                    } catch (IOException e) {
                        Util.showException(e, ERROR_TITLE,
                            "Sorry, but we're having difficulty normalising the audio file: " + counter +
                                "\nPlease try again.");
                        throw new Util.HandledException();
                    }

                }

                // Setting up the ProcessBuilders to execute the bash commands which concatenate and silence the recordings
                // appropriately.
                ProcessBuilder concatBuilder = new ProcessBuilder("/bin/bash", "-c",
                    "ffmpeg -f concat -safe 0 -y " +
                        "-i ./data/" + folder + "/concat.txt " +
                        "-c copy ./data/" + folder + "/playBack.wav");

                ProcessBuilder silenceRemoverBuilder = new ProcessBuilder("/bin/bash", "-c",
                    "ffmpeg -hide_banner -y " +
                        "-i ./data/" + folder + "/playBack.wav -af " +
                        "silenceremove=" +
                        "start_periods=1:" +
                        "start_duration=0:" +
                        "start_threshold=-25dB:" +
                        "stop_periods=-1:" +
                        "stop_duration=1:" +
                        "stop_threshold=-25dB:" +
                        "leave_silence=1 " +
                        "./data/" + folder + "/playBackSilenced.wav");

                ProcessBuilder deleteTempCreations = new ProcessBuilder("/bin/bash", "-c",
                    "rm -r ./data/tempCreations");

                try {
                    Process process = concatBuilder.start();
                    Util.awaitProcess(process, ERROR_TITLE,
                        "Sorry, but combining the audio files failed.");

                    Process processSilence = silenceRemoverBuilder.start();
                    Util.awaitProcess(processSilence, ERROR_TITLE,
                        "Sorry, but silencing the audio files failed.");

                    Process deleteTempCreationsProcess = deleteTempCreations.start();
                    // Ignore non-critical errors for this cleanup process.
                    deleteTempCreationsProcess.waitFor();
                } catch (IOException e) {

                    Util.showException(e, ERROR_TITLE,
                        "Sorry, but we're having difficulty combining the audio files together" +
                            "\nPlease try again.");
                    throw new Util.HandledException();

                } catch (InterruptedException e) {

                    Util.showException(e, ERROR_TITLE,
                        "Sorry, but we got interrupted while combining the audio files together" +
                            "\nPlease try again.");
                    throw new Util.HandledException();

                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    ready("./data/" + folder + "/playBackSilenced.wav");
                });
            }

            @Override
            protected void failed() {
                AudioProcessor.this.failed();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

}
