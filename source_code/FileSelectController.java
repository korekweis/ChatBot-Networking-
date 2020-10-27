import javax.swing.*;
import java.awt.event.*;
import javax.swing.filechooser.*;

public class FileSelectController {
    private FileSelectGUI fileSelectGui;
    private Client client;
    private String file;

    public FileSelectController(Client client, FileSelectGUI fileSelect) {

        this.fileSelectGui = fileSelect;
        this.client = client;

        this.fileSelectGui.addButtonListener(new Listener());
    }

    class Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("browse")) {
                JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp",
                        "tif", "jpeg", "wbmp"));

                chooser.setAcceptAllFileFilterUsed(false);

                int nFile = chooser.showOpenDialog(fileSelectGui.getFrame());

                if (nFile == JFileChooser.APPROVE_OPTION) {
                    fileSelectGui.setPath(chooser.getSelectedFile().getAbsolutePath());
                }

            } else if (e.getActionCommand().equals("send file")) {
                file = fileSelectGui.getPath();

                if (file.length() > 0) {
                    fileSelectGui.setPath("");
                    fileSelectGui.setFile(file);
                    fileSelectGui.connectFile(client.getIPNumber(), client.getPortNumber());
                    client.sendFile(file);
                    fileSelectGui.dispose();
                    client.getChatGui().enableSendFileButton(false);
                }
            }
        }
    }
}
