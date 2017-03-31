import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class WatchFile {

    public static int pollRate = 1000;
    public static String DateFormat = "yyyy/MM/dd HH:mm:ss";

    public static void main(String[] args) {
        String fileName = args[0];
        String command = args[1];

        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            System.out.println(String.format("File '%s' does not exist", 
                        fileName));
            System.exit(0);
        }

        TimerTask task = new FileWatcher(file) {
            protected void onChange(File file) {
                LocalDateTime now = LocalDateTime.now();
                System.out.println(String.format("%s <changed at %s>", 
                    file.getName(),
                    DateTimeFormatter.ofPattern(DateFormat).format(now)));
                try {
                    System.out.println(executeCommand(command));
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, new Date(), pollRate);
    }

    private static String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        String line = new String();
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        return output.toString();
    }
}
