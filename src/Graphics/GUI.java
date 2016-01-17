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
import Operations.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;

public class GUI extends JFrame implements Runnable {

    //ATTRIBUTES OF GUI COMPONENTS
    private JMenu _fileMenu;
    private JMenu _helpMenu;
    private JMenuItem _exitMenuItem;
    private JMenuItem _optionsMenuItem;
    private JMenuItem _aboutMenuItem;
    private JMenuBar _menuBar;
    private JLabel _chooseModeLabel;
    private JTextArea _textOutput;
    private JTextField _outputPathTextField;
    private JButton _startButton;
    private JButton _abortButton;
    private JButton _selectButton;
    private JRadioButton _compressButton;
    private JRadioButton _decompressButton;
    private ButtonGroup _buttonGroup1;
    private JFileChooser _fileChooser;
    private FileNameExtensionFilter _extFilter;
    private JPanel _centerPanel;
    private JPanel _southernPanel;
    private JPanel _northernPanel;

    /**
     * The output path of the compressed archive. Stores the decoded standard
     * path of JAR-file location by default.
     */
    private String _outputPath;

    /**
     * Used to pause GUI-thread after archiving operation
     */
    private final PauseControl _pauseControl;

    /**
     * Associated class used for archiving operations
     */
    private AbstractAlgorithm _zipper;

    /**
     * The additional thread of this class
     */
    private Thread _guiThread;

    /**
     * The run flag to keep guiThread alive
     */
    private boolean _runFlag;

    /**
     * To determine whether operating system is Unix-based or not
     */
    public static boolean _isUnix;

    /**
     * To store the default icon of each frame
     */
    private static BufferedImage _frameIcon;

    /**
     * True if logging via options menu has been enabled
     */
    private static boolean _loggingEnabled;

    /**
     * True if user prefers to make classic zip-file instead of tar-archive
     */
    private static boolean _useClassicZipMode;

    /**
     * Constructor of this class for initialization of graphical user interface
     *
     * @param path The path of the JAR-file, which is the initial path
     */
    public GUI(String path) {
        _outputPath = path;
        _pauseControl = new PauseControl();
        initComponents();
    }

    /**
     * Initializes all the GUI components
     */
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
        _compressButton = new JRadioButton("Compress");
        _decompressButton = new JRadioButton("Decompress");
        _buttonGroup1 = new ButtonGroup();
        _fileChooser = new JFileChooser();
        _extFilter = new FileNameExtensionFilter(".tar.gz", "gz");
        _menuBar = new JMenuBar();
        _chooseModeLabel = new JLabel("Choose mode:");
        _outputPathTextField = new JTextField(_outputPath);
        _textOutput = new JTextArea();
        _centerPanel = new JPanel();
        _southernPanel = new JPanel();
        _northernPanel = new JPanel();

        /*set frame properties*/
        setLayout(new BorderLayout());
        setTitle("GZipper");
        setPreferredSize(new Dimension(600, 300));
        setIconImage(_frameIcon);
        setJMenuBar(_menuBar);

        /*define components*/
        add(_centerPanel, BorderLayout.CENTER);
        add(_southernPanel, BorderLayout.SOUTH);
        add(_northernPanel, BorderLayout.NORTH);
        _centerPanel.setLayout(new BorderLayout());
        _southernPanel.setLayout(new GridLayout());
        _northernPanel.setLayout(new GridLayout());

        _exitMenuItem.addActionListener((ActionEvent evt) -> {
            this.exitMenuActionPerformed(evt);
        });
        _optionsMenuItem.addActionListener((ActionEvent evt) -> {
            this.optionsMenuActionPerformed(evt);
        });
        _aboutMenuItem.addActionListener((ActionEvent evt) -> {
            this.aboutMenuActionPerformed(evt);
        });

        _textOutput.setText("run: \n");
        _textOutput.append("Output path can be changed in text field above" + "\n");
        _textOutput.setEditable(false);
        _textOutput.setBackground(Color.WHITE);

        _outputPathTextField.setBackground(Color.WHITE);

        _fileChooser.setFileHidingEnabled(true);

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

        _compressButton.addActionListener((ActionEvent evt) -> {
            this.compressButtonActionPerformed(evt);
        });

        _decompressButton.addActionListener((ActionEvent evt) -> {
            this.decompressButtonActionPerformed(evt);
        });

        _buttonGroup1.add(_compressButton);
        _buttonGroup1.add(_decompressButton);

        _menuBar.add(_fileMenu);
        _menuBar.add(_helpMenu);
        _fileMenu.add(_optionsMenuItem);
        _fileMenu.addSeparator();
        _fileMenu.add(_exitMenuItem);
        _helpMenu.add(_aboutMenuItem);

        _centerPanel.add(_outputPathTextField, BorderLayout.NORTH);
        _centerPanel.add(_selectButton, BorderLayout.SOUTH);
        _southernPanel.add(_startButton);
        _southernPanel.add(_abortButton);
        _northernPanel.add(_chooseModeLabel, BorderLayout.NORTH);
        _northernPanel.add(_compressButton, BorderLayout.NORTH);
        _northernPanel.add(_decompressButton, BorderLayout.NORTH);

        /*add scroll pane to text area*/
        JScrollPane sp = new JScrollPane(_textOutput,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _centerPanel.add(sp, BorderLayout.CENTER);

        DefaultCaret caret = (DefaultCaret) _textOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM); //for auto-scrolling

        pack();
        setLocationRelativeTo(null); //center position on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Creates a new thread if none is alive yet; will also toggle pauseControl
     * to let the thread start the archive operation (see run() method)
     *
     * @param evt The event that caused this method to be called
     */
    private void startButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _startButton) {

            String path = _outputPathTextField.getText();

            if (!path.equals(_outputPath)) {
                File f = new File(path);
                if (!f.exists() || !f.isDirectory()) {
                    _textOutput.append("Entered path does not exist "
                            + "or is no directory! Will use initial path instead.");
                } else {
                    path = validatePath(path);
                    _textOutput.append("New output path: " + path + "\n");
                    _outputPath = path; //set new output path
                }
            }
            _abortButton.setEnabled(true);
            if (_guiThread == null) {
                _runFlag = true;
                _guiThread = new Thread(this);
                _guiThread.start();
            }
            _pauseControl.unpause();
        }
    }

    /**
     * Tries to abort the running compressing/decompressing operation
     *
     * @param evt The event that caused this method to be called
     */
    private void abortButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _abortButton) {
            _textOutput.append("Trying to abort operation...\n");
            if (_zipper != null) {
                _pauseControl.pause();
                _zipper.interrupt();
                _zipper.stop();
                try {
                    if (_zipper.waitForExecutionEnd()) {
                        _zipper = null;
                    }
                } catch (InterruptedException ex) {
                    _textOutput.append(ex.toString() + "\n");
                }
                _textOutput.append("Operation aborted!\n");
            }
        }
    }

    /**
     * Opens a file dialog to either open an archive or to select (save) files
     * for compressing them into an archive
     *
     * @param evt The event that caused this method to be called
     */
    private void selectButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _selectButton) {
            if (_buttonGroup1.getSelection() != null) {
                _fileChooser.removeChoosableFileFilter(_extFilter);
                if (_compressButton.isSelected()) {
                    _fileChooser.setAcceptAllFileFilterUsed(true);
                    _fileChooser.setMultiSelectionEnabled(true);
                    _fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    _fileChooser.showSaveDialog(this);
                } else { //unzip
                    _fileChooser.setAcceptAllFileFilterUsed(false);
                    _fileChooser.setFileFilter(_extFilter);
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
                        _textOutput.append("\nIllegal characters found in file name!\n"
                                + "Characters not allowed: "
                                + "\\ / | : * \" ? < >\n");
                    } else {
                        _textOutput.append("Selection successful; ready to start compression\n");
                        _startButton.setEnabled(true);
                    }
                }
            } else {
                _textOutput.append("You need to choose a mode first!\n");
            }
        }
    }

    /**
     * Called if zip-mode has been selected
     *
     * @param evt The event that caused this method to be called
     */
    private void compressButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _compressButton) {
            _selectButton.setText("Select files/folders...");
        }
    }

    /**
     * Called if unzip-mode has been selected
     *
     * @param evt The event that caused this method to be called
     */
    private void decompressButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _decompressButton) {
            _selectButton.setText("Select archive...");
        }
    }

    /**
     * Called if options-menu has been selected
     *
     * @param evt The event that caused this method to be called
     */
    private void optionsMenuActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _optionsMenuItem) {
            JFrame frame = new JFrame("Options");
            JCheckBox loggingCheckBox = new JCheckBox("Enable logging (requires restart)");
            JCheckBox archiveTypeCheckBox = new JCheckBox("Switch to ZIP-mode (for session only)");
            if (_loggingEnabled) {
                loggingCheckBox.setSelected(true);
            }
            if (_useClassicZipMode) {
                archiveTypeCheckBox.setSelected(true);
            }
            loggingCheckBox.addActionListener((ActionEvent e) -> {
                if (e.getSource() == loggingCheckBox) {
                    /*updates config file on change of settings; 
                     as this file only contains two lines I chose to write them manually,
                     otherwise using List<String> would be a much handier solution*/
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream("gzipper.ini"), Charset.forName("UTF-8")))) {
                        bw.write("[OPTIONS]");
                        bw.newLine();
                        if (loggingCheckBox.isSelected()) {
                            bw.write("LoggingEnabled=true;");
                            _loggingEnabled = true;
                        } else {
                            bw.write("LoggingEnabled=false;");
                            _loggingEnabled = false;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Error writing config file!", ex);
                    }
                }
            });
            archiveTypeCheckBox.addActionListener((ActionEvent e) -> {
                /*this setting won't be stored in configuration file as 
                 this application's main purpose is to handle tar-archives*/
                if (e.getSource() == archiveTypeCheckBox) {
                    _useClassicZipMode = archiveTypeCheckBox.isSelected();
                    if (_useClassicZipMode) {
                        _extFilter = new FileNameExtensionFilter(".zip", "zip");
                    } else {
                        _extFilter = new FileNameExtensionFilter(".tar.gz", "gz");
                    }
                }
            });
            frame.setLayout(new GridLayout());
            frame.setMinimumSize(new Dimension(300, 100));
            frame.setResizable(false);
            frame.setIconImage(_frameIcon);
            frame.add(loggingCheckBox);
            frame.add(archiveTypeCheckBox);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        }
    }

    /**
     * Called if exit-menu has been selected
     *
     * @param evt The event that caused this method to be called
     */
    private void exitMenuActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _exitMenuItem) {
            setVisible(false);
            dispose();
            System.exit(0);
        }
    }

    /**
     * Called if about-menu has been selected
     *
     * @param evt The event that caused this method to be called
     */
    private void aboutMenuActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _aboutMenuItem) {
            JFrame frame = new JFrame("About");
            JLabel label = new JLabel("<html><br><p align=\"center\">"
                    + "<img src=\"file:" + _outputPath + "res/icon_256.png\" alt=\"res/icon_256.png\">"
                    + "<br>&nbsp;Author: Matthias Fussenegger&nbsp;<br>E-mail: matfu2@me.com<br><b>v2016-01-17</b></p>"
                    + "<br>&nbsp;This program uses parts of the commons-compress library by Apache Foundation&nbsp;<br>"
                    + "&nbsp;and is licensed under the GNU General Public License 3&nbsp;"
                    + "(<a href=\"http://www.gnu.org/licenses/\">http://www.gnu.org/licenses/</a>)"
                    + "&nbsp;<br>&nbsp;</html>", SwingConstants.CENTER);
            JButton button = new JButton("OK");
            button.addActionListener((ActionEvent e) -> {
                if (e.getActionCommand().equals("OK")) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
            button.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent evt) {
                    if (evt.getKeyCode() == 10) { //10 = return key
                        button.doClick();
                    }
                }
            });
            frame.setResizable(false);
            frame.setIconImage(_frameIcon);
            frame.add(label, BorderLayout.CENTER);
            frame.add(button, BorderLayout.SOUTH);
            frame.addKeyListener(null);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        }
    }

    /**
     * Starts the compressing operation
     *
     * @throws InterruptedException If an error occurred
     */
    private void startCompressing() throws InterruptedException {
        if (_zipper == null) {

            if (!_useClassicZipMode) { //tar.gz
                _zipper = new Gzip(_outputPath, "gzipper", _fileChooser.getSelectedFiles(), true);
            } else { //zip
                _zipper = new Zip(_outputPath, "gzipper", _fileChooser.getSelectedFiles(), true);
            }

            _zipper.start();

            if (_zipper.waitForExecutionEnd() & _zipper != null) {
                /*calculate elapsed time in seconds*/
                double seconds = ((double) _zipper.getElapsedTime() / 1E9);
                _textOutput.append("Job done!\n" + "Elapsed time: " + seconds + " seconds\n");
            }
        }
    }

    /**
     * Starts the decompressing operation
     *
     * @throws InterruptedException If an error occurred
     */
    private void startDecompressing() throws InterruptedException {
        if (_zipper == null) {

            _abortButton.setEnabled(true);
            String separator; //depending on operating systems

            if (_isUnix) {
                separator = "/";
            } else {
                separator = "\\";
            }
            if (!_useClassicZipMode) { //tar.gz
                _zipper = new Gzip(_fileChooser.getSelectedFile().getParent()
                        + separator, _fileChooser.getSelectedFile().getName(),
                        _fileChooser.getSelectedFiles(), false);
            } else { //zip
                _zipper = new Zip(_fileChooser.getSelectedFile().getParent()
                        + separator, _fileChooser.getSelectedFile().getName(),
                        _fileChooser.getSelectedFiles(), false);
            }

            _zipper.start();

            if (_zipper.waitForExecutionEnd() & _zipper != null) {
                /*calculate elapsed time in seconds*/
                double seconds = ((double) _zipper.getElapsedTime() / 1E9);
                _textOutput.append("Job done!\n" + "Elapsed time: " + seconds + " seconds\n");
            }
        }
    }

    @Override
    public void run() {
        while (_runFlag) {
            try {
                _pauseControl.pausePoint();
                if (_compressButton.isSelected()) {
                    _textOutput.append("Selected files/folders will be compressed, "
                            + "please wait...\n");
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
                _pauseControl.pause();
            }
        }
    }

    /**
     * Validates the file path based on operating system
     *
     * @param path The path to be validated
     * @return The new valid path
     */
    private String validatePath(String path) {
        String validPath = "";
        if (_isUnix) {
            for (int i = 0; i < path.length(); ++i) {
                if (path.charAt(i) == '\\') {
                    validPath = validPath + '/';
                } else {
                    validPath = validPath + path.charAt(i);
                }
            }
            if (validPath.charAt(validPath.length() - 1) != '/') {
                validPath = validPath + '/';
            }
        } else {
            for (int i = 0; i < path.length(); ++i) {
                if (path.charAt(i) == '/') {
                    validPath = validPath + '\\';
                } else {
                    validPath = validPath + path.charAt(i);
                }
            }
            if (validPath.charAt(validPath.length() - 1) != '\\') {
                validPath = validPath + '\\';
            }
        }
        return validPath;
    }

    /**
     *
     * @author Matthias Fussenegger
     * @param args The command line arguments
     * @version 2016-01-17
     */
    public static void main(String[] args) {
        try { //create file handler for logger if enabled via configuration
            _loggingEnabled = checkLoggerConfig();
        } catch (ConfigErrorException | IOException ex) {
            System.err.println(ex.toString());
        }

        /*String path and decPath are used to make directory where JAR-file is 
         located to the current working directory for creating a new archive*/
        String path = GUI.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        try {
            String decPath;
            if (System.getProperty("os.name").startsWith("Windows")) {
                /*decode path without adding the name of the JAR-file (GZipper.jar = 11 characters)
                 - for debugging inside an IDE please remove the minus operation (- 11)*/
                decPath = URLDecoder.decode(path.substring(1, path.length() - 11), "UTF-8");
            } else {
                _isUnix = true;
                decPath = path.substring(0, path.length() - 11);
            }
            /*get icon image for frame from res-folder in root application folder;
             do not forget to copy it to class files directory when debugging app,
             the image folder can be found in the main directory of the project*/
            FileInputStream imgStream = new FileInputStream(decPath + "/res/icon_32.png");
            _frameIcon = ImageIO.read(imgStream);

            /*set look & feel to system default*/
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            /*draw application frame*/
            java.awt.EventQueue.invokeLater(() -> {
                new GUI(decPath).setVisible(true);
            });
            Thread.sleep(300);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    /**
     * Checks the configuration file if logging has been enabled. If so, it will
     * create a new file handler for the logger. Logging can also be enabled via
     * the options menu of the GUI
     *
     * @return @throws ConfigErrorException If configuration file is corrupt
     * @throws IOException If an error while reading configuration file occurs
     */
    public static boolean checkLoggerConfig() throws ConfigErrorException, IOException {
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("gzipper.ini"), Charset.forName("UTF-8")))) {
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
