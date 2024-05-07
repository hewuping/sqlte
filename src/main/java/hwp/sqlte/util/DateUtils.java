package hwp.sqlte.util;

import hwp.sqlte.Config;

import java.time.ZoneOffset;
import java.util.TimeZone;

public class DateUtils {

    public static ZoneOffset toZoneOffset(TimeZone timeZone) {
        int rawOffset = timeZone.getRawOffset();
        int secondsOffset = rawOffset / 1000;
        return ZoneOffset.ofTotalSeconds(secondsOffset);
    }

}
