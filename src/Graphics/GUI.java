/*
 * Copyright (C) 2015 Matthias Fussenegger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Graphics;

import Exceptions.ConfigErrorException;
import Operations.PauseControl;
import Operations.Zipper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;

public class GUI extends JFrame implements Runnable {

    //ATTRIBUTES
    private JMenu _fileMenu;
    private JMenu _helpMenu;
    private JMenuItem _exitMenuItem;
    private JMenuItem _optionsMenuItem;
    private JMenuItem _aboutMenuItem;
    private JMenuBar _menuBar;
    private JLabel _label1;
    private JTextArea _textOutput;
    private JButton _startButton;
    private JButton _abortButton;
    private JButton _selectButton;
    private JRadioButton _archiveModeZip;
    private JRadioButton _archiveModeUnzip;
    private ButtonGroup _buttonGroup1;
    private JFileChooser _fileChooser;
    private FileNameExtensionFilter _filter;
    private JPanel _panel1; //center panel
    private JPanel _panel2; //southern panel
    private JPanel _panel3; //northern panel

    private final String INITIAL_PATH; //the decoded standard path of JAR-file
    private final PauseControl _ps; //to pause GUI-Thread after operation
    private Zipper _zipper; //associated object for archive operations
    private Thread _guiThread;
    private boolean _runFlag;

    public static boolean _isUnix; //to check whether OS is unix or not
    private static BufferedImage _ico; //the icon for the frame
    private static boolean _loggingEnabled; //true if logging is enabled in "gzipper.ini"

    //CONSTRUCTOR
    public GUI(String path) {
        INITIAL_PATH = path;
        _ps = new PauseControl();
        initComponents();
    }

    //METHODS
    private void initComponents() {
        /*initialize fields*/
        _fileMenu = new JMenu("File");
        _helpMenu = new JMenu("Help");
        _exitMenuItem = new JMenuItem("Exit");
        _optionsMenuItem = new JMenuItem("Options");
        _aboutMenuItem = new JMenuItem("About");
        _startButton = new JButton("Start");
        _abortButton = new JButton("Abort");
        _selectButton = new JButton("Select...");
        _archiveModeZip = new JRadioButton("Compress");
        _archiveModeUnzip = new JRadioButton("Decompress");
        _buttonGroup1 = new ButtonGroup();
        _fileChooser = new JFileChooser();
        _filter = new FileNameExtensionFilter(".tar.gz", "gz");
        _menuBar = new JMenuBar();
        _label1 = new JLabel("Choose ZIP-mode:");
        _textOutput = new JTextArea();
        _panel1 = new JPanel();
        _panel2 = new JPanel();
        _panel3 = new JPanel();

        /*set frame properties*/
        setLayout(new BorderLayout());
        setTitle("GZipper");
        setPreferredSize(new Dimension(520, 250));
        setIconImage(_ico);
        setJMenuBar(_menuBar);

        /*define components*/
        add(_panel1, BorderLayout.CENTER);
        add(_panel2, BorderLayout.SOUTH);
        add(_panel3, BorderLayout.NORTH);
        _panel1.setLayout(new BorderLayout());
        _panel2.setLayout(new GridLayout());
        _panel3.setLayout(new GridLayout());

        _exitMenuItem.addActionListener((ActionEvent evt) -> {
            this.exitMenuActionPerformed(evt);
        });
        _optionsMenuItem.addActionListener((ActionEvent evt) -> {
            this.optionsMenuActionPerformed(evt);
        });
        _aboutMenuItem.addActionListener((ActionEvent evt) -> {
            this.aboutMenuActionPerformed(evt);
        });

        _textOutput.setFont(new Font("Consolas", Font.PLAIN, 12));
        _textOutput.setText("run: \n");
        _textOutput.append("Output path: " + INITIAL_PATH + "\n");
        _textOutput.setEditable(false);
        _textOutput.setBackground(Color.WHITE);

        _startButton.setEnabled(false);
        _startButton.addActionListener((ActionEvent evt) -> {
            this.startButtonActionPerformed(evt);
        });

        _abortButton.setEnabled(false);
        _abortButton.addActionListener((ActionEvent evt) -> {
            this.abortButtonActionPerformed(evt);
        });

        _selectButton.addActionListener((ActionEvent evt) -> {
            this.selectButtonActionPerformed(evt);
        });

        _archiveModeZip.addActionListener((ActionEvent evt) -> {
            this.archiveModeZipButtonActionPerformed(evt);
        });

        _archiveModeUnzip.addActionListener((ActionEvent evt) -> {
            this.archiveModeUnzipButtonActionPerformed(evt);
        });

        _buttonGroup1.add(_archiveModeZip);
        _buttonGroup1.add(_archiveModeUnzip);

        _menuBar.add(_fileMenu);
        _menuBar.add(_helpMenu);
        _fileMenu.add(_optionsMenuItem);
        _fileMenu.addSeparator();
        _fileMenu.add(_exitMenuItem);
        _helpMenu.add(_aboutMenuItem);

        _panel1.add(_textOutput, BorderLayout.CENTER);
        _panel1.add(_selectButton, BorderLayout.SOUTH);
        _panel2.add(_startButton);
        _panel2.add(_abortButton);
        _panel3.add(_label1, BorderLayout.NORTH);
        _panel3.add(_archiveModeZip, BorderLayout.NORTH);
        _panel3.add(_archiveModeUnzip, BorderLayout.NORTH);

        /*add scroll pane to text area*/
        JScrollPane sp = new JScrollPane(_textOutput,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _panel1.add(sp, BorderLayout.CENTER);

        DefaultCaret caret = (DefaultCaret) _textOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM); //for auto-scrolling

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /*this method will create a new thread if none is alive yet;
     it also toggles PauseControl to let Thread start the archive operation*/
    private void startButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _startButton) {
            _abortButton.setEnabled(true);
            if (_guiThread == null) {
                _runFlag = true;
                _guiThread = new Thread(this);
                _guiThread.start();
            }
            _ps.unpause();
        }
    }

    /*trys to abort the compressing/decompressing operation*/
    private void abortButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _abortButton) {
            _textOutput.append("Trying to abort operation...\n");
            if (_zipper != null) {
                _ps.pause();
                _zipper.interrupt();
                _zipper.stop();
                try {
                    if (_zipper.waitForExecEnd() != false) {
                        _zipper = null;
                    }
                } catch (InterruptedException ex) {
                    _textOutput.append(ex.toString() + "\n");
                }
                _textOutput.append("Operation aborted!\n");
            }
        }
    }

    /*opens a file dialog to either open an archive 
     or to select (save) files to compress them into an archive*/
    private void selectButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _selectButton) {
            if (_buttonGroup1.getSelection() != null) {

                if (_archiveModeZip.isSelected()) {
                    _fileChooser.setAcceptAllFileFilterUsed(true);
                    _fileChooser.removeChoosableFileFilter(_filter);
                    _fileChooser.setMultiSelectionEnabled(true);
                    _fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    _fileChooser.showSaveDialog(this);
                } else { //unzip
                    _fileChooser.setAcceptAllFileFilterUsed(false);
                    _fileChooser.setFileFilter(_filter);
                    _fileChooser.setMultiSelectionEnabled(false);
                    _fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    _fileChooser.showOpenDialog(this);
                }
                if (_fileChooser.getSelectedFile() != null) {
                    /*check file name for illegal characters*/
                    String filename = _fileChooser.getSelectedFile().getName();
                    if (filename.contains("<") || filename.contains(">") || filename.contains("/")
                            || filename.contains("\\") || filename.contains("|") || filename.contains(":")
                            || filename.contains("*") || filename.contains("\"") || filename.contains("?")) {
                        drawNewWindow("Warning", "<html><br><p align=\"center\">"
                                + "&nbsp;Illegal characters found in file name!"
                                + "<br><br>&nbsp;<u>Characters not allowed:</u> "
                                + "\\ / | : * \" ? &lt; >&nbsp;<br>&nbsp;</p></html>");
                    } else {
                        _textOutput.append("Selection successful; ready to start compression\n");
                        _startButton.setEnabled(true);
                    }
                }
            } else {
                drawNewWindow("Warning", "<html><br><p align=\"center\">"
                        + "&nbsp;You need to select a ZIP-mode first!"
                        + "&nbsp;</p><br></html>");
            }
        }
    }

    private void archiveModeZipButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _archiveModeZip) {
            _selectButton.setText("Select files/folders...");
        }
    }

    private void archiveModeUnzipButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _archiveModeUnzip) {
            _selectButton.setText("Select archive...");
        }
    }

    private void optionsMenuActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _optionsMenuItem) {
            JFrame frame = new JFrame("Options");
            JCheckBox checkBox = new JCheckBox("Enable logging (requires restart)");
            checkBox.setFont(new Font("Consolas", Font.BOLD, 12));
            if (_loggingEnabled != false) {
                checkBox.setSelected(true);
            }
            checkBox.addActionListener((ActionEvent e) -> {
                if (e.getSource() == checkBox) {
                    /*updates config file on change of settings; 
                     as this file only contains two lines I chose to write them manually,
                     otherwise a List<String> would be a much handier solution*/
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gzipper.ini"), Charset.forName("UTF-8")))) {
                        bw.write("[OPTIONS]");
                        bw.newLine();
                        if (checkBox.isSelected()) {
                            bw.write("LoggingEnabled=true;");
                        } else {
                            bw.write("LoggingEnabled=false;");
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, ex.toString(), ex);
                    }
                }
            });
            frame.setPreferredSize(new Dimension(320, 90));
            frame.setResizable(false);
            frame.setIconImage(_ico);
            frame.add(checkBox, BorderLayout.CENTER);
            frame.addKeyListener(null);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        }
    }

    private void exitMenuActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _exitMenuItem) {
            setVisible(false);
            dispose();
            System.exit(0);
        }
    }

    private void aboutMenuActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _aboutMenuItem) {
            drawNewWindow("About", "<html><br><p align=\"center\">"
                    + "<img src=\"file:" + INITIAL_PATH + "res/icon_256.png\" alt=\"res/icon_256.png\">"
                    + "<br>&nbsp;Author: Matthias Fussenegger&nbsp;<br>E-mail: matfu2@me.com<br><b>v0.6.5</b></p>"
                    + "<br>&nbsp;This program uses parts of the commons-compress library by Apache Foundation&nbsp;<br>"
                    + "&nbsp;and is licensed under the GNU General Public License 3&nbsp;"
                    + "(<a href=\"http://www.gnu.org/licenses/\">http://www.gnu.org/licenses/</a>)&nbsp;<br>&nbsp;</html>");
        }
    }

    /*draw new window with predefinded title and text*/
    private void drawNewWindow(String title, String text) {
        JFrame frame = new JFrame(title);
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        JButton button = new JButton("OK");
        button.addActionListener((ActionEvent evt) -> {
            if (evt.getActionCommand().equals("OK")) {
                frame.setVisible(false);
                frame.dispose();
            }
        });
        label.setFont(new Font("Consolas", Font.BOLD, 12));
        button.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == 10) { //10 = return key
                    button.doClick();
                }
            }
        });
        frame.setResizable(false);
        frame.setIconImage(_ico);
        frame.add(label, BorderLayout.CENTER);
        frame.add(button, BorderLayout.SOUTH);
        frame.addKeyListener(null);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private void startCompressing() throws InterruptedException {
        if (_zipper == null) {
            _zipper = new Zipper(INITIAL_PATH, "gzipper", _fileChooser.getSelectedFiles(), true);
            try {
                _zipper.start();
            } catch (IOException ex) {
                _textOutput.append(ex.toString() + "\n");
            } finally {
                if (_zipper.waitForExecEnd() != false & _zipper != null) {
                    double seconds = ((double) _zipper.getElapsedTime() / 1E9); //calculate elapsed time in seconds
                    _textOutput.append("Job done!\n" + "Elapsed time: " + seconds + " seconds\n");
                }
            }
        }
    }

    private void startDecompressing() throws InterruptedException {
        if (_zipper == null) {
            _abortButton.setEnabled(true);
            if (_isUnix) { //check for OS
                _zipper = new Zipper(_fileChooser.getSelectedFile().getParent() + "/",
                        _fileChooser.getSelectedFile().getName(), _fileChooser.getSelectedFiles(), false);
            } else {
                _zipper = new Zipper(_fileChooser.getSelectedFile().getParent() + "\\",
                        _fileChooser.getSelectedFile().getName(), _fileChooser.getSelectedFiles(), false);
            }
            try {
                _zipper.start();
            } catch (IOException ex) {
                _textOutput.append(ex.toString() + "\n");
            } finally {
                if (_zipper.waitForExecEnd() != false & _zipper != null) {
                    double seconds = ((double) _zipper.getElapsedTime() / 1E9); //calculate elapsed time in seconds
                    _textOutput.append("Job done!\n" + "Elapsed time: " + seconds + " seconds\n");
                }
            }
        }
    }

    @Override
    public void run() {
        while (_runFlag) {
            try {
                _ps.pausePoint();
                if (_archiveModeZip.isSelected()) {
                    _textOutput.append("Selected files/folders will be compressed, please wait...\n");
                    startCompressing();
                } else { //unzip
                    _textOutput.append("Selected archive will be decompressed...\n");
                    startDecompressing();
                }
            } catch (InterruptedException ex) {
                _textOutput.append(ex.toString() + "\n");
            } finally {
                _abortButton.setEnabled(false);
                _zipper = null;
                _ps.pause();
            }
        }
    }

    /**
     *
     * @author Matthias Fussenegger
     * @param args
     * @version 0.6.5
     */
    public static void main(String[] args) {
        /*create file handler for logger if enabled via configuration*/
        try {
            _loggingEnabled = checkLoggerConfig();
        } catch (ConfigErrorException | IOException ex) {
            System.err.println(ex.toString());
        }

        /*String path and decPath used to make directory where JAR-file is located
         to the current working directory for creating a new archive*/
        String path = GUI.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        try {
            String decPath;
            if (System.getProperty("os.name").startsWith("Windows")) {
                /*decode path without adding the name of the JAR-file (GZipper.jar = 11 characters)
                 - for debugging inside an IDE please remove the minus operation (- 11) and make sure
                 to rename archive name (currently: gzipper) when starting to compress/decompress*/
                decPath = URLDecoder.decode(path.substring(1, path.length() - 11), "UTF-8");
            } else {
                _isUnix = true;
                decPath = path.substring(0, path.length() - 11);
            }
            /*get icon image for frame from root application folder;
             do not forget to copy it to class files directory when debugging app,
             the image can be found in the main directory of the project*/
            FileInputStream imgStream = new FileInputStream(decPath + "/res/icon_32.png");
            _ico = ImageIO.read(imgStream);
            /*draw application frame*/
            java.awt.EventQueue.invokeLater(() -> {
                new GUI(decPath).setVisible(true);
            });
            Thread.sleep(300);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            System.exit(1);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    /*check config file if logging has been enabled;
     if so, create new file handler for logger*/
    public static boolean checkLoggerConfig() throws ConfigErrorException, IOException {
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("gzipper.ini"), Charset.forName("UTF-8")))) {
            while ((line = br.readLine()) != null) {
                if (line.startsWith("LoggingEnabled=")) {
                    if (line.endsWith("true;")) {
                        /*create new logger*/
                        Logger logger = Logger.getLogger((GUI.class.getName()));
                        FileHandler fh = new FileHandler("logs/gzipper.log", true);
                        logger.addHandler(fh);
                        return true;
                    } else if (line.endsWith("false;")) {
                        return false;
                    } else {
                        throw new ConfigErrorException();
                    }
                }
            }
        }
        return false;
    }
}
