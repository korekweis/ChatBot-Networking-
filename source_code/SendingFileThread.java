import java.io.*;
import java.net.*;

public class SendingFileThread implements Runnable {

    private Socket socket;
    private DataOutputStream dos;
    private FileSelectGUI fileSelect;
    private String file;
    private final int BUFFER_SIZE = 100;

    public SendingFileThread(Socket soc, String file, FileSelectGUI fileSelect){
        this.socket = soc;
        this.file = file;
        this.fileSelect = fileSelect;
    }

    @Override
    public void run() {
        try {
            fileSelect.disableGUI(true);
//            System.out.println("Sending File...");
            dos = new DataOutputStream(socket.getOutputStream());

            File filename = new File(file);

            int len = (int) filename.length();
            int filesize = (int)Math.ceil(len / BUFFER_SIZE); // get the file size

            String clean_filename = filename.getName();

            //going to ClientHandler.java
            dos.writeUTF("SENDFILE "+ clean_filename.replace(" ", "_") +" "+ filesize);

            InputStream input = new FileInputStream(filename);
            OutputStream output = socket.getOutputStream();

            BufferedInputStream bis = new BufferedInputStream(input);

            byte[] buffer = new byte[BUFFER_SIZE];
            int count, percent = 0;

            while((count = bis.read(buffer)) > 0){
                output.write(buffer, 0, count);
            }

            fileSelect.setTitle("File was sent.!");
            fileSelect.successMessage("File successfully sent to the other client!", "Success");
            fileSelect.getChatGui().enableSendFileButton(true);

            output.flush();
            output.close();

//            System.out.println("File was sent..!");
        } catch (IOException e) {
//            System.out.println(e.getMessage());
        }
    }



}
