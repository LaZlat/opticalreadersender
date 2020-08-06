package lt.vikoeif.lzatkus.opticreader.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import lt.vikoeif.lzatkus.opticreader.business.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Controller {

    public static int THREADS = 3;

    @FXML
    TextField browseTextField;
    @FXML
    ProgressBar progressBar, threadsProgress;
    @FXML
    ListView readList, doneList;
    @FXML
    Label threadsLabel;


    FileManager fileManager;

    public void browsePath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(browseTextField.getScene().getWindow());

        if (selectedDirectory != null) {
            browseTextField.setText(selectedDirectory.getPath());
        }
    }

    public void send() {
        progressBar.setProgress(0);
        threadsLabel.setText(String.valueOf(THREADS));

        fileManager = new FileManager(new File(browseTextField.getText()),progressBar, readList, doneList, threadsProgress);
        fileManager.start();

        //threadsProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }
}
