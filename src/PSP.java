import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class PSP {
	public static void main(String[] args) throws SocketException, IOException {
		// TODO Auto-generated method stub
		String server = "files.000webhost.com";
        int port = 21;
        String user = "tapping";
        String password = "Tapping@2023";

        // Archivo a subir
        File file = new File("C:/prova/prueba.xml");

        // Cliente FTP
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        ftpClient.login(user, password);

        // Configuración de la transferencia de archivos
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();

        // Subida del archivo
        String remoteFile = file.getName();
        FileInputStream inputStream = new FileInputStream(file);
        boolean success = ftpClient.storeFile(remoteFile, inputStream);
        inputStream.close();

        // Cierre de la conexión FTP
        ftpClient.logout();
        ftpClient.disconnect();

        // Mensaje de resultado
        if (success) {
            System.out.println("Archivo subido correctamente");
        } else {
            System.out.println("No se ha podido subir el archivo");
        }

	}
}
