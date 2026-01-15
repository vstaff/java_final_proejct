package com.mp3editor.ui;

import com.mp3editor.model.MP3Metadata;
import com.mp3editor.service.MP3TagService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MP3EditorPanel extends JPanel {
    private JButton chooseCoverButton;
    private JLabel coverLabel;
    private byte[] selectedCoverData;


    private final MP3TagService tagService = new MP3TagService();

    // левая часть - список файлов
    private JList<File> fileList;
    private FileListModel fileListModel;

    // правая часть - поля для тегов
    private JTextField titleField;
    private JTextField artistField;
    private JTextField albumField;
    private JTextField genreField;
    private JTextField yearField;
    private JTextField bpmField;

    private JButton saveButton;

    public MP3EditorPanel() {
        setLayout(new BorderLayout());

        // создаём левую панель со списком файлов
        JPanel leftPanel = createFileListPanel();

        // создаём правую панель с полями тегов
        JPanel rightPanel = createTagEditorPanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createFileListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("MP3 files"));

        fileListModel = new FileListModel();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // При выборе файла загружаем его теги в форму
        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                File selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    loadMetadataToForm(selectedFile);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(300, 0));

        JButton addFilesButton = new JButton("Add files");
        addFilesButton.addActionListener(e -> openFileChooserAndAddFiles());

        JButton removeFileButton = new JButton("Remove selected");
        removeFileButton.addActionListener(e -> removeSelectedFile());

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(addFilesButton);
        buttonsPanel.add(removeFileButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTagEditorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tags"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets.set(5, 5, 5, 5);

        titleField = new JTextField(25);
        artistField = new JTextField(25);
        albumField = new JTextField(25);
        genreField = new JTextField(25);
        yearField = new JTextField(25);
        bpmField = new JTextField(25);

        int row = 0;

        addLabelAndField(panel, gbc, row++, "Title:", titleField);
        addLabelAndField(panel, gbc, row++, "Artist:", artistField);
        addLabelAndField(panel, gbc, row++, "Album:", albumField);
        addLabelAndField(panel, gbc, row++, "Genre:", genreField);
        addLabelAndField(panel, gbc, row++, "Year:", yearField);
        addLabelAndField(panel, gbc, row++, "BPM:", bpmField);

        // Обложка
        coverLabel = new JLabel("No cover selected");
        chooseCoverButton = new JButton("Choose cover image");
        chooseCoverButton.addActionListener(e -> chooseCoverImage());

        // строка с надписью "Cover:" и кнопкой
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Cover:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(chooseCoverButton, gbc);

        // следующая строка — label с текстом статуса
        gbc.gridx = 0;
        gbc.gridy = ++row;
        gbc.gridwidth = 2;
        panel.add(coverLabel, gbc);

        // кнопка Save tags на СЛЕДУЮЩЕЙ строке
        saveButton = new JButton("Save tags");
        saveButton.addActionListener(e -> saveTagsForSelectedFile());

        gbc.gridx = 0;
        gbc.gridy = ++row;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(saveButton, gbc);

        return panel;
    }


    private void chooseCoverImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose cover image");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif", "bmp"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File imageFile = fileChooser.getSelectedFile();
            try {
                selectedCoverData = java.nio.file.Files.readAllBytes(imageFile.toPath());
                coverLabel.setText("Cover selected: " + imageFile.getName() +
                        " (" + selectedCoverData.length + " bytes)");
                coverLabel.setToolTipText("Click to preview"); // можно добавить превью позже
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error reading image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(field, gbc);
    }

    private void openFileChooserAndAddFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true); // можно выбрать несколько файлов [web:21]
        fileChooser.setFileFilter(new FileNameExtensionFilter("MP3 files", "mp3"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                fileListModel.addFile(file);
            }
        }
    }

    private void removeSelectedFile() {
        File selected = fileList.getSelectedValue();
        if (selected != null) {
            fileListModel.removeFile(selected);
            clearForm();
        }
    }

    private void loadMetadataToForm(File file) {
        try {
            MP3Metadata metadata = tagService.readMetadata(file);
            titleField.setText(metadata.getTitle());
            artistField.setText(metadata.getArtist());
            albumField.setText(metadata.getAlbum());
            genreField.setText(metadata.getGenre());
            yearField.setText(metadata.getYear());
            bpmField.setText(metadata.getBpm() != null ? metadata.getBpm().toString() : "");

            // Обложка
            byte[] cover = metadata.getCoverArt();
            if (cover != null && cover.length > 0) {
                selectedCoverData = cover;
                coverLabel.setText("Cover: " + cover.length + " bytes");
            } else {
                selectedCoverData = null;
                coverLabel.setText("No cover");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error reading tags: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTagsForSelectedFile() {
        File selectedFile = fileList.getSelectedValue();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file from the list.",
                    "No file selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Собираем данные из формы
        MP3Metadata metadata = new MP3Metadata();
        metadata.setTitle(titleField.getText());
        metadata.setArtist(artistField.getText());
        metadata.setAlbum(albumField.getText());
        metadata.setGenre(genreField.getText());
        metadata.setYear(yearField.getText());

        String yearText = yearField.getText();
        if (yearText != null && !yearText.isBlank()) {
            String trimmedYear = yearText.trim();

            // Регулярка: ровно 4 цифры
            if (!trimmedYear.matches("\\d{4}")) {
                JOptionPane.showMessageDialog(this,
                        "Year must be a 4-digit number (e.g. 2024).",
                        "Invalid year",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int yearValue = Integer.parseInt(trimmedYear);

            // Дополнительная логическая проверка диапазона
            if (yearValue < 1400 || yearValue > 2099) {
                JOptionPane.showMessageDialog(this,
                        "Year must be between 1400 and 2099.",
                        "Invalid year",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // если всё ок — записываем строковое значение в метаданные
            metadata.setYear(trimmedYear);
        } else {
            metadata.setYear(null);
        }


        metadata.setCoverArt(selectedCoverData);

        String bpmText = bpmField.getText();
        if (bpmText != null && !bpmText.isBlank()) {
            String trimmed = bpmText.trim();

            // Разрешаем только одну или больше цифр
            if (!trimmed.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                        "BPM must contain digits only (0-9).",
                        "Invalid BPM",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Здесь уже безопасно парсим, т.к. строка гарантированно только из цифр
            metadata.setBpm(Integer.parseInt(trimmed));
        }

        // Просим пользователя выбрать, куда сохранить изменённый файл
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save edited MP3");
        fileChooser.setSelectedFile(new File(selectedFile.getParentFile(),
                addSuffixToFileName(selectedFile.getName(), "_edited")));

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File outputFile = fileChooser.getSelectedFile();

        try {
            tagService.writeMetadata(selectedFile, outputFile, metadata);
            JOptionPane.showMessageDialog(this,
                    "Tags saved successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving tags: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String addSuffixToFileName(String fileName, String suffix) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName + suffix;
        }
        String name = fileName.substring(0, dotIndex);
        String ext = fileName.substring(dotIndex);
        return name + suffix + ext;
    }

    private void clearForm() {
        titleField.setText("");
        artistField.setText("");
        albumField.setText("");
        genreField.setText("");
        yearField.setText("");
        bpmField.setText("");
        selectedCoverData = null;
        coverLabel.setText("No cover selected");
    }

    /**
     * Модель списка файлов для JList.
     */
    private static class FileListModel extends javax.swing.AbstractListModel<File> {

        private final List<File> files = new ArrayList<>();

        @Override
        public int getSize() {
            return files.size();
        }

        @Override
        public File getElementAt(int index) {
            return files.get(index);
        }

        public void addFile(File file) {
            if (!files.contains(file)) {
                files.add(file);
                int index = files.size() - 1;
                fireIntervalAdded(this, index, index);
            }
        }

        public void removeFile(File file) {
            int index = files.indexOf(file);
            if (index >= 0) {
                files.remove(index);
                fireIntervalRemoved(this, index, index);
            }
        }
    }
}
