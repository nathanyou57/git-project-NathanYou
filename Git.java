import java.io.File;
import java.io.IOException;

public class Git {
// methods
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


// methods


// methods



}
