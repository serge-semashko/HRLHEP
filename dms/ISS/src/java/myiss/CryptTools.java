package myiss;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class CryptTools {
    private static String algorithm = "DESede";
    private static Key key = null;
    private static Cipher cipher = null;
    private static void setUp() throws Exception {

           byte[] keyBytes = "1234567890azertyuiopqsdf".getBytes("ASCII");
           DESedeKeySpec keySpec = new DESedeKeySpec(keyBytes);
           SecretKeyFactory factory = SecretKeyFactory.getInstance("DESede");
           key = factory.generateSecret(keySpec);   
           cipher = Cipher.getInstance(algorithm);

       }

    public CryptTools() {
    
    }
    
    private static byte[] encrypt(String input)

             throws InvalidKeyException, 

                    BadPaddingException,

                    IllegalBlockSizeException {

             cipher.init(Cipher.ENCRYPT_MODE, key);

             byte[] inputBytes = input.getBytes();

             return cipher.doFinal(inputBytes);

         }



         private static String decrypt(byte[] encryptionBytes)

             throws InvalidKeyException, 

                    BadPaddingException,

                    IllegalBlockSizeException, IllegalBlockSizeException, 
            BadPaddingException, InvalidKeyException {

             cipher.init(Cipher.DECRYPT_MODE, key);
               System.out.println(new String(encryptionBytes));
             byte[] recoveredBytes = cipher.doFinal(encryptionBytes);
             

             String recovered =   new String(recoveredBytes);

             return recovered;

           }
    public static void main(String[] args) 

              throws Exception {

               setUp();

    
    byte[] encryptionBytes = null;

         String input = "АБВГДЕЙКА АБВГДЕЙКА";

         System.out.println("Entered: " + input);

         encryptionBytes = encrypt(input);

         System.out.println(

           "Recovered: " + decrypt(encryptionBytes));

     }
}
