package si.f5.stsaria.ignoreMessage.RecordFileControllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class BanIpFC {
    public static final Object lock = new Object();
    public static String read() throws IOException {
        Path path = Paths.get("records/banIp.record");
        if (path.toFile().isFile()) {
            return Files.readString(path);
        } else {
            return "";
        }
    }
    public static ArrayList<String> records() throws IOException {
        return new ArrayList<>(Arrays.asList(read().split("\n")));
    }
}
