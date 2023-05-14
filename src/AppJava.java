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
	static String rutaRemotaLocals = "/public_html/fotos/Locals/";
	static String rutaLocalLocals = "C:/TappingFotos/local/";
	static String rutaLocalFotos = "C:/TappingFotos/";
	static String idLocal;
	static ArrayList<String> fotosABorrar = new ArrayList<String>();
	
	public static void main(String[] args) throws SocketException, IOException, TransformerException {

		// Sol·licita la ruta del fitxer amb una finestra emergent
		String ruta = JOptionPane.showInputDialog("Indica la ruta del fitxer");
		
		// Comprova si el fitxer XML existeix i realitza les accions pertinents
		comprovarSiXMLExisteix(ruta);
		
		// Mostra un missatge per indicar que el programa ha finalitzat
		JOptionPane.showMessageDialog(null, "El progrma ha finalitzat.");;
		
	}

	// Mètode que comprova si el fitxer XML existeix al servidor FTP i, en funció del resultat, realitza diferents accions
	public static void comprovarSiXMLExisteix(String ruta) throws TransformerException {
		
		// Es crea un nou client FTP i es realitza la connexió al servidor
		 FTPClient ftpClient = new FTPClient();
		    try {
		        ftpClient.connect(server, port);
		        ftpClient.login(user, password);
		        ftpClient.enterLocalPassiveMode();
		        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

				// Es recupera l'identificador del local a partir del fitxer TXT
		        idLocal = agafarIdTxt(ruta);
		        
				// S'obté la ruta del fitxer XML al servidor i es crea un File object per al fitxer XML local
		        String arxiuRemot = rutaRemotaLocals + idLocal + ".xml";
		        File arxiuLocal = new File(rutaLocalLocals + idLocal + ".xml");
		        
				// Es comprova si el fitxer XML existeix al servidor FTP
		        if (ftpClient.retrieveFile(arxiuRemot, new FileOutputStream(arxiuLocal))) {
		        	
					// Si el fitxer XML existeix al servidor, es recupera la data del fitxer TXT i del fitxer XML
		            String dataTxt = agafarDataTxt(ruta);
		            String dataXml = llegirXml(ruta);
		            
					// Es comparen les dates del fitxer TXT i del fitxer XML per decidir quina acció s'ha de realitzar
		            compararDates(dataTxt, dataXml, ruta);
		        } else {
					// Si el fitxer XML no existeix al servidor, es converteix el fitxer TXT a XML i es pugen el fitxer XML i les fotos al servidor
		            String arxiuXml = TxtAXml(ruta);
		            pujarArxiuXml(arxiuXml);
		            String[] fotos = fotos(ruta);
		            pujarFotos(fotos);
		        }
		        
				// Es tanca la connexió amb el servidor FTP
	            ftpClient.logout();
	            ftpClient.disconnect();
		    } catch (IOException e) {
		        System.err.println("Error de E/S al conectar o desconectar del servidor FTP: " + e.getMessage());
		        e.printStackTrace();
		    } finally {
		        try {
		            if (ftpClient.isConnected()) {
		                ftpClient.disconnect();
		            }
		        } catch (IOException e) {
		            System.err.println("Error al cerrar la conexión FTP: " + e.getMessage());
		            e.printStackTrace();
		        }
		    }
	
	}
	
	// Aquesta funció compara dues dates i si són diferents executa diferents accions
	public static void compararDates(String dataTxt, String dataXml, String ruta) throws SocketException, IOException {
		
		// Es crea un objecte de DateFormat amb el format de data especificat
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        try {
        	// Es converteixen les dates String a objectes Date
            Date dataParsejadaTxt = dateFormat.parse(dataTxt);
            Date dataParsejadaXml = dateFormat.parse(dataXml);

            // Si les dates són diferents, es realitzen les següents accions
            if (!dataParsejadaTxt.equals(dataParsejadaXml)) {
            	
            	// Es crea un array de Strings amb els noms de les fotos a afegir
            	String fotosPerAfegir [] = fotos(ruta);
            	
            	// Esborrar les fotos que ja no es necessiten llegides anteriorment
            	borrarFotos(fotosABorrar);
            	// S'extreu el nom del fitxer XML a partir de la ruta i es crea el fitxer XML
            	String nom =TxtAXml(ruta);
    			pujarArxiuXml(nom);
    			// S'envien les fotos que es volen afegir
    			pujarFotos(fotosPerAfegir);
    			
            }else {
            	// Si les dates són iguals, s'informa a l'usuari
            	JOptionPane.showMessageDialog(null, "La data del arxiu no ha cambiat, no s'han actualitzat les fotos");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
		
	}

	//Mètode que llegeix un arxiu XML i n'extreu la data i els noms de les fotos contingudes.
	public static String llegirXml(String ruta) {
		
		 String arxiu = rutaLocalLocals+idLocal+".xml"; 
		 String data = "";
	        try {
	            // Crear l'objecte DocumentBuilderFactory
	            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder builder = factory.newDocumentBuilder();
	            // Parsejar l'arxiu XML
	            Document document = builder.parse(new File(arxiu));
	            // Obtindre l'element arrel
	            Element rootElement = document.getDocumentElement();
	            // Obtindre el node de la data
	            NodeList dataNodes = rootElement.getElementsByTagName("data");
	            if (dataNodes.getLength() > 0) {
	                // Obtindre el primer node de la data
	                Node dataElement =  dataNodes.item(0);
	                // Obtindre el text del node de la data
	                data = dataElement.getTextContent();

	            } else {
	            	JOptionPane.showMessageDialog(null, "El format del xml es incorrecte, torna generar-lo");
	            }
	            // Obtindre el node de les fotos
	            NodeList llistaFotos = rootElement.getElementsByTagName("foto");
	            for (int i = 0; i < llistaFotos.getLength(); i++) {
	                Node fotoNode = llistaFotos.item(i);
		            // Obtindre el node de dins de les fotos
	                if (fotoNode.getNodeType() == Node.ELEMENT_NODE) {
	                    Element photoElement = (Element) fotoNode;
	                    // Obtindre l'element <nom> de cada element <foto>
	                    NodeList llistaNoms = photoElement.getElementsByTagName("nom");
	                    if (llistaNoms.getLength() > 0) {
	                        Node nomNode = llistaNoms.item(0);
	                        String nom = nomNode.getTextContent();
	                        fotosABorrar.add(nom);
	                    }
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return data;
	}
	
	//Aquest mètode converteix el contingut d'un fitxer .txt en un fitxer .xml
	public static String TxtAXml (String ruta) {

		String contingut,nom="";
        try {
        	// Llegim el contingut del fitxer .txt
			BufferedReader br = new BufferedReader(new FileReader(rutaLocalLocals+idLocal+".txt"));
	        ArrayList<String> linies = new ArrayList<String>();
	
	        // Separem el contingut en diferents línies
	        while ((contingut = br.readLine()) != null) {
	            String[] contingutSplit = contingut.split("#");
	            for (String s : contingutSplit) {
	                linies.add(s);
	            }
	        }
	        // Obtenim la data del fitxer
	        String dataTxt = linies.get(0);
	        // Obtenim el nom del fitxer .xml a crear
	        nom = linies.get(1);
	        br.close();
	
	        // Creem el fitxer .xml i li afegim la informació obtinguda
	        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	        Document doc = docBuilder.newDocument();
	
	        Element rootElement = doc.createElement("fotos");
	        doc.appendChild(rootElement);
	
	        Element data = doc.createElement("data");
	        data.appendChild(doc.createTextNode(dataTxt));
	        rootElement.appendChild(data);
	
	        for (int i = 2; i < linies.size(); i++) {
	        	Element foto = doc.createElement("foto");
	            rootElement.appendChild(foto);
	            
	            Element lineElement = doc.createElement("nom");
	            lineElement.appendChild(doc.createTextNode(linies.get(i)));
	            foto.appendChild(lineElement);
	        }
	        
	        // Guardem el fitxer .xml
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        DOMSource source = new DOMSource(doc);
	        String xmlPath = rutaLocalLocals+nom+".xml"; // Ruta on es guardarà el fitxer .xml
	        StreamResult result = new StreamResult(new File(xmlPath));
	        transformer.transform(source, result);
	
	        System.out.println("Archivo XML creado con éxito en la ruta: " + xmlPath);  

        	} catch (IOException | ParserConfigurationException | TransformerException e) {
        		e.printStackTrace();
        	}
        return nom;
	}
	
	// Funció per pujar un arxiu XML a un servidor FTP
	public static void pujarArxiuXml(String ruta) throws SocketException, IOException {
		
		// Obtenim l'arxiu XML local a partir de la ruta especificada com a paràmetre
		File file = new File(rutaLocalLocals+idLocal+".xml");

		// Creem una instància de FTPClient per connectar-nos al servidor FTP
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        ftpClient.login(user, password);

        // Configurem la transferència d'arxius com a arxius binaris i mode passiu
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();

        // Especificem el nom remot de l'arxiu i creem una instància de FileInputStream per llegir l'arxiu local
        String remoteFile = file.getName();
        remoteFile = rutaRemotaLocals+file.getName();
        FileInputStream inputStream = new FileInputStream(file);
        
        // Pujem l'arxiu al servidor FTP i tanquem l'InputStream
        boolean success = ftpClient.storeFile(remoteFile, inputStream);
        inputStream.close();

        // Tanquem la connexió FTP
        ftpClient.logout();
        ftpClient.disconnect();

        // Mostrem un missatge de resultat depenent del resultat de la transferència
        if (success) {
            JOptionPane.showMessageDialog(null, "S'han actualitzat les imatges amb exit");
        } else {
        	JOptionPane.showMessageDialog(null, "No s'han pogut actualitzar les imatges");
        }
	}

	// Aquest mètode agafa la data d'un fitxer de text a partir de la seva ruta
	public static String agafarDataTxt(String ruta) {
		// ruta completa del fitxer a llegir
		String arxiu = rutaLocalLocals + ruta; 
		// variable on s'emmagatzemarà la data llegida
	    String data = "";
	    
	    // s'inicia la lectura del fitxer amb un try-with-resources
	    try (BufferedReader br = new BufferedReader(new FileReader(arxiu))) {
	    	
	    	// es llegeix la primera línia del fitxer, que conté data i hora juntes i id separats per #
	        String dataHoraStr = br.readLine();
	        String dataStr = dataHoraStr.split("#")[0]; // es separen data i hora de l'id
	        
	        // es crea un formateador de data/hora per obtenir la data en un format concret
	        DateTimeFormatter formatejar = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	        // es formata la data llegida amb el formateador creat
	        data = dataStr.formatted(formatejar);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return data;
	}

	/* Aquesta funció rep com a paràmetre una ruta, on es troba un fitxer de text,
	de la qual extreu l'identificador, que es troba en la primera línia del fitxer,
	separant-la pel caràcter # i obtenint la segona part.*/
	public static String agafarIdTxt(String ruta) {
		String id ="";
		String filePath = rutaLocalLocals + ruta; 
	    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	        String idtxt = br.readLine();
	        id = idtxt.split("#")[1];
	    } catch (IOException e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(null, "No s'ha trobat l'arxiu indicat");
	    }
		return id;
	}
	
	//Funció que es connecta al servidor FTP i elimina els arxius amb els noms continguts en un ArrayList de Strings.
	public static void borrarFotos(ArrayList<String> nomsArxius) {
		
		// Es connecta al servidor FTP
		 FTPClient ftpClient = new FTPClient();
		    try {
		        ftpClient.connect(server, port);
		        ftpClient.login(user, password);

		    	// Recorre tots els noms d'arxius de l'ArrayList
		        for (String nomArxiu : nomsArxius) {
		            String rutaArxiu = rutaRemotaLocals + "/" + nomArxiu;
		            //Elimina els arxius indicats
		            ftpClient.deleteFile(rutaArxiu);
		        }
		    } catch (IOException e) {
		        System.out.println("Ocurrió un error al conectarse al servidor FTP: " + e.getMessage());
		    } finally {
		        try {
		    		// Es desconnecta del servidor FTP
		        	ftpClient.logout();
		            ftpClient.disconnect();
		        } catch (IOException e) {
		            System.out.println("Ocurrió un error al desconectarse del servidor FTP: " + e.getMessage());
		        }
		    }
	}

	// Funció per pujar fotos a un servidor FTP
	public static void pujarFotos(String[] fotos) {
		// Creem un client FTP
		FTPClient ftpClient = new FTPClient();
	    try {
	    	// Ens connectem al servidor FTP
	        ftpClient.connect(server, port);
	        ftpClient.login(user, password);
	        ftpClient.enterLocalPassiveMode();
	        ftpClient.changeWorkingDirectory(rutaRemotaLocals);
	        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

	    	// Pujem cada foto de la llista
	        for (String rutaArxiu : fotos) {
	        	// Obtenim el fitxer de la ruta local
	            File arxiu = new File(rutaLocalFotos + rutaArxiu);
	    		String nomArxiu = arxiu.getName(); // Obtenim el nom del fitxer
	    		
	    		// Generem la ruta remota del fitxer
	            String rutaArxiuRemot = rutaRemotaLocals + "/" + nomArxiu;
	            // Creem un stream d'entrada del fitxer
	            FileInputStream inputStream = new FileInputStream(arxiu);
	            // Pugem el fitxer al servidor
	            boolean resultat = ftpClient.storeFile(rutaArxiuRemot, inputStream);
	            inputStream.close();
	            if (resultat) {
	                System.out.println("El archivo " + nomArxiu + " ha sido subido exitosamente.");
	            } else {
	                System.out.println("No se pudo subir el archivo " + nomArxiu + ".");
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

	//Aquesta funció retorna un array de Strings que conté el nom de les fotos que es troben a la ruta especificada com a paràmetre.
	public static String [] fotos(String ruta) {
		
	    // Inicialitzem la llista on guardarem els noms de les fotos
		 String[] fotos = null;
		 ArrayList<String> listaFotos = new ArrayList<String>();
		 // Creem un objecte File amb la ruta especificada
		 File arxiu = new File(rutaLocalLocals + ruta);
		    
		    try {
		        // Obrim un FileReader amb l'arxiu
		        FileReader fr = new FileReader(arxiu);
		        // Creem un BufferedReader per a llegir el FileReader
		        BufferedReader br = new BufferedReader(fr);
		        String linea;
		        
		        // Anem llegint cada línia de l'arxiu
		        while ((linea = br.readLine()) != null) {
		            // Si la línia acaba amb .jpg, .jpeg o .png l'afegim a la llista de fotos
		            if (linea.endsWith(".jpg") || linea.endsWith(".jpeg") || linea.endsWith(".png")) {
		                listaFotos.add(linea);
		            }
		        }
		        
		        // Tanquem el FileReader i el BufferedReader
		        fr.close();
		        br.close();
		        // Convertim la llista de fotos a un array de Strings
		        fotos = listaFotos.toArray(new String[0]);
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		    return fotos;
	}
}
