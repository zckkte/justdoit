import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.security.*;

public class WatchDoProgram {

    public static int pollRate = 1000;

    public static void main(String[] args) {
        String fileName = args[0];

        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            System.out.println(String.format("File '%s' does not exist", fileName));
            return;
        }

        try {
            TimerTask task = new FileWatcher(file) {
                protected void onChange(File file) {
                    LocalDateTime now = LocalDateTime.now();
                    System.out.println(String.format("%s <changed at %s>", 
                        file.getName(),
                        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(now)));
                }
            };

            Timer timer = new Timer();
            timer.schedule(task, new Date(), pollRate);
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }


    }
}
