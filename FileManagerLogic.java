
package memoria.prueba;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileManagerLogic {
    private File selectedFile;

    public FileManagerLogic() {
        
    }

    public void setSelectedFile(File file) {
        this.selectedFile = file;
    }

    public void registrarDatosEnArchivo(String nuevoContenido) {
        if (selectedFile != null && selectedFile.isFile()) {
            try (FileWriter writer = new FileWriter(selectedFile, true)) {
                writer.write(nuevoContenido + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

