/*
 * Copyright (C) 2016 Matthias Fussenegger
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
package gzipper.graphics;

import gzipper.algorithms.*;
import gzipper.operations.*;
import gzipper.exceptions.ConfigErrorException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

/**
 * Main frame of application (graphical user interface)
 *
 * @author Matthias Fussenegger
 * @version 2016-10-22
 */
public class GUI extends JFrame implements Runnable {

    private static final long serialVersionUID = 1L;

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
    private ButtonGroup _modeButtonGroup;
    private JFileChooser _fileChooser;
    private FileNameExtensionFilter _extFilter;
    private JPanel _centerPanel;
    private JPanel _southernPanel;
    private JPanel _northernPanel;

    /**
     * The decoded path of JAR-file
     */
    private final String INITIAL_PATH;

    /**
     * Used to pause {@code _guiThread} after archiving operation
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
     * The run flag to keep {@code _guiThread} alive, see {@code run()}
     */
    private boolean _runFlag;

    /**
     * To parse the configuration file and for updating values
     */
    private static ConfigFileParser _configFileParser;

    /**
     * Constructor of this class to initialize graphical user interface
     *
     * @param path The path of the JAR-file, which is the {@code INITIAL_PATH}
     */
    public GUI(String path) {
        INITIAL_PATH = path;
        /*set recent path to output path if it exists and is valid*/
        if (FileValidator.isValidPath(Settings._outputPath)) {
            Settings._outputPath = new FileValidator().validatePath(Settings._outputPath);
        } else {
            Settings._outputPath = INITIAL_PATH;
        }
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
        _modeButtonGroup = new ButtonGroup();
        _fileChooser = new JFileChooser();
        _extFilter = new FileNameExtensionFilter(".tar.gz", "gz");
        _menuBar = new JMenuBar();
        _chooseModeLabel = new JLabel("Choose mode:");
        _outputPathTextField = new JTextField(Settings._outputPath);
        _textOutput = new JTextArea();
        _centerPanel = new JPanel();
        _southernPanel = new JPanel();
        _northernPanel = new JPanel();

        /*set frame properties*/
        setLayout(new BorderLayout());
        setTitle("GZipper");
        setPreferredSize(new Dimension(600, 300));
        setIconImage(Settings._frameIcon);
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
        _textOutput.append("Output path can be changed in text field above." + "\n");
        _textOutput.setEditable(false);
        _textOutput.setBackground(Color.WHITE);

        _outputPathTextField.setBackground(Color.WHITE);
        _outputPathTextField.setToolTipText("The output path of file(s)");

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

        _modeButtonGroup.add(_compressButton);
        _modeButtonGroup.add(_decompressButton);

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

            if (!path.equals(Settings._outputPath)) {
                if (!FileValidator.isValidPath(path)) {
                    _textOutput.append("Entered path does not exist "
                            + "or is no directory! Will use previous path instead.\n");
                } else {
                    path = new FileValidator().validatePath(path);
                    Settings._outputPath = path; //set new output path
                    _textOutput.append("New output path: " + path + "\n");
                    _configFileParser.updateValue("recentPath", path);
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
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
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
            if (_modeButtonGroup.getSelection() != null) {
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
                        _textOutput.append("Illegal characters found in file name!\n"
                                + "Characters not allowed: "
                                + "\\ / | : * \" ? < >\n");
                    } else {
                        _textOutput.append("Selection successful; ready to start operation\n");
                        _startButton.setEnabled(true);
                    }
                }
            } else {
                _textOutput.append("You need to choose a mode first!\n");
            }
        }
    }

    /**
     * Called if {@code _compressButton} has been selected
     *
     * @param evt The event that caused this method to be called
     */
    private void compressButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _compressButton) {
            _fileChooser.cancelSelection();
            _startButton.setEnabled(false);
            _selectButton.setText("Select files/folders...");
        }
    }

    /**
     * Called if {@code _decompressButton} has been selected
     *
     * @param evt The event that caused this method to be called
     */
    private void decompressButtonActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _decompressButton) {
            _fileChooser.cancelSelection();
            _startButton.setEnabled(false);
            _selectButton.setText("Select archive...");
        }
    }

    /**
     * Called if {@code _optionsMenuItem} has been selected
     *
     * @param evt The event that caused this method to be called
     */
    private void optionsMenuActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _optionsMenuItem) {
            SubFrame frame = new SubFrame("Options", new GridLayout());
            JCheckBox loggingCheckBox = new JCheckBox("Enable logging (requires restart)");
            JCheckBox archiveTypeCheckBox = new JCheckBox("Switch to ZIP-mode (for session only)");
            if (Settings._loggingEnabled) {
                loggingCheckBox.setSelected(true);
            }
            if (Settings._useClassicZipMode) {
                archiveTypeCheckBox.setSelected(true);
            }
            loggingCheckBox.addActionListener((ActionEvent e) -> {
                if (e.getSource() == loggingCheckBox) {
                    if (loggingCheckBox.isSelected()) {
                        _configFileParser.updateValue("LoggingEnabled", true);
                        Settings._loggingEnabled = true;
                    } else {
                        _configFileParser.updateValue("LoggingEnabled", false);
                        Settings._loggingEnabled = false;
                    }
                }
            });
            archiveTypeCheckBox.addActionListener((ActionEvent e) -> {
                /*this setting won't be stored in configuration file as 
                 this application's main purpose is to handle tar-archives*/
                if (e.getSource() == archiveTypeCheckBox) {
                    Settings._useClassicZipMode = archiveTypeCheckBox.isSelected();
                    if (Settings._useClassicZipMode) {
                        _extFilter = new FileNameExtensionFilter(".zip", "zip");
                    } else {
                        _extFilter = new FileNameExtensionFilter(".tar.gz", "gz");
                    }
                }
            });
            frame.addWithKeyListener(loggingCheckBox, KeyEvent.VK_ESCAPE);
            frame.addWithKeyListener(archiveTypeCheckBox, KeyEvent.VK_ESCAPE);
            frame.drawFrame();
        }
    }

    /**
     * Called if {@code _exitMenuItem} has been selected
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
     * Called if {@code _aboutMenuItem} has been selected
     *
     * @param evt The event that caused this method to be called
     */
    private void aboutMenuActionPerformed(ActionEvent evt) {
        if (evt.getSource() == _aboutMenuItem) {
            SubFrame frame = new SubFrame("About", new BorderLayout());
            JLabel label = new JLabel("<html><br><p align=\"center\">"
                    + "<img src=\"file:" + INITIAL_PATH + "res/icon_256.png\" alt=\"res/icon_256.png\">"
                    + "<br>&nbsp;Author: Matthias Fussenegger&nbsp;<br>E-mail: matfu2@me.com<br><b>v2016-10-22</b></p>"
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
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        button.doClick();
                    }
                }
            });
            frame.add(label, BorderLayout.CENTER);
            frame.add(button, BorderLayout.SOUTH);
            frame.drawFrame();
        }
    }

    /**
     * Starts the compressing operation
     *
     * @throws InterruptedException If an error occurred
     */
    private void startCompressing() throws InterruptedException {
        if (_zipper == null) {

            if (!Settings._useClassicZipMode) { //tar.gz
                _zipper = new Gzip(Settings._outputPath, "gzipper",
                        _fileChooser.getSelectedFiles(), true);
            } else { //zip
                _zipper = new Zip(Settings._outputPath, "gzipper",
                        _fileChooser.getSelectedFiles(), true);
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
            String separator; //depends on operating system

            if (Settings._isUnix) {
                separator = "/";
            } else {
                separator = "\\";
            }
            if (!Settings._useClassicZipMode) { //tar.gz
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
                    _textOutput.append("Selected files/folders will be compressed...\n"
                            + "This might take a while depending on size of file(s)\n");
                    startCompressing();
                } else { //unzip
                    _textOutput.append("Selected archive will be decompressed...\n");
                    startDecompressing();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                _abortButton.setEnabled(false);
                _zipper = null;
                _pauseControl.pause();
            }
        }
    }

    /**
     * Main method that will set up and draw application's main frame. It also
     * sets the icon image for the application frames and decodes the path of
     * JAR-file to receive location of configuration file and other resources
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {

        String path = GUI.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File f = new File(path);
        String decPath; //the decoded path of JAR-file

        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                decPath = URLDecoder.decode(path.substring(1, path.length() - f.getName().length()), "UTF-8");
            } else {
                Settings._isUnix = true;
                decPath = path.substring(0, path.length() - f.getName().length());
            }

            try { //parse configuration file
                _configFileParser = new ConfigFileParser(decPath);
                Settings._loggingEnabled = _configFileParser.checkValue("LoggingEnabled");
                Settings._outputPath = _configFileParser.getValue("recentPath");
                if (Settings._loggingEnabled) { //create new logger
                    Logger logger = Logger.getLogger((GUI.class.getName()));
                    FileHandler fh = new FileHandler(decPath + "logs/gzipper.log", true);
                    logger.addHandler(fh);
                }
            } catch (ConfigErrorException | IOException ex) {
                MessageBox.showErrorMessage(ex);
                System.exit(1);
            }

            try { //set look and feel to system default
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                MessageBox.showWarningMessage("Error setting Look & Feel to system default\n"
                        + "Applicaton will launch with standard Look & Feel");
                Logger.getLogger(GUI.class.getName()).log(Level.WARNING, "Error setting look and feel", ex);
            }

            try { //get icon image for frame from "res" folder in application directory
                FileInputStream imgStream = new FileInputStream(decPath + "res/icon_32.png");
                Settings._frameIcon = ImageIO.read(imgStream);
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Resources are missing", ex);
            }

            /*draw main application frame*/
            java.awt.EventQueue.invokeLater(() -> {
                new GUI(decPath).setVisible(true);
            });
            Thread.sleep(300);

        } catch (InterruptedException | UnsupportedEncodingException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
}
