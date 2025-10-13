import java.io.IOError;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class GitWrapperTester {
    public static void main(String args[]) throws IOException, NoSuchAlgorithmException {
        Git.initializeRepo();

        GitWrapper gw = new GitWrapper();
        gw.add("myProgram/inner/world.txt");
        gw.add("myProgram/hello.txt");
        gw.commit("John Doe", "Initial commit");
        //gw.checkout("1234567890");

    }

}