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
        String password = "Tapping2023";

        // Archivo a subir
        File file = new File("C:/prova/Noticia2.xml");

        // Cliente FTP
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        ftpClient.login(user, password);

        // Configuración de la transferencia de archivos
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();

        String remoteFile = file.getName();
        String rutaLocal = "/public_html/Locals";
        File carpetaLocal = new File(rutaLocal);
        String rutaNoticia = "/public_html/Noticies";
        File carpetaNoticia = new File(rutaNoticia);
        
        // Subida del archivo
        if(remoteFile.contains("Local")) {
        	if(!carpetaLocal.exists()) {
        		ftpClient.makeDirectory(rutaLocal);
        	}
        	rutaLocal += "/";
        	remoteFile = rutaLocal+file.getName();
        }else if(remoteFile.contains("Noticia")){
        	if(!carpetaNoticia.exists()) {
        		ftpClient.makeDirectory(rutaNoticia);
        	}
        	rutaNoticia += "/";
        	remoteFile = rutaNoticia+file.getName();
        }
        
        FileInputStream inputStream = new FileInputStream(file);
        boolean success = ftpClient.storeFile(remoteFile, inputStream);
        inputStream.close();

        // Cierre de la conexión FTP
        ftpClient.logout();
        ftpClient.disconnect();

        // Mensaje de resultado
        if (success) {
            System.out.println("Arxiu pujat amb exit.");
        } else {
            System.out.println("No s'ha pogut pujar l'arxiu.");
        }
	}
}