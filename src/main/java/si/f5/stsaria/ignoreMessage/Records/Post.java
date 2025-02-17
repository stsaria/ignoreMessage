package si.f5.stsaria.ignoreMessage.Records;

public class Post {
    public final String id;
    public final int type;
    public final String author;
    public final long sendUnixTime;
    public final String content;
    public final String ip;

    public final String csvString;

    public Post(String id, int type, String author, long sendUnixTime, String content, String ip){
        this.id = id;
        this.type = type;
        this.author = author;
        this.sendUnixTime = sendUnixTime;
        this.content = content;
        this.ip = ip;

        this.csvString = id+","+type+","+author+","+sendUnixTime+","+content+","+ip;
    }
}
