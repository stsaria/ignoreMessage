package si.f5.stsaria.ignoreMessage.RecordFileControllers;

import si.f5.stsaria.ignoreMessage.Records.Post;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class PostFC {
    public static final Object lock = new Object();
    public static String read() throws IOException {
        Path path = Paths.get("records/message.record");
        if (path.toFile().isFile()) {
            return Files.readString(path);
        } else {
            return "";
        }
    }
    public static void append(String string) throws IOException {
        File file = new File("records/message.record");
        FileWriter writer = new FileWriter(file, true);
        writer.write(string+"\n");
        writer.close();
    }
    public static ArrayList<Post> records() throws IOException {
        ArrayList<Post> posts = new ArrayList<>();
        String[] recordSplitComma;
        for (String record : read().split("\n")){
            record = record.strip();
            recordSplitComma = record.split(",");
            if (recordSplitComma.length != 6) continue;
            try {Integer.parseInt(recordSplitComma[1]);} catch (Exception e) {continue;}
            try {Long.parseLong(recordSplitComma[3]);} catch (Exception e) {continue;}
            posts.add(new Post(recordSplitComma[0], Integer.parseInt(recordSplitComma[1]), recordSplitComma[2], Long.parseLong(recordSplitComma[3]), recordSplitComma[4], recordSplitComma[5]));
        }
        return posts;
    }
}
