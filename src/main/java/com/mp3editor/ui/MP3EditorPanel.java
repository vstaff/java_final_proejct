package com.mp3editor.ui;

import com.mp3editor.model.MP3Metadata;
import com.mp3editor.service.MP3TagService;

// все компоненты swing для создания интерфейса
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

// компоненты awt для компоновки интерфейса
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

// стандартные классы java
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// классы для изменения внешнего вида диалогов выбора файлов
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class MP3EditorPanel extends JPanel {

    // компоненты для работы с обложкой
    private JButton chooseCoverButton;
    private JLabel coverLabel;
    private byte[] selectedCoverData;

    // сервис для работы с mp3 тегами
    private final MP3TagService tagService = new MP3TagService();

    // компоненты левой панели - список выбранных mp3 файлов
    private JList<File> fileList;
    private FileListModel fileListModel;

    // текстовые поля для редактирования тегов
    private JTextField titleField;
    private JTextField artistField;
    private JTextField albumField;
    private JTextField genreField;
    private JTextField yearField;
    private JTextField bpmField;

    // кнопка сохранения изменений
    private JButton saveButton;

    public MP3EditorPanel() {
        // задаем основной layout - левая и правая панели
        setLayout(new BorderLayout());

        // создаем левую панель со списком файлов
        JPanel leftPanel = createFileListPanel();

        // создаем правую панель с формой редактирования тегов
        JPanel rightPanel = createTagEditorPanel();

        // размещаем панели: слева список файлов, справа форма редактирования
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    // создает левую панель со списком mp3 файлов и кнопками управления
    private JPanel createFileListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // рамка с заголовком "mp3 files"
        panel.setBorder(BorderFactory.createTitledBorder("MP3 files"));

        // создаем модель данных для списка файлов
        fileListModel = new FileListModel();
        // создаем сам список файлов
        fileList = new JList<>(fileListModel);
        // разрешаем выбор только одного файла за раз
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // обработчик выбора файла в списке
        fileList.addListSelectionListener(e -> {
            // игнорируем промежуточные события при удержании мыши
            if (!e.getValueIsAdjusting()) {
                File selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    // загружаем теги выбранного файла в форму редактирования
                    loadMetadataToForm(selectedFile);
                }
            }
        });

        // обертка со скролбаром для списка файлов
        JScrollPane scrollPane = new JScrollPane(fileList);
        // фиксированная ширина панели со списком
        scrollPane.setPreferredSize(new Dimension(300, 0));

        // кнопка добавления файлов
        JButton addFilesButton = new JButton("Add files");
        addFilesButton.addActionListener(e -> openFileChooserAndAddFiles());

        // кнопка удаления выбранного файла из списка
        JButton removeFileButton = new JButton("Remove selected");
        removeFileButton.addActionListener(e -> removeSelectedFile());

        // панель для размещения кнопок
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(addFilesButton);
        buttonsPanel.add(removeFileButton);

        // размещаем компоненты: список по центру, кнопки снизу
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    // создает правую панель с формой редактирования тегов
    private JPanel createTagEditorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tags"));

        // настройки компоновки для сетки label + поле ввода
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets.set(5, 5, 5, 5);

        // создаем все текстовые поля для редактирования тегов
        titleField = new JTextField(25);
        artistField = new JTextField(25);
        albumField = new JTextField(25);
        genreField = new JTextField(25);
        yearField = new JTextField(25);
        bpmField = new JTextField(25);

        int row = 0;

        // размещаем пары label + поле ввода для каждого тега
        addLabelAndField(panel, gbc, row++, "Title:", titleField);
        addLabelAndField(panel, gbc, row++, "Artist:", artistField);
        addLabelAndField(panel, gbc, row++, "Album:", albumField);
        addLabelAndField(panel, gbc, row++, "Genre:", genreField);
        addLabelAndField(panel, gbc, row++, "Year:", yearField);
        addLabelAndField(panel, gbc, row++, "BPM:", bpmField);

        // статусная надпись для обложки
        coverLabel = new JLabel("No cover selected");
        // кнопка выбора изображения обложки
        chooseCoverButton = new JButton("Choose cover image");
        chooseCoverButton.addActionListener(e -> chooseCoverImage());

        // строка с надписью "Cover:" и кнопкой выбора
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Cover:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(chooseCoverButton, gbc);

        // следующая строка - статус выбранной обложки (растягивается на 2 колонки)
        gbc.gridx = 0;
        gbc.gridy = ++row;
        gbc.gridwidth = 2;
        panel.add(coverLabel, gbc);

        // кнопка сохранения всех изменений
        saveButton = new JButton("Save tags");
        saveButton.addActionListener(e -> saveTagsForSelectedFile());

        // размещаем кнопку сохранения ниже (растягивается на 2 колонки)
        gbc.gridx = 0;
        gbc.gridy = ++row;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(saveButton, gbc);

        return panel;
    }

    // создает диалог выбора файлов
    private JFileChooser createNativeWindowsFileChooser(int dialogType) {
        JFileChooser chooser = new JFileChooser();
        // устанавливаем тип диалога (открытие или сохранение)
        chooser.setDialogType(dialogType);

        // сохраняем текущий стиль интерфейса приложения
        LookAndFeel previousLookAndFeel = UIManager.getLookAndFeel();

        try {
            // временно переключаемся на системный стиль windows
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // обновляем стиль только диалога выбора файлов
            SwingUtilities.updateComponentTreeUI(chooser);
        } catch (ClassNotFoundException |
                 InstantiationException |
                 IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
//            // логируем ошибку смены стиля, но не прерываем работу
//            Logger.getLogger(MP3EditorPanel.class.getName())
//                    .log(Level.WARNING, "Cannot set native look and feel", e);
        }

        try {
            // возвращаем исходный стиль для остального приложения
            UIManager.setLookAndFeel(previousLookAndFeel);
        } catch (UnsupportedLookAndFeelException e) {
            // игнорируем ошибку восстановления стиля
        }

        return chooser;
    }

    // открывает диалог выбора изображения для обложки
    private void chooseCoverImage() {
        JFileChooser fileChooser = createNativeWindowsFileChooser(JFileChooser.OPEN_DIALOG);
        fileChooser.setDialogTitle("Choose cover image");
        // фильтр показывает только изображения
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif", "bmp"));

        int result = fileChooser.showOpenDialog(this);

        // если пользователь выбрал файл
        if (result == JFileChooser.APPROVE_OPTION) {
            File imageFile = fileChooser.getSelectedFile();
            try {
                // читаем весь файл изображения в массив байтов
                selectedCoverData = java.nio.file.Files.readAllBytes(imageFile.toPath());
                // обновляем статус с именем файла и размером
                coverLabel.setText("Cover selected: " + imageFile.getName() +
                        " (" + selectedCoverData.length + " bytes)");
                // подсказка при наведении мыши
                coverLabel.setToolTipText("Click to preview");
            } catch (Exception ex) {
                // показываем ошибку чтения файла
                JOptionPane.showMessageDialog(this,
                        "Error reading image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // размещает label и текстовое поле в одной строке сетки
    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        // первый столбец - подпись
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);

        // второй столбец - поле ввода
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(field, gbc);
    }

    // открывает диалог выбора mp3 файлов для добавления в список
    private void openFileChooserAndAddFiles() {
        JFileChooser fileChooser = createNativeWindowsFileChooser(JFileChooser.OPEN_DIALOG);
        // разрешаем множественный выбор файлов
        fileChooser.setMultiSelectionEnabled(true);
        // показываем только mp3 файлы
        fileChooser.setFileFilter(new FileNameExtensionFilter("MP3 files", "mp3"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // добавляем все выбранные файлы в список
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                fileListModel.addFile(file);
            }
        }
    }

    // удаляет выбранный файл из списка
    private void removeSelectedFile() {
        File selected = fileList.getSelectedValue();
        if (selected != null) {
            fileListModel.removeFile(selected);
            // очищаем форму редактирования
            clearForm();
        }
    }

    // загружает текущие теги выбранного файла в форму редактирования
    private void loadMetadataToForm(File file) {
        try {
            // читаем метаданные файла через сервис
            MP3Metadata metadata = tagService.readMetadata(file);
            // заполняем текстовые поля
            titleField.setText(metadata.getTitle());
            artistField.setText(metadata.getArtist());
            albumField.setText(metadata.getAlbum());
            genreField.setText(metadata.getGenre());
            yearField.setText(metadata.getYear());
            bpmField.setText(metadata.getBpm() != null ? metadata.getBpm().toString() : "");

            // обрабатываем обложку
            byte[] cover = metadata.getCoverArt();
            if (cover != null && cover.length > 0) {
                selectedCoverData = cover;
                coverLabel.setText("Cover: " + cover.length + " bytes");
            } else {
                selectedCoverData = null;
                coverLabel.setText("No cover");
            }
        } catch (Exception ex) {
            // показываем ошибку чтения тегов
            JOptionPane.showMessageDialog(this,
                    "Error reading tags: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // сохраняет измененные теги в новый mp3 файл
    private void saveTagsForSelectedFile() {
        // проверяем что файл выбран
        File selectedFile = fileList.getSelectedValue();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file from the list.",
                    "No file selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // собираем новые значения тегов из формы
        MP3Metadata metadata = new MP3Metadata();
        metadata.setTitle(titleField.getText());
        metadata.setArtist(artistField.getText());
        metadata.setAlbum(albumField.getText());
        metadata.setGenre(genreField.getText());
        metadata.setYear(yearField.getText());

        // валидация года
        String yearText = yearField.getText();
        if (yearText != null && !yearText.isBlank()) {
            String trimmedYear = yearText.trim();

            // проверяем что введено ровно 4 цифры
            if (!trimmedYear.matches("\\d{4}")) {
                JOptionPane.showMessageDialog(this,
                        "Year must be a 4-digit number (e.g. 2024).",
                        "Invalid year",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int yearValue = Integer.parseInt(trimmedYear);

            // проверяем разумный диапазон дат
            if (yearValue < 1400 || yearValue > 2099) {
                JOptionPane.showMessageDialog(this,
                        "Year must be between 1400 and 2099.",
                        "Invalid year",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // сохраняем валидный год
            metadata.setYear(trimmedYear);
        } else {
            metadata.setYear(null);
        }

        // сохраняем выбранную обложку
        metadata.setCoverArt(selectedCoverData);

        // валидация bpm
        String bpmText = bpmField.getText();
        if (bpmText != null && !bpmText.isBlank()) {
            String trimmed = bpmText.trim();

            // проверяем что введены только цифры
            if (!trimmed.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                        "BPM must contain digits only (0-9).",
                        "Invalid BPM",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // преобразуем в число
            metadata.setBpm(Integer.parseInt(trimmed));
        }

        // показываем диалог сохранения нового файла
        JFileChooser fileChooser = createNativeWindowsFileChooser(JFileChooser.SAVE_DIALOG);
        fileChooser.setDialogTitle("Save edited MP3");
        fileChooser.setFileFilter(new FileNameExtensionFilter("MP3 files", "mp3"));
        // предлагаем имя файла с суффиксом _edited
        fileChooser.setSelectedFile(new File(selectedFile.getParentFile(),
                addSuffixToFileName(selectedFile.getName(), "_edited")));

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File outputFile = fileChooser.getSelectedFile();

        try {
            // сохраняем изменения через сервис
            tagService.writeMetadata(selectedFile, outputFile, metadata);
            // сообщаем об успешном сохранении с полным путем
            JOptionPane.showMessageDialog(this,
                    "Tags saved successfully to:\n" + outputFile.getAbsolutePath(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving tags: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // добавляет суффикс к имени файла перед расширением (song.mp3 -> song_edited.mp3)
    private String addSuffixToFileName(String fileName, String suffix) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName + suffix;
        }
        String name = fileName.substring(0, dotIndex);
        String ext = fileName.substring(dotIndex);
        return name + suffix + ext;
    }

    // очищает все поля формы редактирования
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

    // внутренняя модель данных для списка файлов JList
    private static class FileListModel extends javax.swing.AbstractListModel<File> {

        private final List<File> files = new ArrayList<>();

        // возвращает количество файлов в списке
        @Override
        public int getSize() {
            return files.size();
        }

        // возвращает файл по индексу
        @Override
        public File getElementAt(int index) {
            return files.get(index);
        }

        // добавляет файл в конец списка
        public void addFile(File file) {
            if (!files.contains(file)) {
                files.add(file);
                int index = files.size() - 1;
                // уведомляем JList об добавлении элемента
                fireIntervalAdded(this, index, index);
            }
        }

        // удаляет файл из списка
        public void removeFile(File file) {
            int index = files.indexOf(file);
            if (index >= 0) {
                files.remove(index);
                // уведомляем JList об удалении элемента
                fireIntervalRemoved(this, index, index);
            }
        }
    }
}
