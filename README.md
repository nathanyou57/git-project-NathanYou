# git-project-NathanYou
GP (2.1) - In my program, InitializeRepo() sets up a  Git-like repository by creating a git folder in the current directory and, inside it, the objects folder and also the index and HEAD files if they hadn't already existed. To check my setup, I wrote a tester to make sure all the files and folders were there.

GP (2.2) - Documentation here --> https://ssojet.com/hashing/sha-1-in-java/
The method starts off by creating a SHA-1 MessageDigest, feed it the UTF-8 bytes of the input string to compute the raw 20-byte digest, convert that digest into a base-16 string, then pad the hex with leading zeros until there are 40 characters. The method returns the 40-char hex hash.

GP (2.3) - Reads the file contents and then creates the SHA1 of the read contents. Then it builds the blob path and writes the original contents in it so that the BLOB can store an exact copy.

GP (2.3.1) - Documentation here --> https://flylib.com/books/en/1.134.1/compressing_and_decompressing_streams.html
also added a compression toggle, you can choose whether or not to compress the file in the tester.

GP (2.4) - If the index file was empty, all I did was write the line. If the index already had entries, I needed a newline character first so the new line starts correctly. This way there are no trailing spaces on any line and also no extra blank line at the end.