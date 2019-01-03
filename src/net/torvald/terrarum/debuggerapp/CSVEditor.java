package net.torvald.terrarum.debuggerapp;

import net.torvald.terrarum.utils.CSVFetcher;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * Should be made into its own artifact to build.
 *
 * Only recognisable columns are read and saved, thus this app should be update when new properties are added.
 *
 * Created by minjaesong on 2019-01-01.
 */
public class CSVEditor extends JFrame {

    /** Default columns. When you open existing csv, it should overwrite this. */
    private String[] columns = new String[]{"id", "drop", "name", "shdr", "shdg", "shdb", "shduv", "str", "dsty", "mate", "solid", "plat", "wall", "fall", "dlfn", "fv", "fr", "lumr", "lumg", "lumb", "lumuv"};
    private final int FOUR_DIGIT = 42;
    private final int SIX_DIGIT = 50;
    private final int TWO_DIGIT = 30;
    private final int ARBITRARY = 240;
    private int[] colWidth = new int[]{FOUR_DIGIT, FOUR_DIGIT, ARBITRARY, SIX_DIGIT, SIX_DIGIT, SIX_DIGIT, SIX_DIGIT, TWO_DIGIT, FOUR_DIGIT, FOUR_DIGIT, TWO_DIGIT, TWO_DIGIT, TWO_DIGIT, TWO_DIGIT, TWO_DIGIT, TWO_DIGIT, TWO_DIGIT, SIX_DIGIT, SIX_DIGIT, SIX_DIGIT, SIX_DIGIT};

    private CSVFormat csvFormat = CSVFetcher.INSTANCE.getTerrarumCSVFormat();

    private final int INITIAL_ROWS = 2;

    private JPanel panelSpreadSheet = new JPanel();
    private JPanel panelComment = new JPanel();
    private JSplitPane panelWorking = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelSpreadSheet, panelComment);

    private JMenuBar menuBar = new JMenuBar();
    private JTable spreadsheet = new JTable(new DefaultTableModel(columns, INITIAL_ROWS)); // it MUST be DefaultTableModel because that's what I'm using
    private JTextPane caption = new JTextPane();
    private JTextPane comment = new JTextPane();
    private JLabel statBar = new JLabel("null.");

    private Properties props = new Properties();
    private Properties lang = new Properties();

    public CSVEditor() {
        // setup application properties //
        try {
            props.load(new StringReader(captionProperties));
            lang.load(new StringReader(translations));
        }
        catch (Throwable e) {

        }

        // setup layout //

        this.setLayout(new BorderLayout());
        panelSpreadSheet.setLayout(new BorderLayout());
        panelComment.setLayout(new BorderLayout());

        spreadsheet.setVisible(true);

        caption.setVisible(true);
        caption.setEditable(false);
        caption.setContentType("text/html");
        caption.setText("<span style=\"font:sans-serif; color:#888888; font-style:italic;\">Description of the selected column will be displayed here.</span>");

        comment.setVisible(true);
        comment.setPreferredSize(new Dimension(100, 220));
        comment.setText("# This is a comment section.\n# All the comment must begin with this '#' mark.\n# null value on the CSV is represented as 'N/A'.");

        panelSpreadSheet.add(menuBar, BorderLayout.NORTH);
        panelSpreadSheet.add(new JScrollPane(spreadsheet, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
        ), BorderLayout.CENTER);
        panelComment.add(caption, BorderLayout.NORTH);
        panelComment.add(new JScrollPane(comment, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
        ), BorderLayout.CENTER);
        this.add(statBar, BorderLayout.SOUTH);
        this.add(panelWorking, BorderLayout.CENTER);
        this.add(menuBar, BorderLayout.NORTH);

        this.setTitle("Terrarum CSV Editor");
        this.setVisible(true);
        this.setSize(1154, 768);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // setup menubar //

        menuBar.add(new JMenu("File") {
            {
                add("Open…").addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        // let's show generic warning first
                        if (discardAgreed()) {

                            // actually read file
                            JFileChooser fileChooser = new JFileChooser() {
                                {
                                    setFileSelectionMode(JFileChooser.FILES_ONLY);
                                    setMultiSelectionEnabled(false);
                                }
                            };

                            fileChooser.showOpenDialog(null);

                            if (fileChooser.getSelectedFile() != null) {
                                if (fileChooser.getSelectedFile().exists()) {
                                    List<CSVRecord> records = CSVFetcher.INSTANCE.readFromFile(
                                            fileChooser.getSelectedFile().getAbsolutePath());

                                    // turn list of records into a spreadsheet

                                    // first dispose of any existing data
                                    ((DefaultTableModel) spreadsheet.getModel()).setRowCount(0);

                                    // then work on the file
                                    for (CSVRecord record : records) {
                                        Vector newRow = new Vector(columns.length);

                                        // construct newRow
                                        for (String column : columns) {
                                            String value = record.get(column);
                                            if (value == null) {
                                                value = csvFormat.getNullString();
                                            }

                                            newRow.add(spreadsheet.getColumnModel().getColumnIndex(column), value);
                                        }

                                        ((DefaultTableModel) spreadsheet.getModel()).addRow(newRow);
                                    }

                                    // then add the comments
                                    // since the Commons CSV simply ignores the comments, we have to read them on our own.
                                    try {
                                        StringBuilder sb = new StringBuilder();
                                        List<String> allTheLines = Files.readAllLines(
                                                fileChooser.getSelectedFile().toPath());

                                        allTheLines.forEach(line -> {
                                            if (line.startsWith("" + csvFormat.getCommentMarker().toString())) {
                                                sb.append(line);
                                                sb.append('\n');
                                            }
                                        });

                                        comment.setText(sb.toString());

                                        statBar.setText(lang.getProperty("STAT_LOAD_SUCCESSFUL"));
                                    }
                                    catch (Throwable fuck) {
                                        displayError("ERROR_INTERNAL", fuck);
                                    }
                                }
                                // if file not found
                                else {
                                    displayMessage("NO_SUCH_FILE");
                                }
                            } // if opening cancelled, do nothing
                        } // if discard cancelled, do nothing
                    }
                });

                add("Save…").addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        JFileChooser fileChooser = new JFileChooser() {
                            {
                                setFileSelectionMode(JFileChooser.FILES_ONLY);
                                setMultiSelectionEnabled(false);
                            }
                        };

                        fileChooser.showSaveDialog(null);

                        if (fileChooser.getSelectedFile() != null) {
                            try {
                                FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile());

                                fos.write(toCSV().getBytes());

                                fos.flush();
                                fos.close();


                                statBar.setText(lang.getProperty("STAT_SAVE_SUCCESSFUL"));
                            }
                            catch (IOException iofuck) {
                                displayError("WRITE_FAIL", iofuck);
                            }
                        } // if saving cancelled, do nothing
                    }
                });

                add("New").addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (discardAgreed()) {
                            // ask new rows
                            Integer rows = askInteger("NEW_ROWS");

                            if (rows != null) {
                                // first, delete everything
                                ((DefaultTableModel) spreadsheet.getModel()).setRowCount(0);

                                // then add some columns
                                ((DefaultTableModel) spreadsheet.getModel()).setRowCount(rows);

                                // notify the user as well
                                statBar.setText(lang.getProperty("STAT_NEW_FILE"));
                            }
                        }
                    }
                });
            }
        });
        menuBar.add(new JMenu("Edit") {
            {
                add("New rows…");
                add("New column…");
                add("Delete current row");
                add("Delete current column");
            }
        });

        menuBar.revalidate();

        // setup spreadsheet //

        // no resize
        spreadsheet.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // set column width
        for (int i = 0; i < columns.length; i++) {
            spreadsheet.getColumnModel().getColumn(i).setPreferredWidth(colWidth[i]);
        }
        // make tables do things
        spreadsheet.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

            }
        });
        // make tables do things
        spreadsheet.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // make caption line working
                JTable table = ((JTable) e.getSource());
                int col = table.getSelectedColumn();
                String colName = table.getColumnName(col);
                String captionText = props.getProperty(colName);

                caption.setText("<span style=\"font:sans-serif;\"><b>" + colName + "</b><span style=\"color:#404040;\">" +
                        ((captionText == null) ? "" : ": " + captionText) +
                        "</span></span>"
                );
            }
        });


        statBar.setText(lang.getProperty("STAT_INIT"));
    }

    public static void main(String[] args) {
        new CSVEditor();
    }

    private String toCSV() {
        StringBuilder sb = new StringBuilder();

        int cols = spreadsheet.getColumnModel().getColumnCount();
        int rows = spreadsheet.getRowCount(); // actual rows, not counting the titles row

        // add all the column titles
        for (int i = 0; i < cols; i++) {
            sb.append('"');
            sb.append(spreadsheet.getColumnName(i));
            sb.append('"');
            if (i + 1 < cols) sb.append(';');
        } sb.append('\n');

        // loop for all the rows
        forEachRow:
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Object rawValue = spreadsheet.getModel().getValueAt(row, col);

                String cell;
                if (rawValue == null)
                    cell = "";
                else
                    cell = ((String) rawValue).toUpperCase();

                // skip if ID cell is empty
                if (col == 0 && cell.isEmpty()) {
                    continue forEachRow;
                }

                sb.append('"');
                sb.append(cell);
                sb.append('"');
                if (col + 1 < cols) sb.append(';');
            }
            sb.append("\n");
        } sb.append("\n\n");

        // add comments
        sb.append(comment.getText());


        return sb.toString();
    }

    private boolean discardAgreed() {
        return 0 == JOptionPane.showOptionDialog(null,
                lang.getProperty("WARNING_YOUR_DATA_WILL_GONE") + " " + lang.getProperty("WARNING_CONTINUE"),
                null,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"OK", "Cancel"},
                "Cancel"
        );
    }
    private boolean confirmedContinue(String messageKey) {
        return 0 == JOptionPane.showOptionDialog(null,
                lang.getProperty(messageKey) + " " + lang.getProperty("WARNING_CONTINUE"),
                null,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"OK", "Cancel"},
                "Cancel"
        );
    }
    private void displayMessage(String messageKey) {
        JOptionPane.showOptionDialog(null,
                lang.getProperty(messageKey),
                null,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"OK", "Cancel"},
                "Cancel"
        );
    }
    private void displayError(String messageKey, Throwable cause) {
        JOptionPane.showOptionDialog(null,
                lang.getProperty(messageKey) + "\n" + cause.toString(),
                null,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"OK", "Cancel"},
                "Cancel"
        );
    }

    /**
     *
     * @param messageKey
     * @return null if the operation cancelled, nonzero int if the choice was made
     */
    private Integer askInteger(String messageKey) {
        OptionSize optionWindow = new OptionSize(lang.getProperty(messageKey));
        int confirmedStatus = optionWindow.showDialog();

        if (confirmedStatus == JOptionPane.CANCEL_OPTION) {
            return null;
        }
        else {
            return ((Integer) optionWindow.capacity.getValue());
        }
    }

    private String captionProperties =
            "" + // dummy string to make IDE happy with the auto indent
                    "id=ID of this block\n" +
                    "drop=ID of the block this very block should drop when mined\n" +
                    "name=String identifier of the block\n" +
                    "shdr=Shade Red (light absorption). Valid range 0.0-4.0\n" +
                    "shdg=Shade Green (light absorption). Valid range 0.0-4.0\n" +
                    "shdb=Shade Blue (light absorption). Valid range 0.0-4.0\n" +
                    "shduv=Shade UV (light absorbtion). Valid range 0.0-4.0\n" +
                    "lumr=Luminosity Red (light intensity). Valid range 0.0-4.0\n" +
                    "lumg=Luminosity Green (light intensity). Valid range 0.0-4.0\n" +
                    "lumb=Luminosity Blue (light intensity). Valid range 0.0-4.0\n" +
                    "lumuv=Luminosity UV (light intensity). Valid range 0.0-4.0\n" +
                    "str=Strength of the block\n" +
                    "dsty=Density of the block. Water have 1000 in the in-game scale\n" +
                    "mate=Material of the block\n" +
                    "solid=Whether the file has full collision\n" +
                    "plat=Whether the block should behave like a platform\n" +
                    "wall=Whether the block can be used as a wall\n" +
                    "fall=Whether the block should fall through the empty space\n" +
                    "dlfn=Dynamic Light Function. 0=Static. Please see <strong>notes</strong>\n" +
                    "fv=Vertical friction when player slide on the cliff. 0 means not slide-able\n" +
                    "fr=Horizontal friction. &lt;16:slippery 16:regular &gt;16:sticky\n";

    /**
     * ¤ is used as a \n marker
     */
    private String translations =
            "" +
                    "WARNING_CONTINUE=Continue?\n" +
                    "WARNING_YOUR_DATA_WILL_GONE=Existing edits will be lost.\n" +
                    "OPERATION_CANCELLED=Operation cancelled.\n" +
                    "NO_SUCH_FILE=No such file exists, operation cancelled.\n" +
                    "NEW_ROWS=Enter the number of rows to initialise the new CSV.¤Remember, you can always add or delete rows later.\n" +
                    "ADD_ROWS=Enter the number of rows to add:\n" +
                    "WRITE_FAIL=Writing to file has failed:\n" +
                    "STAT_INIT=Creating a new CSV. You can still open existing file.\n" +
                    "STAT_SAVE_SUCCESSFUL=File saved successfully.\n" +
                    "STAT_NEW_FILE=New CSV created.\n" +
                    "STAT_LOAD_SUCCESSFUL=File loaded successfully.\n" +
                    "ERROR_INTERNAL=Something went wrong.\n";
}

class OptionSize {
    JSpinner capacity = new JSpinner(new SpinnerNumberModel(
            10,
            1,
            4096,
            1
    ));
    private JPanel settingPanel = new JPanel();

    OptionSize(String message) {
        settingPanel.add(new JLabel("<html>" + message.replace("¤", "<br />") + "</html>"));
        settingPanel.add(capacity);
    }

    /**
     * returns either JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION
     */
    int showDialog() {
        return JOptionPane.showConfirmDialog(null, settingPanel,
                null, JOptionPane.OK_CANCEL_OPTION);
    }
}
