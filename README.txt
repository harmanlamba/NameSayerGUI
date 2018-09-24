=======================================================================
                        Name Sayer User Guide
=======================================================================


Requirements for the program:
-----------------------------
1) Ensure that you have ffmpeg installed on your local machine
2) Ensure that you have java installed on your local machine



Running the program:
--------------------

1) To run the program you can either execute the runme.sh file or use the command java -jar NameSayer.jar in the terminal
	
	*) Note, please ensure to give the bash script (runme.sh) executable rights >> chmod u+x runme.sh
	*) Note, please make sure that you run the script/command from the working directory having the script and the jar
	*) Note, to run the script file type the following in the terminal >> ./runme.sh



What is being displayed at start up:
------------------------------------

1) After starting the application, the application will make folders that it uses. Please insert your databse recordings into the ./data/database folder for them to get populated into the application. NOTE: The application does not have to be closed for this process, it can be left open.
2) Once the database recordings are populated you should see them.



Playing individual recordings:
------------------------------

1) Select the single recording you wish to play by either checking the checkbox or clicking on the name which will automatically check the checkbox for you and hit play in the bottom bar.
2) A media time slider will appear and start to move relative to the recording. You may adjust the volume using the volume slider located at the bottom right.
3) To pause the recording, hit on the pause button which replaces the play button once the recording starts to play
4) To go to the next track press the ">" button in the bottom bar, to the right of play/pause button conversely press "<" to go back one recording

*) NOTE: After selecting the Recording if nothing seems to play, this is because the file is empty or the recording is too silent, as a result being filtered
by the Software. Do not panic, this is normal. Simply, give it a poor quality rating to indicate the issue.



Playing a list of recordings:
-----------------------------

1) To play a concatenated string, choose the recordings avalible by either using the checkbox or clicking on them. A number should appear next to the checkbox this indicates the order in which the recordings will be concatenated.
2) You may shuffle the list of recordings by clicking on the shuffle icon on the bottom left side of the media bar

*) NOTE: After selecting the Recording if nothing seems to play, this is because the file is empty or the recording is too silent, as a result being filtered
by the Software. Do not panic, this is normal. Simply, give it a poor quality rating to indicate the issue.



Recording an attempt:
---------------------

1) There are 2 ways to record an attempt, the way mentioned here is by not using the practice tool. To use the practice tool method please skip to the practice tool usage section of the README. 
2) To record an attempt, select the name(s) you wish to make an attempt for, and on the bottom left side of the media bar click on record.
3) This will pop up a new window called "Recording Tool - [name(s)]". Here you may check your mic level on the bottom side of the window where if your mic is working the progress bar will indicate the level of your mic.
4) When you are ready to record, click on the record button and an animation will indicate the remaining time you have from 5 seconds.
5) You may hear your recorded audio by clicking play
6) You may re-record the attempt by clicking record again
7) Once you are satisfied by the attempt, you can click save and the attempt will be added to the main program-scene



Deleting an attempt:
---------------------

*) It is important to note that the program only allows for users to delete ATTEMPTS, and not datbase recordings thus the files that are attempts will have a trash can symbol next to the name.
1) To delete the recording attempt, press the trash symbol



Rating a recording:
-------------------

1) To rate a recording simply click on the amount of stars (1 being the worse and 5 being the best), on the main view where all the recordings are. This will automatically update the file "quality.dat" located in ./data/database. 
2) You may change the quality rating of the recording in the practice tool.



Using the compare tool:
-----------------------

*) The compare tool was designed to compare USER recordings with DATABASE recordings in a quick manner, thus that option is only avalible if 2 recordings of the same name are selected and one is an attempt and the other is a database recording.
1) To enter compare mode, select 2 recordings with the same name. One being a database recording and the other being a user attempt recording
2) Click on Compare, to enter the compare tool
3) A new window will pop up that presents you with two sides, Database and User
4) To play the database recording simply hit play on the database side, and you may adjust the volume accordingly
5) To play the user attempt recording simply hit play on the user side, and you may adjust the volume accordingly
6) To exit the compare tool, simply click on the x on the top right corner of the window.



Using the practice tool:
------------------------

1) To enter the practice tool select the name(s) of recordings you wish to practice
2) Click on practice to enter the practice tool, you should be prompted with a new window
3) Once in, on the left side a combo box will be present with all the database recordings with the first entry being a concatenation of the recordings. On the right side you may see the attempts directly associated with the database recording on the left. The user recordings are filtered by date of creation. Additionally, the label on the top indicates which recording is being practiced.
4) To change the recording that you want to practice, simply use the drop down menu to change the recording in the database side which will automatically update the label and the attempts on the user attempts side.
5) You may choose to add more attempts in practice mode by clicking on the record button, which will pop up the same Recording Tool. (For more information please read the recording tool section of this README)
6) You may shuffle the order of the recordings in practice mode, by hitting the shuffle button similar to the shuffle button in the main screen of the application.
7) You may rate the database recordings directly in the practice tool by chosing the stars, for more information on quality rating please refer to the quality rating section of this README.
8) Once done practicing, to exit click on the x in the top right corner



Quitting NameSayer:
-------------------

1) Click on the x on the top right corner, which will safely close the program.
