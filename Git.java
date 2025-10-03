import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;

public class Git {
    // methods INITIALIZING A REPOSITORY
    public static void initializeRepo() throws java.io.IOException {
        java.io.File gitDir = new java.io.File("./git");
        java.io.File objectsDir = new java.io.File(gitDir, "objects");
        java.io.File indexFile = new java.io.File(gitDir, "index");
        java.io.File headFile = new java.io.File(gitDir, "HEAD");

        if (gitDir.isDirectory() && objectsDir.isDirectory() && indexFile.isFile() && headFile.isFile()) {
            System.out.println("Git Repository Already Exists");
            return;
        }

        if (!gitDir.exists()) {
            gitDir.mkdir();
        }
        if (!objectsDir.exists()) {
            objectsDir.mkdir();
        }
        if (!indexFile.exists()) {
            indexFile.createNewFile();
        }
        if (!headFile.exists()) {
            headFile.createNewFile();
        }

        System.out.println("Git Repository Created");
    }

    // methods GENERATING THE SHA1 HASH

    public static String SHA1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1"); // selects the SHA1 hash function
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8)); // returns a byte array

            String hex = new BigInteger(1, digest).toString(16); // turns the byte array into hex text
            while (hex.length() < 40) {
                hex = "0" + hex; // add "0s" to the left to 40 chars
            }
            return hex; // returns the SHA1 hash

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // methods CREATING BLOB FILES IN THE OBJECTS DIRECTORY

    private static boolean compression = true;

    public static void toggleCompression(boolean on) {
        compression = on;
    }

    public static void makeBlob(String filePath) throws IOException {
        File objectsDir = new File("./git/objects");

        if (!objectsDir.isDirectory()) {
            initializeRepo();
        }

        if (compression) {
            File originalFile = new File(filePath);
            File zipped = compressFile(originalFile);
            filePath = zipped.getPath(); // switch to use the compressed file
        }

        String contents = readFile(filePath); // read
        String sha = SHA1(contents); // hash

        String blobPath = "./git/objects/" + sha; // create blob file path

        File blobFile = new File(blobPath); // write into the blob file
        if (!blobFile.exists()) {
            createFile(blobPath, contents); // create the file (this is for if it doesn't exist)

        }
    }

    // methods READ AND COPY FILES

    public static String readFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8));) {
            int ch;

            while ((ch = br.read()) != -1) {
                sb.append((char) ch);
            }
            br.close();
        }

        return sb.toString();
    }

    public static boolean createFile(String path, String content) throws IOException {
        File f = new File(path);

        if (f.exists()) {
            return false;
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8));) {
            if (content != null) {
                w.write(content);
            }
        }

        return true;
    }

    // methods COMPRESSION

    public static File compressFile(File inputFile) throws IOException {
        FileInputStream in = new FileInputStream(inputFile.toString()); // input stream reads original file

        FileOutputStream out = new FileOutputStream(inputFile.toString() + ".zip"); // where the compressed file will be
                                                                                    // written
        DeflaterOutputStream deflaterOut = new DeflaterOutputStream(out);

        int currentByte;
        while ((currentByte = in.read()) != -1) {
            deflaterOut.write(currentByte); // Transfer data over
        }

        in.close();
        deflaterOut.close();

        return new File(inputFile.toString() + ".zip");
    }

    // methods Updating the Index File

    public static void updateIndex(File file) throws IOException, NoSuchAlgorithmException {

        String content = readFile(file.getPath());
        String sha = SHA1(content);

        File index = new File("./git/index");
        if (!index.exists()) {
            index.createNewFile();
        }

        String line;
        if (index.length() == 0) {
            line = sha + " " + file.getPath();
        } else {
            line = "\n" + sha + " " + file.getPath();
        }

        try (FileWriter writer = new FileWriter(index, true)) {
            writer.write(line);
        }

        System.out.println("Added to index: " + file.getName());
    }

    // methods Making Tree Objects
    /*
     * Stores a directory as a tree in the objects folder, recursively
     * storing subdirectories as trees as well.
     * 
     * @param directoryPath The path to the directory to be stored as a tree.
     * 
     * @return The SHA1 hash of the stored tree object.
     */
    public static String storeTree(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
            System.out.println("The path " + directoryPath + " is not a directory.");
            return null;
        }
        StringBuilder treeContent = new StringBuilder();
        File[] files = dir.listFiles();
        for (File file : files) {
            System.out.println(file.getPath());
        }
        if (files != null) {
            for (File file : files) {
                try {
                    if (treeContent.length() > 0) {
                        treeContent.append("\n");
                    }
                    String fileType = file.isDirectory() ? "tree" : "blob";
                    String fileSHA = file.isDirectory() ? storeTree(file.getPath()) : SHA1(readFile(file.getPath()));
                    treeContent.append(fileType).append(" ").append(fileSHA).append(" ").append(file.getName());
                    System.out.println(treeContent.toString());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                createFile("tempTreeFile", treeContent.toString());
                makeBlob("tempTreeFile");
                File tempTreeFile = new File("tempTreeFile");
                tempTreeFile.delete();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return SHA1(treeContent.toString());
        }
        return null;
    }

    // methods working list operations

    public static boolean createWorkingList() throws IOException {
        File objectsDir = new File("./git/objects");
        File workingList = new File(objectsDir.getPath() + "/workingList");
        workingList.createNewFile();
        File indexFile = new File("./git/index");
        BufferedReader br = new BufferedReader(new FileReader(indexFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(workingList, true));
        int iteration = 0;
        while (br.ready()) {
            String line = br.readLine();
            if (iteration == 0)
                bw.write((new File(line.substring(line.indexOf(" "), line.length())).isDirectory() ? "tree " : "blob ")
                        + line);
            else
                bw.write("\n" + (new File(line.substring(line.indexOf(" "), line.length())).isDirectory() ? "tree "
                        : "blob ") + line);
            iteration++;
        }
        br.close();
        bw.close();
        return true;
    }
}
