package namesayer.controller;

import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javax.sound.sampled.*;

public class MicrophoneLevel {

    //Setting up Constants that won't change and are needed
    private static final float SAMPLE_RATE = 8000;
    private static final int BIT_DEPTH = 8;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = false;
    private static final boolean IS_BIG_ENDIAN = false;
    private static final AudioFormat FORMAT =
        new AudioFormat(SAMPLE_RATE, BIT_DEPTH, CHANNELS, SIGNED, IS_BIG_ENDIAN);

    private Task<Void> _task;
    private DoubleProperty _levelProperty = new SimpleDoubleProperty();

    public MicrophoneLevel() {
        startTask();
    }

    //A method to stop reading the microphone level
    public void close() {
        _task.cancel(true);
    }


    public DoubleProperty levelProperty() {
        return _levelProperty;
    }

    public double getLevel() {
        return _levelProperty.getValue();
    }

    //Starting to pick up the mic levels
    private void startTask() {
        assert Platform.isFxApplicationThread();
        assert _task == null;

        _task = new Task<Void>() {

            private int _level = 0;
            private byte _buffer[] = new byte[128];
            private TargetDataLine _line;

            @Override
            protected Void call() throws Exception {
                _line = AudioSystem.getTargetDataLine(FORMAT);
                _line.open(FORMAT);
                _line.start();
                while (!isCancelled()) {
                    int bytesRead = _line.read(_buffer, 0, _buffer.length);
                    if (bytesRead == 0) break;

                    //Calculated gotten data and filtering the data, so the data does not jump around abruptly.
                    int raw = calculateAudioLevel(_buffer);
                    _level = filterLevel(_level, raw);
                    Platform.runLater(() -> _levelProperty.setValue(_level / 256.0));
                }
                _line.close();
                return null;
            }

            @Override
            protected void succeeded() {
                // Restart if closed.
                Platform.runLater(() -> startTask());
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };

        Thread thread = new Thread(_task);
        thread.setDaemon(true);
        thread.start();
    }

    //Setting up boundary values in order for the mic level to decay slowly when there is no signal received from the mic
    private int filterLevel(int oldValue, int rawValue) {
        int level;
        if (rawValue > oldValue) {
            level = rawValue;
        } else {
            level = oldValue - 16;
        }

        if (level < 0) return 0;
        if (level > 128) return 128;
        return level;
    }

    /**
     * The inspiration for this code came from the following link, where we used the same code to calculated the RMS value
     * for the Microphone input levels. The link is as follows: https://stackoverflow.com/questions/3899585/microphone-level-in-java
     */
    private int calculateAudioLevel(byte[] audioData) {
        long lSum = 0;
        for (int i = 0; i < audioData.length; i++) {
            lSum = lSum + audioData[i];
        }

        double dAvg = lSum / audioData.length;
        double sumMeanSquare = 0d;

        for (int j = 0; j < audioData.length; j++) {
            sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);
        }

        double averageMeanSquare = sumMeanSquare / audioData.length;

        return (int) (Math.pow(averageMeanSquare, 0.5d) + 0.5);
    }
}
