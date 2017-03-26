import java.util.*;
import java.io.*;
import java.security.*;

public abstract class FileWatcher extends TimerTask {

    private MessageDigest digest;
    private File file;

    protected abstract void onChange(File file);

    public FileWatcher(File file) throws IOException, NoSuchAlgorithmException {
        this.file = file;
        this.digest = this.computeFileDigest(file);
    }

    public final void run() {
        try {
            MessageDigest digest = this.computeFileDigest(this.file);
            if (this.digest != digest) {
                this.digest = digest;
                System.out.println(digestToString(digest));
                onChange(file);
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return;
        }
    }

    protected MessageDigest computeFileDigest(File file) 
        throws IOException, NoSuchAlgorithmException {

        MessageDigest digest = null;
        int nRead = 0;
        byte[] buffer = new byte[1000];
        try (FileInputStream inputStream = new FileInputStream(file)) {
          digest = MessageDigest.getInstance("MD5");
          while ((nRead = inputStream.read(buffer)) != -1) {
              digest.update(buffer, 0, nRead);
          }
        }

        return digest;
    }

    private String digestToString(MessageDigest digest) {
        byte[] mdbytes = digest.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
