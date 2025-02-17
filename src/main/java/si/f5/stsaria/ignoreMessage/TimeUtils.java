package si.f5.stsaria.ignoreMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtils {
    public static long getNowUnixTime(){
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
