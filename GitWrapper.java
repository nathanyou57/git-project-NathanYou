import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.io.File;

public class GitWrapper {

    /**
     * Initializes a new Git repository.
     * This method creates the necessary directory structure
     * and initial files (index, HEAD) required for a Git repository.
     */
    public void init() throws IOException {
        Git.initializeRepo();
    };

    /**
     * Stages a file for the next commit.
     * This method adds a file to the index file.
     * If the file does not exist, it throws an IOException.
     * If the file is a directory, it throws an IOException.
     * If the file is already in the index, it does nothing.
     * If the file is successfully staged, it creates a blob for the file.
     * 
     * @param filePath The path to the file to be staged.
     */
    public void add(String filePath) throws IOException, NoSuchAlgorithmException {
        String filePathOfAllDirs = createFilePathOfAllDirs(filePath);
        File creatorOfAllDirs = new File(filePathOfAllDirs);
        creatorOfAllDirs.mkdirs();
        File fileCreator = new File(filePath);
        fileCreator.createNewFile();
        Git.makeBlob(filePath);
        Git.updateIndex(fileCreator);
    };

    public String createFilePathOfAllDirs(String filePath) throws IOException {
        for (int i = filePath.length() - 1; i >= 0; i--) {
            if (filePath.charAt(i) == '/') {
                return filePath.substring(0, i);
            }
        }
        return "";
    }

    /**
     * Creates a commit with the given author and message.
     * It should capture the current state of the repository by building trees based
     * on the index file,
     * writing the tree to the objects directory,
     * writing the commit to the objects directory,
     * updating the HEAD file,
     * and returning the commit hash.
     * 
     * The commit should be formatted as follows:
     * tree: <tree_sha>
     * parent: <parent_sha>
     * author: <author>
     * date: <date>
     * summary: <summary>
     *
     * @param author  The name of the author making the commit.
     * @param message The commit message describing the changes.
     * @return The SHA1 hash of the new commit.
     */
    public String commit(String author, String message) throws IOException {
        String hashOfCommitContents = Git.commit(author, message);
        return hashOfCommitContents;
    };

    /**
     * EXTRA CREDIT:
     * Checks out a specific commit given its hash.
     * This method should read the HEAD file to determine the "checked out" commit.
     * Then it should update the working directory to match the
     * state of the repository at that commit by tracing through the root tree and
     * all its children.
     *
     * @param commitHash The SHA1 hash of the commit to check out.
     */
    public void checkout(String commitHash) {

    };
}