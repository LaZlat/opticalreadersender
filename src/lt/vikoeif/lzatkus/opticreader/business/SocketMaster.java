package lt.vikoeif.lzatkus.opticreader.business;

import java.io.*;

import java.net.Socket;
import java.util.concurrent.Callable;

public class SocketMaster implements Callable<String> {

    static String HOST = "localhost";
    static int PORT = 5000;

    private File[] files;
    private String address;
    private int port;
    private StringBuilder pureText = new StringBuilder();

    public SocketMaster(File[] files, String address, int port) {
        this.files = files;
        this.address = address;
        this.port = port;
    }

    public String sendFile(File[] files, String address, int port) throws IOException {

            Socket socket =  new Socket(address, port);
            System.out.println("Connected");

            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            DataOutputStream dos = new DataOutputStream(out);

            dos.writeInt(files.length);

            for (File x : files) {
                dos.writeLong(x.length());
                dos.writeUTF(x.getName());

                FileInputStream fis = new FileInputStream(x);
                BufferedInputStream bis = new BufferedInputStream(fis);

                int theByte = 0;
                while ((theByte = bis.read()) != -1) {
                    out.write(theByte);
                }

                bis.close();
            }
            
            socket.shutdownOutput();

            InputStream in = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(in);

            pureText.append(dataInputStream.readUTF());

            System.out.println("Closing socket");
            socket.shutdownInput();

            socket.close();

            return pureText.toString();
    }

    @Override
    public String call() throws Exception {
        return sendFile(files,address,port);
    }
}
