package jinr.arch.upload;
import java.io.*;

class SendObj implements Serializable{
    byte [] b;
    int id;
    int n;
    String name;
    
    SendObj(byte [] byt, int i_d, int no, String nam){
        b = new byte[byt.length];
        b = byt;
        id = i_d;
        n = no;
        name = nam;
    }
        
}
