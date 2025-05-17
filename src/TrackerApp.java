import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.prefs.Preferences; // For saving last input

public class TrackerApp extends JFrame {

    private JTextField lastDoseDateTimeField;
    private JLabel resultLabel;
    private JLabel halfLifeLabel;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // For saving/loading the last input
    private static final String PREF_LAST_DOSE_TIME = "lastDoseTime";
    private Preferences prefs;

    public TrackerApp() {
        setTitle("Vortioxetine Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 250);
        setLocationRelativeTo(null); // Center the window
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        prefs = Preferences.userNodeForPackage(TrackerApp.class);

        // --- UI Components ---

        // Half-life info
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        halfLifeLabel = new JLabel("Using Vortioxetine half-life: " + VortioxetineCalculator.getHalfLifeHours() + " hours.");
        add(halfLifeLabel, gbc);

        // Last Dose Label
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Last Dose (yyyy-MM-dd HH:mm):"), gbc);

        // Last Dose Input Field
        gbc.gridx = 1;
        lastDoseDateTimeField = new JTextField(20);
        // Load last saved value
        lastDoseDateTimeField.setText(prefs.get(PREF_LAST_DOSE_TIME, ""));
        add(lastDoseDateTimeField, gbc);

        // "Now" Button
        gbc.gridx = 0;
        gbc.gridy++;
        JButton nowButton = new JButton("Set to Now");
        nowButton.setToolTipText("Set the last dose time to the current time.");
        add(nowButton, gbc);

        // Calculate Button
        gbc.gridx = 1;
        JButton calculateButton = new JButton("Calculate % Remaining");
        add(calculateButton, gbc);

        // Result Label
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        resultLabel = new JLabel("Result: - %");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(resultLabel, gbc);

        // Disclaimer
        gbc.gridy++;
        JLabel disclaimerLabel = new JLabel("<html><center><b>Disclaimer:</b> This is a simplified estimate and NOT medical advice.<br>Consult your doctor for any medical concerns.</center></html>");
        disclaimerLabel.setForeground(Color.RED);
        add(disclaimerLabel, gbc);


        // --- Action Listeners ---
        nowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lastDoseDateTimeField.setText(LocalDateTime.now().format(dateTimeFormatter));
            }
        });

        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateAndDisplay();
            }
        });

        // Also calculate on Enter press in the text field
        lastDoseDateTimeField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateAndDisplay();
            }
        });
    }

    private void calculateAndDisplay() {
        String dateTimeString = lastDoseDateTimeField.getText();
        if (dateTimeString.trim().isEmpty()) {
            resultLabel.setText("Result: Please enter dose time.");
            resultLabel.setForeground(Color.ORANGE);
            return;
        }

        try {
            LocalDateTime lastDoseTime = LocalDateTime.parse(dateTimeString, dateTimeFormatter);
            LocalDateTime currentTime = LocalDateTime.now();

            // Save the input for next time
            prefs.put(PREF_LAST_DOSE_TIME, dateTimeString);

            double percentage = VortioxetineCalculator.calculatePercentageRemaining(lastDoseTime, currentTime);

            if (percentage < 0) {
                resultLabel.setText("Result: Dose time cannot be in the future.");
                resultLabel.setForeground(Color.RED);
            } else if (percentage < 0.01) { // For very small, almost zero values
                resultLabel.setText(String.format("Result: < 0.01 %% (Essentially negligible)"));
                resultLabel.setForeground(Color.BLUE);
            }
            else {
                resultLabel.setText(String.format("Result: %.2f %% remaining.", percentage));
                resultLabel.setForeground(Color.BLUE); // Or Color.BLACK
            }

        } catch (DateTimeParseException ex) {
            resultLabel.setText("Result: Invalid date/time format. Use yyyy-MM-dd HH:mm");
            resultLabel.setForeground(Color.RED);
        } catch (IllegalArgumentException ex) {
            resultLabel.setText("Result: Error - " + ex.getMessage());
            resultLabel.setForeground(Color.RED);
        }
    }

    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TrackerApp().setVisible(true);
            }
        });
    }
}