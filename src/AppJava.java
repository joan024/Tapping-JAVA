import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AppJava {

	static String server = "files.000webhost.com";
	static int port = 21;
	static String user = "tapping";
	static String password = "Tapping2023";
	static String rutaRemota1 = "/public_html/fotos/Locals/";
	static String rutaLocales = "C:/TappingFotos/local/";
	static String rutaFotos = "C:/TappingFotos/";
	static Scanner sc = new Scanner(System.in);
	
	public static void main(String[] args) throws SocketException, IOException, TransformerException {

		String ruta = JOptionPane.showInputDialog("Indica la ruta del fitxer");
		
		comprobarXml(ruta);
		
		
	}

	
	public static void comprobarXml(String ruta) throws TransformerException {
		
		 FTPClient ftpClient = new FTPClient();
		    try {
		        ftpClient.connect(server, port);
		        ftpClient.login(user, password);
		        ftpClient.enterLocalPassiveMode();
		        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		        String id = idTxt(ruta);
		        String[] files = ftpClient.listNames(rutaRemota1 + id + ".xml");
		        if (files != null && files.length > 0) {
		            // El archivo existe, descargarlo
		        	String remoteFile = rutaRemota1 + id + ".xml";
		        	File localFile = new File(rutaLocales + id + ".xml");
		        	OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
		        	InputStream inputStream = ftpClient.retrieveFileStream(remoteFile);
		        	byte[] bytesArray = new byte[4096];
		        	int bytesRead = -1;
		        	while ((bytesRead = inputStream.read(bytesArray)) != -1) {
		        	    outputStream.write(bytesArray, 0, bytesRead);
		        	}
		        	boolean success = ftpClient.completePendingCommand();
		        	if (success) {
		        	    System.out.println("Se ha baixat Correctament");
		        	} else {
		        	    System.out.println("No se ha pogut baixa.");
		        	}
		        	outputStream.close();
		        	inputStream.close();

		        	String dataTxt = fechaTxt(ruta);
		        	String dataXml = fechaXml(ruta);
		        	comprobarData(dataTxt,dataXml,ruta);
		 
		        }else {
		        	String archivo =TxtAXml(ruta);
		        	pujarArxiu(archivo);
		            System.out.println("Se ha subido Correctamente");
		        }
	            ftpClient.logout();
	            ftpClient.disconnect();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	
	}
	
	public static void comprobarData(String dataTxt, String dataXml, String ruta) throws SocketException, IOException {
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        try {
            Date date1 = dateFormat.parse(dataTxt);
            Date date2 = dateFormat.parse(dataXml);

            if (!date1.equals(date2)) {
            	String fotos [] = fotos(ruta);
            	borrarFotos(fotos);
            	String nom =TxtAXml(ruta);
    			pujarArxiu(nom);
    			SubirFotos(fotos);
    			
            }else {
            	System.out.println("Les Dates son les Mateixes");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
		
	}

	public static String fechaXml(String ruta) {
		
		 String id = idTxt(ruta);
		 String archivo = rutaLocales+id+".xml"; 
		 String fecha = "";
	        try {
	            // Crear el objeto DocumentBuilderFactory
	            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder builder = factory.newDocumentBuilder();
	            // Parsear el archivo XML
	            Document document = builder.parse(new File(archivo));
	            // Obtener el elemento raíz
	            Element rootElement = document.getDocumentElement();
	            // Obtener el nodo de fecha
	            NodeList fechaNodes = rootElement.getElementsByTagName("data");
	            if (fechaNodes.getLength() > 0) {
	                // Obtener el primer nodo de fecha
	                Node fechaElement =  fechaNodes.item(0);
	                // Obtener el texto del nodo de fecha
	                	fecha = fechaElement.getTextContent();
	                // Aquí tienes la fecha obtenida del archivo XML
	                System.out.println(fecha);
	            } else {
	                System.out.println("No se encontró ninguna etiqueta de fecha en el archivo XML.");
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return fecha;
	}
	
	public static String TxtAXml (String ruta) {

		String contenido,nom="";
        int cont = 0;

        try {
		BufferedReader br = new BufferedReader(new FileReader(rutaLocales+ruta+""));
        ArrayList<String> lineas = new ArrayList<String>();

        while ((contenido = br.readLine()) != null) {
            String[] contenidoSplit = contenido.split("#");
            for (String s : contenidoSplit) {
                lineas.add(s);
            }
        }
        String fecha = lineas.get(0);
        nom = lineas.get(1);
        br.close();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("fotos");
        doc.appendChild(rootElement);

        Element data = doc.createElement("data");
        data.appendChild(doc.createTextNode(fecha));
        rootElement.appendChild(data);

        for (int i = 2; i < lineas.size(); i++) {
        	Element foto = doc.createElement("foto");
            rootElement.appendChild(foto);
            
            Element lineElement = doc.createElement("nom");
            lineElement.appendChild(doc.createTextNode(lineas.get(i)));
            foto.appendChild(lineElement);
            cont++;
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        String xmlPath = rutaLocales+nom+".xml"; // Ruta donde se guardará el archivo XML
        StreamResult result = new StreamResult(new File(xmlPath));
        transformer.transform(source, result);

        System.out.println("Archivo XML creado con éxito en la ruta: " + xmlPath);  

        	} catch (IOException | ParserConfigurationException | TransformerException e) {
        		e.printStackTrace();
        	}
        return nom;
	}
	
	public static void pujarArxiu(String ruta) throws SocketException, IOException {
		
		String archivo = TxtAXml(ruta);
		String id = idTxt(archivo);
		File file = new File(rutaLocales+id+".xml");

        // Cliente FTP
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        ftpClient.login(user, password);

        // Configuración de la transferencia de archivos
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();

        String remoteFile = file.getName();
        File carpetaLocal = new File(rutaRemota1+id+".xml");
        if(!carpetaLocal.exists()) {
        	ftpClient.makeDirectory(rutaRemota1+id+".xml");
        }
        remoteFile = rutaRemota1+file.getName();
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

	public static String fechaTxt(String ruta) {
		String arxiu = rutaLocales + ruta; 
	    String fecha = "";
	    try (BufferedReader br = new BufferedReader(new FileReader(arxiu))) {
	        String fechaHoraStr = br.readLine();
	        String fechaStr = fechaHoraStr.split("#")[0];
	        LocalDateTime fechaParsed = parsearFechaHora(fechaStr, "dd/MM/yyyy HH:mm:ss");
	        DateTimeFormatter formatear = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	        fecha = fechaParsed.format(formatear);
	        System.out.println(fecha);
	        String nombreArchivo;
	        while ((nombreArchivo = br.readLine()) != null) {
	            System.out.println("Nombre de Archivo: " + nombreArchivo);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return fecha;
	}
	
	private static LocalDateTime parsearFechaHora(String fechaHoraStr, String formato) {
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato);
	    return LocalDateTime.parse(fechaHoraStr, formatter);
	}

	public static String idTxt(String ruta) {
		String id ="";
		String filePath = rutaLocales + ruta; 
	    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	        String idtxt = br.readLine();
	        id = idtxt.split("#")[1];
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		return id;
	}
	
	public static void borrarFotos(String[] nombresArchivos) {
		
		 FTPClient ftpClient = new FTPClient();
		    try {
		        ftpClient.connect(server, port);
		        ftpClient.login(user, password);

		        for (String nombreArchivo : nombresArchivos) {
		            String rutaArchivo = rutaRemota1 + "/" + nombreArchivo;
		            boolean resultado = ftpClient.deleteFile(rutaArchivo);
		            if (resultado) {
		                System.out.println("El archivo " + rutaArchivo + " ha sido borrado exitosamente.");
		            } else {
		                System.out.println("No se pudo borrar el archivo " + rutaArchivo + ".");
		            }
		        }
		    } catch (IOException e) {
		        System.out.println("Ocurrió un error al conectarse al servidor FTP: " + e.getMessage());
		    } finally {
		        try {
		            ftpClient.disconnect();
		        } catch (IOException e) {
		            System.out.println("Ocurrió un error al desconectarse del servidor FTP: " + e.getMessage());
		        }
		    }
	}

	public static void SubirFotos(String[] fotos) {
		FTPClient ftpClient = new FTPClient();
	    try {
	        ftpClient.connect(server, port);
	        ftpClient.login(user, password);
	        ftpClient.enterLocalPassiveMode();
	        ftpClient.changeWorkingDirectory(rutaRemota1);
	        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

	        for (String rutaArchivo : fotos) {
	            File archivo = new File(rutaFotos + rutaArchivo);
	            String nombreArchivo = archivo.getName();
	            String rutaArchivoRemota = rutaRemota1 + "/" + nombreArchivo;
	            FileInputStream inputStream = new FileInputStream(archivo);
	            boolean resultado = ftpClient.storeFile(rutaArchivoRemota, inputStream);
	            inputStream.close();
	            if (resultado) {
	                System.out.println("El archivo " + nombreArchivo + " ha sido subido exitosamente.");
	            } else {
	                System.out.println("No se pudo subir el archivo " + nombreArchivo + ".");
	            }
	        }
	        ftpClient.logout();
	    } catch (IOException e) {
	        System.out.println("Ocurrió un error al conectarse al servidor FTP: " + e.getMessage());
	    } finally {
	        try {
	            ftpClient.disconnect();
	        } catch (IOException e) {
	            System.out.println("Ocurrió un error al desconectarse del servidor FTP: " + e.getMessage());
	        }
	    }
	}

	public static String [] fotos(String ruta) {
		
		 String[] fotos = null;
		    ArrayList<String> listaFotos = new ArrayList<String>();
		    File archivo = new File(rutaLocales + ruta);
		    
		    try {
		        FileReader fr = new FileReader(archivo);
		        BufferedReader br = new BufferedReader(fr);
		        String linea;
		        while ((linea = br.readLine()) != null) {
		            if (linea.endsWith(".jpg") || linea.endsWith(".jpeg") || linea.endsWith(".png")) {
		                listaFotos.add(linea);
		            }
		        }
		        fr.close();
		        fotos = listaFotos.toArray(new String[0]);
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		    return fotos;
	}
}
