package si.f5.stsaria.ignoreMessage.SpringControllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import si.f5.stsaria.ignoreMessage.Recorders.PostR;
import si.f5.stsaria.ignoreMessage.Records.RootAndReplyPost;

import java.util.ArrayList;

@Controller
public class MessageFormController {
    @RequestMapping(path = "/", method=RequestMethod.GET)
    public ModelAndView index(@CookieValue(name = "result", defaultValue = "", required = false) String resultC, @RequestParam(value = "result", defaultValue = "92", required = false) String result, ModelAndView mav, HttpServletResponse hsr) {
        mav.setViewName("redirect:/");
        try {Integer.valueOf(result);} catch (NumberFormatException ignore) {result = "92";}
        if (!result.equals("92")){
            hsr.addCookie(new Cookie("result", result));
            return mav;
        }
        mav.setViewName("index");
        hsr.addCookie(new Cookie("result", "92"));
        result = resultC;
        PostR postR = new PostR();
        ArrayList<RootAndReplyPost> rootAndReplyPosts = postR.getRootAndRepliesPosts(new int[]{0, 25});
        mav.addObject("rootAndRepliesPosts", rootAndReplyPosts);
        mav.addObject("result", Integer.valueOf(result));
        return mav;
    }
    @RequestMapping(path = "/root", method=RequestMethod.POST)
    public String postRoot(@RequestParam(value = "author") String author, @RequestParam(value = "content") String content, HttpServletRequest request){
        String ip;
        String xForwardedFor =  request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) ip = xForwardedFor;
        else ip = request.getRemoteAddr();
        int result;
        try {
            result = new PostR().add(author, content, ip);
        } catch (Exception ignore) {result = -1;}
        return "redirect:/?result="+result;
    }
    @RequestMapping(path = "/reply", method=RequestMethod.POST)
    public String postReply(@RequestParam(value = "id") String rootId, HttpServletRequest request, @RequestParam(value = "author") String author, @RequestParam(value = "content") String content){
        String ip;
        String xForwardedFor =  request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) ip = xForwardedFor;
        else ip = request.getRemoteAddr();
        int result;
        try {
            result = new PostR().add(rootId, author, content, ip);
        } catch (Exception ignore) {result = -1;}
        return "redirect:/?result="+result;
    }
}
