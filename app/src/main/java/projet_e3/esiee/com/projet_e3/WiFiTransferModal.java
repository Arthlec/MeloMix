package projet_e3.esiee.com.projet_e3;
import java.io.Serializable;

public class WiFiTransferModal implements Serializable {

    private String FileName;
    private Long FileLength;
    private String InetAddress;
    //private String extension;


    public WiFiTransferModal() {

    }

    public WiFiTransferModal(String inetaddress) {
        this.InetAddress = inetaddress;
    }

    public WiFiTransferModal(String name, Long filelength) {
        this.FileName = name;
        this.FileLength = filelength;
    }
    public String getInetAddress() {
        return InetAddress;
    }

    public Long getFileLength() {
        return FileLength;
    }

    public String getFileName() {
        return FileName;
    }


}