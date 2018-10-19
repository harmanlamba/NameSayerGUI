package namesayer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;

/**
 * Miscellaneous helper functions for error handler and reoccuring file io procedures.
 */
public class Util {
    private static final Path ERROR_LOGS_FILE = Paths.get("./logs.txt");

    /**
     * Attempts to create the directory or convert it into a directory if not already exists.
     */
    public static void ensureFolderExists(Path path) throws HandledException {
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }
            if (!Files.isDirectory(path)) {
                Files.delete(path);
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            showException(e, "Error preparing folder",
                "Sorry, but we're having difficulty opening one of our folders needed:\n" +
                "\"" + path + "\". Try restarting the app.");
            throw new HandledException();
        }
    }

    /**
     * Provides a consistent mechanism to instrusively inform the user of a critical error.
     * Ensures the dialog is created from the application thread, and waits for it to close.
     */
    public static void showProblem(String title, String friendlySummary, String detail) {
        Task<Void> alertTask = new Task<Void>() {
            @Override
            protected Void call() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(friendlySummary);
                alert.setContentText(detail);
                alert.showAndWait();
                return null;
            }
            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };
        if (Platform.isFxApplicationThread()) {
            // Run immediately - don't create deadlock with its own thread.
            alertTask.run();
        } else {
            Platform.runLater(alertTask);
            try {
                // Wait for it to close.
                alertTask.get();
            } catch (Exception e) {
                // Can't do much about this.
                e.printStackTrace();
            }
        }
    }

    /**
     * Call this when encountering an unexpected exception beyond our control, such as
     * filesystem IO operations and external bash commands. These errors should be alerted
     * to the user and logged appropriately for further investigation for developers.
     */
    public static void showException(Throwable e, String title, String friendlyMessage) {
        e.printStackTrace();
        showProblem(title, friendlyMessage, "Refer to the terminal for more information.");
    }

    /**
     * Wait for process to finish and when the exit code is not zero, then report error
     * to user and log the stderr to a file. Assumes that stderr is not being read
     * by another stream reader elsewhere.
     */
    public static void awaitProcess(Process process, String errorTitle, String friendlyMessage) throws HandledException {
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        List<String> stderrMessage = new ArrayList<>();
        String line;

        int exitStatus = 0;
        try {
            while ((line = stderrReader.readLine()) != null) {
                stderrMessage.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            exitStatus = process.waitFor();
        } catch (InterruptedException e) {
            showException(e, errorTitle, friendlyMessage);
        }

        stderrMessage.add("Program exited with exit status = " + exitStatus);

        if (exitStatus != 0) {
            try {
                Files.write(ERROR_LOGS_FILE, stderrMessage,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            showProblem(errorTitle, friendlyMessage, "Please see logs.txt for more information.");
            throw new HandledException();
        }
    }

    /**
     * Dataless exception to keep the program in a thrown state, while acknowledging that the
     * error has already been correctly handled and no further action is necessary.
     */
    @SuppressWarnings("serial")
    public static class HandledException extends Exception {
    }

}
