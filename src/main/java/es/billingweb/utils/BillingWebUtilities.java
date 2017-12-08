/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.utils;

import com.google.gson.Gson;
import es.billingweb.model.tables.pojos.ItUser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.faces.event.ActionEvent;

/**
 * Utilities package. Class that contents some utilities for the application,
 * like the encryption MD5 method, date formats and default dates
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 *
 */
public class BillingWebUtilities {

    /**
     * getMD5: method that encrypt a string with MD5 algorithm reference.
     * source: http://www.asjava.com/core-java/java-md5-example/
     *
     * @param input: string to encrypt
     * @return: encrypted string
     */
    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the object of the event with the given name
     *
     * @param event
     * @param name
     * @return
     */
    public static Object getActionAttribute(ActionEvent event, String name) {
        return event.getComponent().getAttributes().get(name);
    }

    /**
     * Return the clientId from the table that contents the cell. The client id
     * from the cell has the format [tableClientId]:[row]:[cell]
     *
     * @param clientId string to the client Id from cell
     * @return the clientId for the table that contens the cell
     */
    public static String getTableIdFromCellId(String clientId) {
        String string = null;
        int totalOcurrence;
        int limit;
        int i, count;
        StringBuilder aux = new StringBuilder(clientId.length());

        totalOcurrence = clientId.length() - clientId.replace(":", "").length();
        // we stop just in:
        // [tableClientId]:[row]:[cell]        
        limit = (totalOcurrence - 1);
        count = 0;

        if (limit <= 0) {
            return string;
        }

        for (i = 0; i < clientId.length(); i++) {
            if (clientId.charAt(i) == ':') {
                count = count + 1;
            }
            if (count == limit) {
                break;
            } else {
                aux.append(clientId.charAt(i));
            }
        }

        string = aux.toString();

        return string;

    }

    public static <T> void replaceObjectInList(List<T> list, T object, int pos) {
        // Retrieves the old value for the current row
        list.remove(pos);
        list.add(pos, object);
    }

    /*
    public static <T> void copyGenericList(List<T> source, List<T> destination) {

        Class<? extends T> aux;
        T copyData = (T) new Object();
        Field[] properties;
        String propertyName;

        destination.clear();

        for (T obj : source) {
            aux = (Class<T>) obj.getClass();
            properties = aux.getDeclaredFields();
            for (Field property : properties) {
                propertyName = property.getName();
                if (!Modifier.isStatic(property.getModifiers())) {
                    // Discard static property (SERIAL)
                    //copyData.set()
                }
            }
            destination.add(obj);
        }
    }
     */
    public static void copyItUserList(List<ItUser> source, List<ItUser> destination) {

        ItUser copyUser = new ItUser();

        destination.clear();

        for (ItUser user : source) {
            copyUser.from(user);
            destination.add(copyUser);
        }
    }


    /*
public <T> T deepCopy(T object, Class<T> type) {
    try {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(object, type), type);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
     */
    public static Object deepClone(Object object) throws Exception {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            throw e;
        }
    }

    public static <T> void copyGenericList(List<T> source, List<T> destination) throws Exception {

        T aux;
        destination.clear();

        for (T obj : source) {
            aux = (T) deepClone(obj);
            destination.add(aux);
        }

    }
    
    public static boolean canModify (String profile){
        boolean permission = false;
        
     switch (profile) {
         case "ADMIN":
             permission=true;
             break;         
         default:
             
     }
     return permission;
    }


}
