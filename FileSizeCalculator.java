import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.*;

public class FileSizeCalculator extends JFrame {

    private JButton btnOpen = new JButton("Open File...");
    private JTextArea previewArea = new JTextArea();
    private JLabel lblFileName = new JLabel("No file selected");
    private JLabel lblFileSize = new JLabel("Size: -");
    private JLabel lblWordCount = new JLabel("Words: -");
    private JLabel lblCharCount = new JLabel("Chars (incl. spaces): -");
    private JLabel lblNonSpaceChars = new JLabel("Chars (excl. spaces): -");
    private JLabel lblLongestWord = new JLabel("Longest word: -");
    private JLabel lblAvgWordLen = new JLabel("Average word length: -");

    public FileSizeCalculator() {
        super("File Size & Text Statistics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(previewArea);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeft.add(btnOpen);
        top.add(topLeft, BorderLayout.WEST);
        top.add(lblFileName, BorderLayout.CENTER);

        JPanel stats = new JPanel();
        stats.setLayout(new GridLayout(0, 1, 4, 4));
        stats.setBorder(BorderFactory.createTitledBorder("Statistics"));
        stats.add(lblFileSize);
        stats.add(lblWordCount);
        stats.add(lblCharCount);
        stats.add(lblNonSpaceChars);
        stats.add(lblLongestWord);
        stats.add(lblAvgWordLen);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(8, 8));
        cp.add(top, BorderLayout.NORTH);
        cp.add(scroll, BorderLayout.CENTER);
        cp.add(stats, BorderLayout.EAST);

        btnOpen.addActionListener(e -> onOpenFile());

        setVisible(true);
    }

    private void onOpenFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
        chooser.setFileFilter(filter);
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        lblFileName.setText(file.getName());

        // File size
        long bytes = file.length();
        lblFileSize.setText("Size: " + formatFileSize(bytes));

        try {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            previewArea.setText(text);
            analyzeText(text);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error reading file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void analyzeText(String text) {
        int totalChars = text.length();
        int nonSpaceChars = text.replaceAll("\\s+", "").length();

        Pattern wordPattern = Pattern.compile("\\b[\\p{L}\\p{N}']+\\b");
        Matcher m = wordPattern.matcher(text);

        int wordCount = 0;
        int totalWordLetters = 0;
        String longestWord = "";
        while (m.find()) {
            String w = m.group();
            wordCount++;
            totalWordLetters += w.length();
            if (w.length() > longestWord.length()) {
                longestWord = w;
            }
        }

        double avgWordLen = (wordCount > 0) ? ((double) totalWordLetters / wordCount) : 0.0;

        lblCharCount.setText("Chars (incl. spaces): " + totalChars);
        lblNonSpaceChars.setText("Chars (excl. spaces): " + nonSpaceChars);
        lblWordCount.setText("Words: " + wordCount);
        lblLongestWord.setText("Longest word: " + (longestWord.isEmpty() ? "-" : longestWord));
        lblAvgWordLen.setText(String.format("Average word length: %.2f", avgWordLen));
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.2f KB", kb);
        double mb = kb / 1024.0;
        return String.format("%.2f MB", mb);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileSizeCalculator::new);
    }
}
