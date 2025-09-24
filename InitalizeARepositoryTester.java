import java.io.File;
import java.io.IOException;

public class InitalizeARepositoryTester{
    public static void main(String[] args) throws IOException {
        Git.initializeRepo();
        testInitalizeRepo();
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
}