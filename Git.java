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
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class Git {

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

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
            return null;
        }
        StringBuilder treeContent = new StringBuilder();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    if (treeContent.length() > 0) {
                        treeContent.append("\n");
                    }
                    String fileType = file.isDirectory() ? "tree" : "blob";
                    String fileSHA;
                    if (file.isDirectory()) {
                        fileSHA = storeTree(file.getPath());
                    } else {
                        makeBlob(file.getPath());
                        fileSHA = SHA1(readFile(file.getPath()));
                    }
                    treeContent.append(fileType).append(" ").append(fileSHA).append(" ").append(file.getName());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                String treeSHA = SHA1(treeContent.toString());
                createFile("./git/objects/" + treeSHA, treeContent.toString());
                return treeSHA;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    // methods working list operations
    /*
     * Creates a working list file in the objects directory based on the current
     * index.
     * 
     * @return true if the working list was created successfully, otherwise return
     * false
     * 
     */
    public static boolean createWorkingList() throws IOException {
        File workingList = new File("./git/objects/workingList");
        workingList.createNewFile();
        File indexFile = new File("./git/index");
        BufferedReader br = new BufferedReader(new FileReader(indexFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(workingList));
        String line;
        boolean first = true;
        while ((line = br.readLine()) != null) {
            if (!first) {
                bw.write("\n");
            }
            bw.write("blob " + line);
            first = false;
        }
        br.close();
        bw.close();
        return true;
    }

    /*
     * Processes the working list and generates a tree object if needed.
     * 
     * @return the SHA1 hash of the created tree object, and null if no tree was
     * created.
     * 
     */
    public static String processWorkingList() {
        File workingList = new File("./git/objects/workingList");
        if (!workingList.exists()) {
            return null;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(workingList));
            String line;
            String deepestDirPath = null;
            int maxDepth = -1;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ", 3);
                if (parts.length >= 3) {
                    String filePath = parts[2];
                    File file = new File(filePath);
                    // screwed up file paths on my compu
                    String path = file.getPath().replace("\\", "/");
                    int depth = path.split("/").length;
                    if (depth > maxDepth) {
                        maxDepth = depth;
                        deepestDirPath = file.getParent();
                    }
                }
            }
            br.close();
            if (deepestDirPath != null) {
                File currentDir = new File(".").getAbsoluteFile();
                File deepestDir = new File(deepestDirPath).getAbsoluteFile();
                if (deepestDir.equals(currentDir)) {
                    return null;
                }
                String treeSHA = storeTree(deepestDirPath);
                BufferedReader br2 = new BufferedReader(new FileReader(workingList));
                StringBuilder newWorkingList = new StringBuilder();
                boolean foundTreeEntry = false;
                while ((line = br2.readLine()) != null) {
                    if (line.trim().isEmpty())
                        continue;
                    String[] parts = line.split(" ", 3);
                    if (parts.length >= 3) {
                        String filePath = parts[2];
                        File file = new File(filePath);
                        if (deepestDirPath.equals(file.getParent())) {
                            if (!foundTreeEntry) {
                                if (newWorkingList.length() > 0) {
                                    newWorkingList.append("\n");
                                }
                                newWorkingList.append("tree ").append(treeSHA).append(" ").append(deepestDirPath);
                                foundTreeEntry = true;
                            }
                        } else {
                            if (newWorkingList.length() > 0) {
                                newWorkingList.append("\n");
                            }
                            newWorkingList.append(line);
                        }
                    }
                }
                br2.close();
                BufferedWriter bw = new BufferedWriter(new FileWriter(workingList));
                bw.write(newWorkingList.toString());
                bw.close();
                return treeSHA;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createRootTreeFile(String workingListContents, String rootTreeSHA) throws IOException {
        File rootTree = new File("git/objects/" + rootTreeSHA);
        FileWriter rootTreeWriter = new FileWriter(rootTree);
        rootTreeWriter.write(workingListContents);
        rootTreeWriter.close();
    }

    // arbitrary change
    /*
     * Generates all tree objects from the working list until only one tree remains.
     * 
     * @return The SHA1 hash of the root tree object, and null if no trees
     */
    public static String generateAllTrees() {
        try {
            createWorkingList();
            String rootTreeSHA = null;
            String currentTreeSHA;
            while ((currentTreeSHA = processWorkingList()) != null) {
                rootTreeSHA = currentTreeSHA;
            }
            return rootTreeSHA;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String commit(String author, String message) throws IOException {
        StringBuilder commitContentsStringBuilder = new StringBuilder();
        String rootTreeHash = generateAllTrees();
        commitContentsStringBuilder.append("tree: " + rootTreeHash);
        BufferedReader headReader = new BufferedReader(new FileReader("./git/HEAD"));
        String parentCommitHash = headReader.readLine();
        if (parentCommitHash == null) {
            parentCommitHash = "";
        }
        commitContentsStringBuilder.append("\nparent: " + parentCommitHash);
        headReader.close();
        commitContentsStringBuilder.append("\nauthor: " + author);
        String date = now();
        commitContentsStringBuilder.append("\ndate: " + date);
        commitContentsStringBuilder.append("\nmessage: " + message);
        String commitContents = commitContentsStringBuilder.toString();
        String hashOfCommitContents = SHA1(commitContents);
        File commit = new File("git/objects/" + hashOfCommitContents);
        commit.createNewFile();
        FileWriter commitWriter = new FileWriter(commit);
        commitWriter.write(commitContents);
        commitWriter.close();
        updateHead(hashOfCommitContents);
        return hashOfCommitContents;
    }

    public static String now() throws IOException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    public static void updateHead(String hashOfCommitContents) throws IOException {
        File head = new File("./git/HEAD");
        FileWriter headWriter = new FileWriter(head);
        headWriter.write(hashOfCommitContents);
        headWriter.close();
    }

}