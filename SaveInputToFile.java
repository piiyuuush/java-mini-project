import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;

public class SaveInputToFile extends JFrame {
    private JTextField textField;
    private JTextArea textArea;
    private JButton saveButton, newProjectButton, loadProjectButton;
    private File currentProjectFile;

    public SaveInputToFile() {
        // Frame setup
        setTitle("ER Diagram Generator");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        // Input panel (top)
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        JLabel label = new JLabel("Enter diagram details: ");
        textField = new JTextField(25);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(label, BorderLayout.WEST);
        inputPanel.add(textField, BorderLayout.CENTER);

        // Text area (center)
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Button panel (bottom)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        saveButton = new JButton("Save");
        newProjectButton = new JButton("New Project");
        loadProjectButton = new JButton("Load Project");

        buttonPanel.add(newProjectButton);
        buttonPanel.add(loadProjectButton);
        buttonPanel.add(saveButton);

        // Ensure projects folder exists
        File projectsDir = new File("projects");
        if (!projectsDir.exists()) {
            projectsDir.mkdir();
        }

        // Action: New Project
        newProjectButton.addActionListener(e -> createNewProject());

        // Action: Load Project
        loadProjectButton.addActionListener(e -> loadExistingProject());

        // Action: Save
        saveButton.addActionListener(e -> {
            if (currentProjectFile != null) {
                saveToFile(textField.getText());
            } else {
                JOptionPane.showMessageDialog(this, "No project selected. Please create or load a project first.");
            }
        });

        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void createNewProject() {
        String projectName = JOptionPane.showInputDialog(this, "Enter project name:");
        if (projectName != null && !projectName.trim().isEmpty()) {
            currentProjectFile = new File("projects/" + projectName + ".txt");
            try {
                if (currentProjectFile.createNewFile()) {
                    JOptionPane.showMessageDialog(this, "New project created: " + currentProjectFile.getName());
                } else {
                    JOptionPane.showMessageDialog(this, "Project already exists. It will be used.");
                }
                loadFileIntoTextArea();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error creating project: " + ex.getMessage());
            }
        }
    }

    private void loadExistingProject() {
        File projectsDir = new File("projects");
        String[] files = projectsDir.list((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "No projects available.");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select a project:",
                "Load Project",
                JOptionPane.PLAIN_MESSAGE,
                null,
                files,
                files[0]
        );

        if (selected != null) {
            currentProjectFile = new File("projects/" + selected);
            loadFileIntoTextArea();
            JOptionPane.showMessageDialog(this, "Loaded project: " + currentProjectFile.getName());
        }
    }

    private void loadFileIntoTextArea() {
        try {
            String content = new String(Files.readAllBytes(currentProjectFile.toPath()));
            textArea.setText(content);
        } catch (IOException ex) {
            textArea.setText("Error reading file: " + ex.getMessage());
        }
    }

    private void saveToFile(String text) {
        try (FileWriter writer = new FileWriter(currentProjectFile, true)) {
            writer.write(text + System.lineSeparator());
            loadFileIntoTextArea(); // refresh text area after saving
            textField.setText(""); // clear input after saving
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SaveInputToFile::new);
    }
}