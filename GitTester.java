import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class GitTester {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        // TEST 1
        Git.initializeRepo();
        testInitalizeRepo();

        // TEST 2
        makeBlobTester();

        // TEST 3
        updateIndexTester();

        // TEST 4
        reset(false);
    }


// methods test initialize repository
    public static void testInitalizeRepo() {
        boolean works = true;

        File git = new File("./git");
        if (!git.isDirectory()) {
            works = false;
        }

        File objects = new File("./git/objects");
        if (!objects.isDirectory()) {
            works = false;
        }

        File index = new File("./git/index");
        if (!index.isFile()) {
            works = false;
        }

        File head = new File("./git/HEAD");
        if (!head.isFile()) {
            works = false;
        }

        if (!works) {
            System.out.println("doesn't work");
        } else {
            System.out.println("works");
        }
        
    }


// methods check that create BLOB works and also reset files for reuse
public static void makeBlobTester() throws IOException {
    cleanUp();

    String fileName = "testFile.txt";
    String message = "This is just a test blob!";
    Git.createFile(fileName, message);

    Git.toggleCompression(false); // make true if you want to compress
    Git.makeBlob(fileName);

    String expectedSHA = Git.SHA1(message);
    File blob = new File("./git/objects/" + expectedSHA);

    if (blob.exists()) {
        System.out.println("BLOB works");
    } else {
        System.out.println("BLOB Doesn't work");
    }

    new File(fileName).delete();
}


public static void cleanUp() throws IOException {
    File objects = new File("./git/objects");

    if (!objects.exists()) {
        objects.mkdir();
    }

    File[] files = objects.listFiles();
    if (files != null) {
        for (int i = files.length - 1; i >= 0; i--) {
            files[i].delete();
            System.out.println("Removed file");
        }
    }

    FileWriter wipeIndex = new FileWriter("./git/index", false);
    wipeIndex.close();

    String[] tempFiles = {"output", "output2", "output3"};
    for (String name : tempFiles) {
        File f = new File(name);
        if (!f.exists()) {
            f.createNewFile();
        }
        f.delete();
    }
}


// methods Check whether or not index works
public static void updateIndexTester() throws IOException, NoSuchAlgorithmException {
    cleanUp();

    String[] filenames = {"a.txt", "b.txt", "c.txt"};
    String[] contents = {
        "first blob content",
        "second blob content",
        "totally different third one"
    };

    for (int i = 0; i < filenames.length; i++) {
        Git.createFile(filenames[i], contents[i]);
    }

    for (int i = 0; i < filenames.length; i++) {
        Git.makeBlob(filenames[i]);
        Git.updateIndex(new File(filenames[i]));
    }

    List<String> indexLines = Files.readAllLines(Paths.get("./git/index"), StandardCharsets.UTF_8);

    boolean allPassed = true;

    for (int i = 0; i < filenames.length; i++) {
        String expectedSHA = Git.SHA1(contents[i]);
        File blob = new File("./git/objects/" + expectedSHA);

        if (!blob.exists()) {
            System.out.println("BLOB don't exist");
            allPassed = false;
        }

        String expectedLine = expectedSHA + " " + filenames[i];
        if (!indexLines.contains(expectedLine)) {
            System.out.println("Index Don't work");
            allPassed = false;
        }
    }

    if (allPassed) {
        System.out.println("Index work");
    }

    for (String name : filenames) {
        new File(name).delete();
    }
}


// methods Reset all non java files (true means that you want to remove all non java files in the current directory and recursively walks into the subdirectories to remove all other non java files. false means that you just want to remove from the current directory)


private static void removeNonJavaFiles(File dir) {
    File[] entries = dir.listFiles();
    if (entries == null) return;

    for (File f : entries) {
        if (f.isDirectory()) {
            if (f.getName().equals("git")) continue;
            removeNonJavaFiles(f);
        } else {
            if (!f.getName().endsWith(".java")) {
                f.delete();
            }
        }
    }
}

public static void reset(boolean depth) throws IOException {

    // BLOBS
    File objects = new File("./git/objects");
    if (objects.exists() && objects.isDirectory()) {
        File[] entries = objects.listFiles();
        if (entries != null) {
            for (File k : entries) removeNonJavaFiles(k);
            entries = objects.listFiles();
            if (entries != null) for (File k : entries) k.delete();
        }
    }

    // INDEX
    File index = new File("./git/index");
    if (index.exists()) {
        try (FileWriter fw = new FileWriter(index, false)) { /* empty */ }
    }

    File[] here = new File(".").listFiles();
    if (here == null) return;

    for (File f : here) {
        if (f.getName().equals("git")) continue;

        if (f.isDirectory()) {
            if (depth) {
                removeNonJavaFiles(f);
            }
        } else {
            if (!f.getName().endsWith(".java")) {
                f.delete();
            }
        }
    }
}



}
