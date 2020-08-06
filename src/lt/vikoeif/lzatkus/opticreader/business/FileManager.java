package lt.vikoeif.lzatkus.opticreader.business;



import javafx.application.Platform;
import javafx.concurrent.Task;

import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import lt.vikoeif.lzatkus.opticreader.controller.Controller;

import java.io.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class FileManager extends Thread {



    private StringBuilder finalText = new StringBuilder();
    private File path;
    private ProgressBar progressBar, threadsProgress;
    private ListView readList, doneList;

    public FileManager(File path, ProgressBar progressBar, ListView readList, ListView doneList, ProgressBar threadsProgress) {
        this.path = path;
        this.progressBar = progressBar;
        this.threadsProgress = threadsProgress;
        this.readList = readList;
        this.doneList = doneList;
    }

    public void cycleThroughFiles() throws IOException, InterruptedException, ExecutionException {
        File[] files = path.listFiles();
        List<String>  filesName = new ArrayList<>();
        int counter = 1;
        int finalSize = files.length;

        for (int i = 0; i < files.length; i++) {
            filesName.add(files[i].getName());
        }


        while(!filesName.isEmpty()) {

            new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Platform.runLater(() -> threadsProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS));
                    return null;
                }
            }).start();


            new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Platform.runLater(() -> readList.getItems().setAll(filesName));
                    return null;
                }
            }).start();

            ExecutorService executor = Executors.newFixedThreadPool(1);
            List<Future<String>> workers = new ArrayList<Future<String>>();

            int tempCount = 0;
            int currentFileCount = 0;
            ArrayList<File> filesReadyToBe = new ArrayList<>();

            for (File x : files) {

                currentFileCount++;
                tempCount++;
                filesReadyToBe.add(x);

                if (tempCount == 5 || currentFileCount == files.length) {
                    File[] hotFiles = new File[5];

                    for (int i = 0; i < filesReadyToBe.size(); i++) {
                        hotFiles[i] = filesReadyToBe.get(i);
                    }

                    if(counter % 2 == 0) {
                        counter++;
                        try {
                            workers.add(executor.submit(new SocketMaster(hotFiles, SocketMaster.HOST, SocketMaster.PORT)));
                        } catch(Exception e) {
                            System.out.println("its off");
                        }
                    } else {
                        counter++;
                        try {
                        workers.add(executor.submit(new SocketMaster(hotFiles, "localhost", 5020)));
                        } catch(Exception e) {
                            System.out.println("its off");
                        }
                    }

                    tempCount = 0;
                    filesReadyToBe.clear();
                }
            }

            try {

                while (!workers.isEmpty()) {
                    Future<String> worker = null;

                    for (int idx = 0; idx < workers.size(); idx++) {
                        worker = workers.get(idx);

                        if (worker.isDone()) {
                            workers.remove(idx);
                            break;
                        }

                        worker = null;
                    }

                    if (worker == null) {
                        Thread.sleep(500);
                    } else {
                        try {
   
                            String[] splitString = worker.get().split("@@@");
                            String[] filesDone = new String[splitString.length - 1];

                            for (int i = 0; i < splitString.length; i++) {
                                int x = i;
                                if (x + 1 == splitString.length) {
                                    finalText.append(splitString[i]);
                                } else {
                                    filesDone[i] = splitString[i];
                                }
                            }


                            new Thread(new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    Platform.runLater(() -> progressBar.setProgress(progressBar.getProgress() + ((double) 5 / finalSize)));
                                    return null;
                                }
                            }).start();
                            new Thread(new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    Platform.runLater(() -> doneList.getItems().addAll(filesDone));
                                    return null;
                                }
                            }).start();


                            List<String> fDoneAsList = Arrays.asList(filesDone);

                            int size = filesName.size();
                            int secSize = fDoneAsList.size();
                            for (int i = 0; i < size; i++) {
                                for (int z = 0; z < secSize; z++) {
                                    if (filesName.get(i).equals(fDoneAsList.get(z))) {
                                        filesName.remove(i);
                                        size--;
                                        i = -1;
                                        break;
                                    }
                                }
                            }

                            new Thread(new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    Platform.runLater(() -> readList.getItems().setAll(filesName));
                                    return null;
                                }
                            }).start();
                        } catch (Exception e) {
                            System.out.println("One of the sockets are closed");
                        }
                    }
                }

                executor.shutdown();

                writeToFile(finalText.toString());

                new Thread(new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> threadsProgress.setProgress(0));
                        return null;
                    }
                }).start();
            } catch ( Exception e) {
                System.out.println("Oh no, socket closed");;
            }
            if (!filesName.isEmpty()) {
                ArrayList<File> newFiles = new ArrayList<>();

                for (int i = 0; i < files.length; i++) {
                    for (String x : filesName) {
                        if (files[i].getName().toLowerCase().equals(x.toLowerCase())) {
                            newFiles.add(files[i]);
                        }
                    }
                }

                files = newFiles.toArray(new File[newFiles.size()]);
            }

        }
    }

    public void writeToFile(String text) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("C:\\Users\\s033860\\Desktop\\ats.txt")));

        bw.append(text);

        bw.close();
    }

    @Override
    public void run() {
        try {
            cycleThroughFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
