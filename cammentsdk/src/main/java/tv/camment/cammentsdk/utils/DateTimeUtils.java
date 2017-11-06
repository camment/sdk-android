package tv.camment.cammentsdk.utils;

import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeUtils {

    private static final SimpleDateFormat ISO_DATE_FORMAT;
    private static final SimpleDateFormat TIME_ONLY_FORMAT;
    private static final SimpleDateFormat FULL_INFO_FORMAT;

    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        TIME_ONLY_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
        FULL_INFO_FORMAT = new SimpleDateFormat("EEEE, MMM d, HH:mm", Locale.US);
    }

    public static synchronized long getCurrentUTCTimestamp() {
        return getCurrentUTCCalendar().getTimeInMillis();
    }

    private static synchronized Calendar getCurrentUTCCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar;
    }

    public static synchronized Calendar getCalendarForTimeZone(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar;
    }

    public static synchronized String getTimeOnlyStringForUI(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTimeInMillis(timestamp);
        return TIME_ONLY_FORMAT.format(calendar.getTime());
    }

    public static synchronized String showStartStringForUI(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTimeInMillis(timestamp);

        return "Watch on " + FULL_INFO_FORMAT.format(calendar.getTimeInMillis());
    }

    public static synchronized long getTimestampFromIsoDateString(String isoString) {
        if (TextUtils.isEmpty(isoString)) {
            return -1;
        }

        try {
            final Date date = ISO_DATE_FORMAT.parse(isoString);
            return date.getTime();
        } catch (ParseException e) {
            Log.e("Utils", "isoDate " + isoString, e);
        }
        return -1;
    }

}
