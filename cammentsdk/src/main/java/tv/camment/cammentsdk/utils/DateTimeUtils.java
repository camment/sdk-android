package tv.camment.cammentsdk.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeUtils {

    private static final SimpleDateFormat ISO_DATE_FORMAT;
    private static final SimpleDateFormat ISO_DOB_FORMAT;
    private static final SimpleDateFormat CASE_DATE_FORMAT;
    private static final SimpleDateFormat DOB_DATE_FORMAT;
    private static final SimpleDateFormat MEASUREMENTS_FRAG_FORMAT;
    private static final SimpleDateFormat TIME_ONLY_FORMAT;

    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        ISO_DOB_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        ISO_DOB_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        CASE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        CASE_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        DOB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        DOB_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        MEASUREMENTS_FRAG_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US);
        MEASUREMENTS_FRAG_FORMAT.setTimeZone(TimeZone.getDefault());
        TIME_ONLY_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
        MEASUREMENTS_FRAG_FORMAT.setTimeZone(TimeZone.getDefault());
    }

    public static synchronized long getCurrentUTCTimestamp() {
        return getCurrentUTCCalendar().getTimeInMillis();
    }

    public static synchronized Calendar getCurrentUTCCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar;
    }

}
