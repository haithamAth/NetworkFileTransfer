package com.company;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * UsersManager
 * register new users, retrieve registered on startup and log them in.
 * Created by haitham on 4/20/17.
 */
public class UsersManager {
    static String usersPath = "users.db";
    static HashMap<Integer ,Client> registeredUsers;
    static HashMap<Integer ,Client> loggedinUsers;

    static {
        loggedinUsers = new HashMap<>();
        try{
            registeredUsers = retrieveUsers();
        } catch (Exception e){
            try {
                (new Folder(usersPath)).createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    /**
     * Register new users
     *
     * @param fname     First Name
     * @param lname     Last Name
     * @param uname     Username
     * @param pass      Password
     * @param accRights Access Rights
     * @return Client|Null registered user object
     */
    static Client register(String fname, String lname, String uname, String pass, byte accRights) throws IOException {

        if (checkUsername(uname)) {
            Client newUser = new Client(fname, lname, uname, pass, accRights, new Date());
            newUser.updateLastLogin();
            registeredUsers.put(newUser.getId(), newUser);
            updateUsersFile();
            loggedinUsers.put(newUser.getId(), newUser);
            return newUser;
        } else {
            throw new IllegalArgumentException ("Username already registered");
        }
    }

    /**
     * Login users to system using username and password
     *
     * @param uname Client user name
     * @param pass  Client Password
     * @return boolean true if user logged in
     */
    static Client login(String uname, String pass) {
        Client cl = checkUser(uname);
        if(cl != null){
            if(checkPass(cl, pass)) {
                cl.updateLastLogin();
                loggedinUsers.put(cl.getId(), cl);
                return cl;
            } else
                throw new IllegalArgumentException("User/Password not match");
        }
        throw new IllegalArgumentException("User '"+uname+"' not found!");
    }

    static boolean checkPass(Client user, String pass) {
        return user.getPassword().equals(pass);
    }

    /**
     * Logout users from the system
     *
     * @param client user object
     * @return boolean true if logged out
     */
    static boolean logout(Client client) {
        loggedinUsers.remove(client.getId());
        return false;
    }

    static void passwd(Client user, String newPass) {
        user.setPassword(newPass);
    }

    static HashMap<Integer, Client> retrieveUsers() {
        try {
            ObjectInputStream oin = new ObjectInputStream(new FileInputStream(usersPath));

            HashMap<Integer, Client> clients = (HashMap<Integer, Client>) oin.readObject();
            Client.countUsers = clients.size();
            oin.close();
            return clients;
        } catch (Exception e) {
            //e.printStackTrace();
            return new HashMap<>();
        }
    }

    static void updateUsersFile() {
        try {
            ObjectOutputStream usersOut = new ObjectOutputStream(new FileOutputStream(usersPath));
            usersOut.writeObject(registeredUsers);
            usersOut.flush();
            usersOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean checkUsername(String uname) {
        if(! registeredUsers.isEmpty())
            for (Map.Entry<Integer, Client> clientEntry :
                    registeredUsers.entrySet()) {
            Client client = clientEntry.getValue();
                if (client.getUsername().equals(uname))
                    return false;
            }
        return true;
    }

    static Client checkUser(String uname) {
        for (Map.Entry<Integer, Client> clientEntry :
                registeredUsers.entrySet()) {
            Client client = clientEntry.getValue();
            if (client.getUsername().equals(uname))
                return client;
        }
        return null;
    }

    static void delete(Client client) throws Exception {
        if(!isLoggedIn(client)) {
            registeredUsers.remove(client.getId());
            client.delete();
            updateUsersFile();
        } else
            throw new IllegalArgumentException("You can't delete logged in users");
    }

    static boolean isLoggedIn(Client client) {
        for (Map.Entry<Integer, Client> clientEntry :
                loggedinUsers.entrySet()) {
            Client cl = clientEntry.getValue();
            if (cl.getId() == client.getId())
                return true;
        }
        return false;
    }

    static Vector<Client> getLoggedInUsers() {
        Vector<Client> out = new Vector<>();
        for (Map.Entry<Integer, Client> clientEntry :
                loggedinUsers.entrySet()) {
            out.add(clientEntry.getValue());
        }
        return out;
    }

    static Vector<Client> getRegisteredUsers() {
        Vector<Client> out = new Vector<>();
        for (Map.Entry<Integer, Client> clientEntry :
                registeredUsers.entrySet()) {
            out.add(clientEntry.getValue());
        }
        return out;
    }
}
