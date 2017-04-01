import java.io.*;
import java.util.*;
import java.util.function.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import com.sun.nio.file.SensitivityWatchEventModifier;

public class WatchDo {

    public static int ARGUMENT_LIMIT = 2;
    public static String TIMESTAMP_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public static void main(String[] args) {
        if (args.length != ARGUMENT_LIMIT) {
            System.out.println("Incorrect number of arguments");
            System.exit(-1);
        }

        File file = new File(args[0]);
        if (!file.exists() || file.isDirectory()) {
            System.out.println(String.format("File '%s' does not exist", 
                file.getName()));
            System.exit(-1);
        }

        try {
            run(file, args[1]);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    private static void run(File file, String command) 
        throws IOException, InterruptedException {
        String parentDirectory = file.getParent() != null ? file.getParent() 
            : System.getProperty("user.dir");
        final Path userPath = FileSystems.getDefault().getPath(parentDirectory);
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            userPath.register(watchService, 
                    new WatchEvent.Kind<?>[] {StandardWatchEventKinds.ENTRY_MODIFY},
                    SensitivityWatchEventModifier.HIGH);
            while (true) {
                WatchKey watchKey = watchService.take();
                watchKey.pollEvents().stream()
                    .map((WatchEvent<?> event) -> (Path) event.context())
                    .filter((Path path) -> path.endsWith(file.getName()))
                    .forEach((Path changed) -> {
                        printChangeMessage(changed.toString());
                        System.out.print(executeCommand(command));
                    });

                if (!watchKey.reset()) break;
            }
        }
    }

    private static String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        String line = new String();

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new 
                    InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) output.append(line + "\n");
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        return output.toString();
    }

    private static void printChangeMessage(String fileName) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(String.format("%s <changed at %s>", fileName, 
            DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT).format(now)));
    }

}
