package smartfriend.util.general;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author user
 */
public class ImageXMLParser {

    private String ID;
    private String imageName;
    private String imageLocation;
    private String imageDescription;
    private String xmlName;
    
    public ImageXMLParser(String xmlName){
        
        this.xmlName=xmlName;
    }
    
    public void setImageDetails(String ID, String name,String imageDescription, String location) {
        this.ID = ID;
        this.imageName = name;
        this.imageDescription= imageDescription;
        this.imageLocation = location;
    }

    public String getImageID() {
        
        return ID;
    }

    public String getImageName(int imageID) {
        returnImageDetails(Integer.toString(imageID));
        return imageName;
    }

    public String getImageDescription(int imageID) {
        returnImageDetails(Integer.toString(imageID));
        return imageDescription;
    }
    
    public String getImageLocation(int imageID) {
        returnImageDetails(Integer.toString(imageID));
        return imageLocation;
    }

    public void returnImageDetails(String ImageID) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Load the input XML document, parse it and return an instance of the Document class.
            Document document = builder.parse(MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty(xmlName));

            NodeList nodeList = document.getDocumentElement().getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    String ID = node.getAttributes().getNamedItem("ID").getNodeValue();
                    if (ID.equals(ImageID)) {

                        String imagename = elem.getElementsByTagName("Imagename")
                                .item(0).getChildNodes().item(0).getNodeValue();
                        
                        String imagedescription = elem.getElementsByTagName("Imagedescription")
                                .item(0).getChildNodes().item(0).getNodeValue();

                        String imagelocation = MainConfiguration.getCurrentDirectory() + elem.getElementsByTagName("Imagelocation").item(0)
                                .getChildNodes().item(0).getNodeValue();
                        setImageDetails(ID, imagename, imagedescription,imagelocation);
                    }
                }
            }
        } catch (ParserConfigurationException p) {

        } catch (SAXException s) {

        } catch (IOException o) {

        }
    }
}
