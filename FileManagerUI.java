package memoria.prueba;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class FileManagerUI extends JFrame {

    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private JTextField fileNameField;
    private JTextArea fileContentArea;
    private File selectedFile;
    private File copiedFile;

    public FileManagerUI() {
        setTitle("File Manager");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create the file tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileNode(new File("C:/"))); // Change the root path according to your operating system
        treeModel = new DefaultTreeModel(root);
        fileTree = new JTree(treeModel);
        fileTree.setRootVisible(false);

        // Add a selection listener to the file tree
        fileTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof FileNode) {
                selectedFile = ((FileNode) node.getUserObject()).getFile();
                showFileDetails();
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        mainPanel.add(treeScrollPane, BorderLayout.WEST);

        // Panel for file details
        JPanel detailsPanel = new JPanel(new GridLayout(3, 1));

        // Panel for changing file name
        JPanel renamePanel = new JPanel();
        JLabel nameLabel = new JLabel("Name:");
        fileNameField = new JTextField(20);
        JButton renameButton = new JButton("Rename");
        renameButton.addActionListener(e -> renameFile());
        renamePanel.add(nameLabel);
        renamePanel.add(fileNameField);
        renamePanel.add(renameButton);
        detailsPanel.add(renamePanel);

        // Panel for editing file content
        JPanel editPanel = new JPanel(new BorderLayout());
        JLabel contentLabel = new JLabel("Content:");
        fileContentArea = new JTextArea(10, 30);
        JScrollPane contentScrollPane = new JScrollPane(fileContentArea);
        JButton saveButton = new JButton("Save changes");
        saveButton.addActionListener(e -> saveFileContent());
        editPanel.add(contentLabel, BorderLayout.NORTH);
        editPanel.add(contentScrollPane, BorderLayout.CENTER);
        editPanel.add(saveButton, BorderLayout.SOUTH);
        detailsPanel.add(editPanel);

        // Panel for additional options
        JPanel optionsPanel = new JPanel(new GridLayout(1, 4));

        JButton copyPasteButton = new JButton("Copiar y Pegar");
        copyPasteButton.addActionListener(e -> copyAndPasteFile());
        optionsPanel.add(copyPasteButton);

        detailsPanel.add(optionsPanel);

        mainPanel.add(detailsPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setVisible(true);

        // Reload the tree in a separate thread
        new Thread(this::reloadTree).start();

        JButton crearArchivoButton = new JButton("Crear Archivo Comercial");
        crearArchivoButton.addActionListener(e -> crearArchivoComercial());
        optionsPanel.add(crearArchivoButton);

        JButton crearCarpetaButton = new JButton("Crear Carpeta");
        crearCarpetaButton.addActionListener(e -> crearCarpeta());
        optionsPanel.add(crearCarpetaButton);

        JButton registrarDatosButton = new JButton("Registrar Datos en Archivo");
        registrarDatosButton.addActionListener(e -> registrarDatosEnArchivo());
        optionsPanel.add(registrarDatosButton);

        JComboBox<String> sortingOptions = new JComboBox<>();
        sortingOptions.addItem("Nombre");
        sortingOptions.addItem("Fecha");
        sortingOptions.addItem("Tipo");
        sortingOptions.addItem("Tamaño");

        // Agregar un listener para el menú desplegable
        sortingOptions.addActionListener(e -> {
            String selectedOption = (String) sortingOptions.getSelectedItem();
            switch (selectedOption) {
                case "Nombre":
                    ordenarPorNombre();
                    break;
                case "Fecha":
                    ordenarPorFecha();
                    break;
                case "Tipo":
                    ordenarPorTipo();
                    break;
                case "Tamaño":
                    ordenarPorTamaño();
                    break;
            }
        });

        optionsPanel.add(new JLabel("Ordenar por:"));
        optionsPanel.add(sortingOptions);

        getContentPane().add(optionsPanel, BorderLayout.NORTH);

    }

    private void copyAndPasteFile() {
        if (selectedFile != null && selectedFile.exists()) {
            // Primero solicitamos al usuario el archivo que desea copiar
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccione el archivo a copiar");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File fileToCopy = fileChooser.getSelectedFile();
                if (fileToCopy.exists()) {
                    // Luego solicitamos al usuario la ubicación donde desea pegar el archivo
                    fileChooser.setDialogTitle("Seleccione la ubicación para pegar el archivo");
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    result = fileChooser.showSaveDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File destination = fileChooser.getSelectedFile();
                        // Copiamos el archivo al destino seleccionado
                        if (fileToCopy.isDirectory()) {
                            copyFolder(fileToCopy, destination);
                        } else {
                            copyFile(fileToCopy, new File(destination, fileToCopy.getName()));
                        }
                        reloadTree();
                        JOptionPane.showMessageDialog(this, "Archivo copiado y pegado exitosamente.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "El archivo seleccionado para copiar no existe.");
                }
            }
        }
    }

    private void showFileDetails() {
        if (selectedFile != null) {
            fileNameField.setText(selectedFile.getName());
            if (selectedFile.isFile()) {
                // Read file content in a separate thread
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        SwingUtilities.invokeLater(() -> {
                            fileContentArea.setText(content.toString());
                            fileContentArea.setEditable(true);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                fileContentArea.setText("");
                fileContentArea.setEditable(false);
            }
        }
    }

    private void renameFile() {
        if (selectedFile != null && selectedFile.exists()) {
            String newFileName = fileNameField.getText();
            File newFile = new File(selectedFile.getParent(), newFileName);
            if (selectedFile.renameTo(newFile)) {
                JOptionPane.showMessageDialog(this, "File name changed successfully.");
                reloadTree();
            } else {
                JOptionPane.showMessageDialog(this, "Error renaming file.");
            }
        }
    }

    private void saveFileContent() {
        if (selectedFile != null && selectedFile.isFile()) {
            // Write file content in a separate thread
            new Thread(() -> {
                try (FileWriter writer = new FileWriter(selectedFile)) {
                    writer.write(fileContentArea.getText());
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "File content saved successfully."));
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error saving file content."));
                }
            }).start();
        }
    }

    private void createNewFile() {
        String fileName = JOptionPane.showInputDialog(this, "Enter the name of the new file:");
        if (fileName != null && !fileName.trim().isEmpty()) {
            File newFile = new File(selectedFile, fileName);
            try {
                if (newFile.createNewFile()) {
                    JOptionPane.showMessageDialog(this, "File created successfully.");
                    reloadTree();
                } else {
                    JOptionPane.showMessageDialog(this, "Error creating file.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createNewFolder() {
        String folderName = JOptionPane.showInputDialog(this, "Enter the name of the new folder:");
        if (folderName != null && !folderName.trim().isEmpty()) {
            File newFolder = new File(selectedFile, folderName);
            if (newFolder.mkdir()) {
                JOptionPane.showMessageDialog(this, "Folder created successfully.");
                reloadTree();
            } else {
                JOptionPane.showMessageDialog(this, "Error creating folder.");
            }
        }
    }

    private void copyFile() {
        if (selectedFile != null && selectedFile.exists()) {
            copiedFile = selectedFile;
            JOptionPane.showMessageDialog(this, "File copied successfully.");
        }
    }

    private void pasteFile() {
        if (copiedFile != null && copiedFile.exists()) {
            File destination = new File(selectedFile, copiedFile.getName());
            if (copiedFile.isDirectory()) {
                copyFolder(copiedFile, destination);
            } else {
                copyFile(copiedFile, destination);
            }
            reloadTree();
            JOptionPane.showMessageDialog(this, "File pasted successfully.");
        }
    }

    private void copyFile(File source, File destination) {
        try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reloadTree() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();
        File[] roots = File.listRoots();
        for (File file : roots) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FileNode(file));
            root.add(node);
            addChildren(node);
        }
        treeModel.reload();
    }

    private void copyFolder(File source, File destination) {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    File srcFile = new File(source, file.getName());
                    File destFile = new File(destination, file.getName());
                    if (file.isDirectory()) {
                        copyFolder(srcFile, destFile);
                    } else {
                        copyFile(srcFile, destFile);
                    }
                }
            }
        }
    }

    private void addChildren(DefaultMutableTreeNode node) {
        File file = ((FileNode) node.getUserObject()).getFile();
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                Arrays.sort(children, Comparator.comparing(File::getName));
                for (File child : children) {
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(child));
                    node.add(childNode);
                    addChildren(childNode);
                }
            }
        }
    }

    private void organizar() {
        if (selectedFile != null && selectedFile.isDirectory()) {
            File[] files = selectedFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        organizarArchivo(file);
                    }
                }
                JOptionPane.showMessageDialog(this, "Archivos organizados exitosamente.");
                reloadTree();
            } else {
                JOptionPane.showMessageDialog(this, "Error al obtener archivos de la carpeta.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una carpeta para organizar los archivos.");
        }
    }

    private void organizarArchivo(File file) {
        String extension = obtenerExtension(file.getName());
        if (extension != null && !extension.isEmpty()) {
            String tipoCarpeta = obtenerTipoCarpeta(extension);
            if (tipoCarpeta != null && !tipoCarpeta.isEmpty()) {
                File carpetaDestino = new File(selectedFile, tipoCarpeta);
                if (!carpetaDestino.exists()) {
                    carpetaDestino.mkdir();
                }
                try {
                    Path origenPath = Paths.get(file.getPath());
                    Path destinoPath = Paths.get(carpetaDestino.getPath(), file.getName());
                    Files.move(origenPath, destinoPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String obtenerExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0 && index < fileName.length() - 1) {
            return fileName.substring(index + 1).toLowerCase();
        }
        return "";
    }

    private String obtenerTipoCarpeta(String extension) {
        switch (extension) {
            case "txt":
                return "Documentos de texto";
            case "doc":
            case "docx":
                return "Documentos de Word";
            case "pdf":
                return "Documentos PDF";
            default:
                return "Otros documentos";
        }
    }

    private void ordenarPorNombre() {
        ordenarArchivos(new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });
    }

    private void ordenarPorFecha() {
        ordenarArchivos(new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return Long.compare(file1.lastModified(), file2.lastModified());
            }
        });
    }

    private void ordenarPorTipo() {
        ordenarArchivos(new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                String ext1 = obtenerExtension(file1.getName());
                String ext2 = obtenerExtension(file2.getName());
                return ext1.compareToIgnoreCase(ext2);
            }
        });
    }

    private void ordenarPorTamaño() {
        ordenarArchivos(new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return Long.compare(file1.length(), file2.length());
            }
        });
    }

    private void ordenarArchivos(Comparator<File> comparator) {
        if (selectedFile != null && selectedFile.isDirectory()) {
            File[] files = selectedFile.listFiles();
            if (files != null) {
                Arrays.sort(files, comparator);
                reloadTree();
            } else {
                JOptionPane.showMessageDialog(this, "Error al obtener archivos de la carpeta.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una carpeta para ordenar los archivos.");
        }
    }

    private void crearArchivoComercial() {
        String nombreArchivo = JOptionPane.showInputDialog(this, "Ingrese el nombre del nuevo archivo:");
        String extension = JOptionPane.showInputDialog(this, "Ingrese la extensión del nuevo archivo:");
        if (nombreArchivo != null && extension != null && !nombreArchivo.trim().isEmpty() && !extension.trim().isEmpty()) {
            String nombreCompletoArchivo = nombreArchivo + "." + extension;
            File nuevoArchivo = new File(selectedFile, nombreCompletoArchivo);
            try {
                if (nuevoArchivo.createNewFile()) {
                    JOptionPane.showMessageDialog(this, "Archivo creado exitosamente.");
                    reloadTree();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al crear el archivo.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void crearCarpeta() {
        String nombreCarpeta = JOptionPane.showInputDialog(this, "Ingrese el nombre de la nueva carpeta:");
        if (nombreCarpeta != null && !nombreCarpeta.trim().isEmpty()) {
            File nuevaCarpeta = new File(selectedFile, nombreCarpeta);
            if (nuevaCarpeta.mkdir()) {
                JOptionPane.showMessageDialog(this, "Carpeta creada exitosamente.");
                reloadTree();
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear la carpeta.");
            }
        }
    }

    private void registrarDatosEnArchivo() {
        if (selectedFile != null && selectedFile.isFile()) {
            String nuevoContenido = JOptionPane.showInputDialog(this, "Ingrese el nuevo contenido:");
            if (nuevoContenido != null) {
                try (FileWriter writer = new FileWriter(selectedFile, true)) {
                    writer.write(nuevoContenido + "\n");
                    JOptionPane.showMessageDialog(this, "Datos registrados exitosamente en el archivo.");
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al registrar datos en el archivo.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo para registrar datos.");
        }
    }

    private static class FileNode {

        private final File file;

        public FileNode(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return file.getName();
        }
    }
}
