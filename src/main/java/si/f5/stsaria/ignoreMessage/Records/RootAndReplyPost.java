package si.f5.stsaria.ignoreMessage.Records;

import java.util.ArrayList;

public class RootAndReplyPost{
    public final Post rootPost;
    public final ArrayList<Post> replyPosts;

    public RootAndReplyPost(Post rootPost, ArrayList<Post> replyPosts){
        this.rootPost = rootPost;
        this.replyPosts = replyPosts;
    }
}
