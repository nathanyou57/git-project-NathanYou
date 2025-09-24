import java.io.File;
import java.io.IOException;

public class InitalizeARepositoryTester {

    public static void main(String[] args) throws IOException {
        // TEST 1
        Git.initializeRepo();
        testInitalizeRepo();

        // TEST 2
        testCleanup();
    }

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

    public static void testCleanup() {
        boolean removed = cleanGit();

        if (removed && !(new File("./git").exists())) {
            System.out.println("cleanup works");
        } 
        else {
            System.out.println("cleanup doesn't work");
        }
    }

    public static boolean cleanGit() {
        File gitDir = new File("./git");

        if (!gitDir.exists()){
            return true;
            }

        return deleteDirectory(gitDir);
    }

public static boolean deleteDirectory(File dir) {
    if (dir == null || !dir.exists()){
        return false;
    }

    File[] directoryContents = dir.listFiles();
    if (directoryContents != null) {
        for (File entry : directoryContents) {
            if (entry.isDirectory()) {
                if (!deleteDirectory(entry)){
                    return false;
                }
            }
            else {
                if (!deleteFile(entry)){
                return false;
                }
            }
        }
    }
    return dir.delete();
}

    public static boolean deleteFile(File f) {
        if (f == null){
            return false;
        }
        if (!f.exists()){
            return true;
          }

        return f.delete();
    }
}
