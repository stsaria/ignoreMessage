package si.f5.stsaria.ignoreMessage.Recorders;

import org.apache.commons.lang3.StringUtils;
import si.f5.stsaria.ignoreMessage.IgnoreMessageApplication;
import si.f5.stsaria.ignoreMessage.RecordFileControllers.BanIpFC;
import si.f5.stsaria.ignoreMessage.RecordFileControllers.PostFC;
import si.f5.stsaria.ignoreMessage.Records.Post;
import si.f5.stsaria.ignoreMessage.Records.RootAndReplyPost;
import si.f5.stsaria.ignoreMessage.TimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PostR {
    public final Post emptyRootPost = new Post("", 0, "", 0, "", "");
    public final RootAndReplyPost emptyRootAndReplyPost = new RootAndReplyPost(emptyRootPost, new ArrayList<>());
    private Post getRootMessage(String rootPostId){
        ArrayList<Post> allPosts;
        synchronized (PostFC.lock) {
            try {
                allPosts = PostFC.records();
            } catch (Exception ignore) {
                return this.emptyRootPost;
            }
        }
        for (Post post : allPosts.reversed()){
            if (post.id.equals(rootPostId) && post.type == 0) return post;
        }
        return this.emptyRootPost;
    }
    private ArrayList<Post> getReplyMessages(String rootPostId){
        ArrayList<Post> allPosts;
        ArrayList<Post> targetPosts = new ArrayList<>();
        synchronized (PostFC.lock) {
            try {
                allPosts = PostFC.records();
            } catch (Exception ignore) {
                return new ArrayList<>();
            }
        }
        for (Post post : allPosts.reversed()){
            if (post.type != 1) continue;
            if (!post.id.equals(rootPostId)) continue;
            targetPosts.add(post);
        }
        return targetPosts;
    }
    public ArrayList<Post> getRootMessages(int[] range){
        ArrayList<Post> allPosts;
        ArrayList<Post> targetPosts = new ArrayList<>();
        synchronized (PostFC.lock) {
            try {
                allPosts = PostFC.records();
            } catch (Exception ignore) {
                return new ArrayList<>();
            }
        }
        int i = 0;
        for (Post post : allPosts.reversed()){
            if (range[0] > i) continue;
            else if (range[1] < i+1) break;
            else if (post.type != 0) continue;
            else if (Math.abs(TimeUtils.getNowUnixTime() - post.sendUnixTime)
                    > IgnoreMessageApplication.properties.getPropertyInt("maxChatTimeOutHour")* 3600L
                && getReplyMessages(post.id).size() > IgnoreMessageApplication.properties.getPropertyInt("minResponsesChat")
            ) continue;
            targetPosts.add(post);
            i++;
        }
        return targetPosts;
    }
    public ArrayList<RootAndReplyPost> getRootAndRepliesPosts(int[] range){
        ArrayList<RootAndReplyPost> rootAndRepliesPosts = new ArrayList<>();
        for (Post rootPost : this.getRootMessages(range)){
            rootAndRepliesPosts.add(new RootAndReplyPost(rootPost, this.getReplyMessages(rootPost.id)));
        }
        return rootAndRepliesPosts;
    }
    public RootAndReplyPost getRootAndRepliesPost(String rootPostId){
        Post rootPost = getRootMessage(rootPostId);
        if (rootPost == null) return this.emptyRootAndReplyPost;
        return new RootAndReplyPost(rootPost, getReplyMessages(rootPostId));
    }
    public ArrayList<String> getRootAndRepliesPostSendFromIp(String rootPostId){
        ArrayList<String> ips = new ArrayList<>();
        RootAndReplyPost rootAndReplyPost = this.getRootAndRepliesPost(rootPostId);
        ips.add(rootAndReplyPost.rootPost.ip);
        rootAndReplyPost.replyPosts.forEach(replyPost -> ips.add(replyPost.ip));
        return ips;
    }
    public boolean existsRootMessage(String rootId){
        ArrayList<Post> allPosts;
        AtomicBoolean result = new AtomicBoolean(false);
        synchronized (PostFC.lock) {
            try {
                allPosts = PostFC.records();
            } catch (Exception ignore) {
                return false;
            }
        }
        allPosts.reversed().forEach(post -> {if(post.type == 0 && post.id.equals(rootId)) result.set(true);});
        return result.get();
    }
    private int canAdd(String author, String content) {
        if (author.isEmpty() || author.getBytes().length > 30) return 1;
        else if (content.isEmpty() || content.getBytes().length > 512) return 2;
        return 0;
    }
    private int canAdd(String rootId, String author, String content, String ip) throws IOException {
        if (author.getBytes().length > 30) return 1;
        else if (content.getBytes().length > 512) return 2;
        else if (!existsRootMessage(rootId)) return 3;
        else if (getReplyMessages(rootId).size()+1 > IgnoreMessageApplication.properties.getPropertyInt("maxResponsesChat")) return 4;
        else if (this.getRootAndRepliesPostSendFromIp(rootId).contains(ip)) {return 92;}
        synchronized (BanIpFC.lock) {if (BanIpFC.records().contains(ip)) return 92;}
        return 0;
    }
    public int add(String author, String content, String ip) throws IOException {
        author = StringUtils.replaceEach(author, new String[]{"\n", ","}, new String[]{"", ""});
        content = StringUtils.replaceEach(content, new String[]{"\n", ","}, new String[]{"\\n", "--..--"});
        synchronized (PostFC.lock) {
            int canAddResult = canAdd(author, content);
            if (canAddResult == 0){
                PostFC.append(new Post(UUID.randomUUID().toString(), 0, author, TimeUtils.getNowUnixTime(), content, ip).csvString);
                return 0;
            }
            return canAddResult;
        }
    }
    public int add(String rootId, String author, String content, String ip) throws IOException {
        author = StringUtils.replaceEach(author, new String[]{"\n", ","}, new String[]{"", ""});
        content = StringUtils.replaceEach(content, new String[]{"\n", ","}, new String[]{"\\n", "--..--"});
        synchronized (PostFC.lock) {
            int canAddResult = canAdd(rootId, author, content, ip);
            if (canAddResult == 0){
                PostFC.append(new Post(rootId, 1, author, TimeUtils.getNowUnixTime(), content, ip).csvString);
                return 0;
            }
            return canAddResult;
        }
    }
}
