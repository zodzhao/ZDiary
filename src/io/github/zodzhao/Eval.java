package io.github.zodzhao;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Scanner;


/**
 * Created by jiazhengzhao on 8/15/17.
 * evaluate parsed instructions and read or write di.
 */
class Eval {
    static String homeDir = System.getProperty("user.home");

    String FILEPATH = homeDir + "/.zdiary/res/di/";
    String PASSPATH = homeDir + "/.zdiary/res/.secure/";
    private int password;
    private BufferedReader in;
    private Utility u;
    Console console = System.console();

    Eval(BufferedReader in) throws Exception {
        //init
        this.in = in;

        //create path
        File f = new File(FILEPATH);
        f.mkdirs();
        File pf = new File(PASSPATH);
        pf.mkdirs();


        //Generate keys for rsa
        File publicKey1 = new File(PASSPATH + "KeyPair/publicKey");
        File privateKey1 = new File(PASSPATH + "KeyPair/privateKey");

        if (!(publicKey1.exists() && privateKey1.exists())) {
            GenerateKeys gk;
            try {
                gk = new GenerateKeys(1024);
                gk.createKeys();
                gk.writeToFile(PASSPATH + "KeyPair/publicKey", gk.getPublicKey().getEncoded());
                gk.writeToFile(PASSPATH + "KeyPair/privateKey", gk.getPrivateKey().getEncoded());
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        u = new Utility();


        // Prompt
        System.out.println("INITIATING...version 0.2");

        if (console == null) {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }
        //SET OR GET PASSWORD
        File varTmpDir = new File(PASSPATH + "passobj");
        if (varTmpDir.exists()) {

            ObjectReader or = new ObjectReader(PASSPATH + "passobj");
            password = (int) or.readObject();

            // prompt input password
            char passwordArray[] = console.readPassword("Enter your password: ");
            String inputPassword = new String(passwordArray);
            //check if same;
            if ((inputPassword.hashCode()) != (password)) {
                System.exit(0);
            } else {
                System.out.println("UNLOCKED");
            }
        } else {
            //set password
            String password1 = "1";
            String password2 = "2";
            System.out.println("WELCOME TO THE YOUR DIARY");
            while (!password1.equals(password2)) {
                System.out.println("Please Enter Your Password Here:");
                password1 = in.readLine();
                System.out.println("Please Enter Your Confirm Your Password:");
                password2 = in.readLine();
            }
            ObjectWriter ow = new ObjectWriter(PASSPATH + "passobj");
            ow.writeObject(password1.hashCode());

            System.out.println("You're All Set :P");
            System.out.print(">");
        }

    }

    String eval(String line) throws Exception {
        Parse p = new Parse();

        return p.parse(line, this);

    }

    /**
     * Write the Diary
     *
     * @return
     */
    String write(String filename) throws Exception {

        // check duplicate
//        String workingDir = System.getProperty(FILEPATH);
        // a File instance for the directory:
        File workingDirFile = new File(FILEPATH);
        // a File instance for a file in that directory:
        File testfile = new File(workingDirFile, filename);
        if (testfile.exists()) {
            return "Sorry, it exists. Please come up with another name";
        } else {
            File f = new File(FILEPATH + filename);
            f.getParentFile().mkdirs();
            f.createNewFile();

            PrintWriter writer = new PrintWriter(FILEPATH + filename, "UTF-8");
            String EXIT = "finish";
            String PROMPT = "-";
            String line = "";
            System.out.print(PROMPT);
            while ((line = in.readLine()) != null) {
                if (EXIT.equals(line)) {
                    break;
                }
                if (!line.trim().isEmpty()) {
                    writer.println(u.encrypt(line));
                }
                System.out.print(PROMPT);
            }
            writer.close();
            return filename + " CLOSED and SAVED!";
        }


    }

    /**
     * STEP 1: GET THE ORIGINAL PASSWORD
     * STEP 2: SET PASSWORD LIKE NEVER DID BEFORE
     *
     * @return
     */
    String setPassword() throws IOException {
        // prompt input old password
        System.out.println("Hey you wanna change your password huh?");
        System.out.println("First, please enter the current password");
        // get the line
        char passwordArray[] = console.readPassword("Enter your old password: ");
        String usrInputOld = new String(passwordArray);

        // compare
        if (usrInputOld.hashCode() == password) {
            System.out.println("Yay you can reset now");

            //set password
            String password1 = "1";
            String password2 = "2";
            while (!password1.equals(password2)) {
                System.out.println("Please Enter Your Password Here:");
                password1 = in.readLine();
                System.out.println("Please Confirm Your Password:");
                password2 = in.readLine();
            }

            //write to object
            ObjectWriter ow = new ObjectWriter(PASSPATH + "passobj");
            ow.writeObject(password1.hashCode());
            return "Your password has been reset, please don't forget it";
        } else {
            return "Uh Oh Wrong password! Bye you idiot";
        }
    }

    String delete(String filename) {
        try {
            File file = new File(FILEPATH + filename);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filename + " Deleted!";
    }

    String read(String filename) {


        File file = new File(FILEPATH + filename);

        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String i = sc.nextLine();
                System.out.println(u.decrypt(i));
            }
            sc.close();
            return "";

        } catch (FileNotFoundException e) {
            return "This is not the droid you are looking for.";
        } catch (BadPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return "This is not the droid you are looking for.";
        }

    }

    String view() {
        File folder = new File(FILEPATH);
        File[] listOfFiles = folder.listFiles();
        try {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    System.out.println("File " + file.getName());
                } else if (file.isDirectory()) {
                    System.out.println("Directory " + file.getName());
                }
            }
            return "";
        } catch (NullPointerException e) {
            return "Sorry there is not yet any files";
        }

    }
}
