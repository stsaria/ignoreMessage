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
import java.util.List;
import java.util.UUID;

public class PostR {
    private final Post emptyRootPost = new Post("", 0, "", 0, "", "");
    private final RootAndReplyPost emptyRootAndReplyPost = new RootAndReplyPost(emptyRootPost, new ArrayList<>());
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
    public RootAndReplyPost getRootAndRepliesPost(String rootPostId){
        Post rootPost = getRootMessage(rootPostId);
        if (rootPost == null) return new RootAndReplyPost(null, null);
        return new RootAndReplyPost(rootPost, getReplyMessages(rootPostId));
    }
    public boolean existsRootMessage(String rootId){
        ArrayList<Post> allPosts;
        synchronized (PostFC.lock) {
            try {
                allPosts = PostFC.records();
            } catch (Exception ignore) {
                return false;
            }
        }
        for (Post post : allPosts.reversed()){
            if (post.type != 0) continue;
            if (post.id.equals(rootId)) return true;
        }
        return false;
    }
    private int canAdd(String author, String content, String ip) throws IOException {
        if (author.isEmpty() || author.getBytes().length > 30) return 1;
        else if (content.isEmpty() || content.getBytes().length > 512) return 2;
        return 0;
    }
    private int canAdd(String rootId, String author, String content, String ip) throws IOException {
        if (author.getBytes().length > 30) return 1;
        else if (content.getBytes().length > 512) return 2;
        else if (!existsRootMessage(rootId)) return 3;
        else if (getReplyMessages(rootId).size()+1 > IgnoreMessageApplication.properties.getPropertyInt("manResponsesChat")) return 4;
        synchronized (BanIpFC.lock) {if (BanIpFC.records().contains(ip)) return 5;}
        return 0;
    }
    public int add(String author, String content, String ip) throws IOException {
        author = StringUtils.replaceEach(author, new String[]{"\n", ","}, new String[]{"", ""});
        content = StringUtils.replaceEach(content, new String[]{"\n", ","}, new String[]{"\\n", "--..--"});
        synchronized (PostFC.lock) {
            int canAddResult = canAdd(author, content, ip);
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
