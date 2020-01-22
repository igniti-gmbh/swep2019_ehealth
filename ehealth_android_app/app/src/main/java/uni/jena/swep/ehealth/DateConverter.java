package uni.jena.swep.ehealth;

import android.util.Log;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.util.Date;

import java.text.SimpleDateFormat;

public class DateConverter {

    @TypeConverter
    public static Date toDate(String dateString) {
        if (dateString == null) {
            return null;
        } else {
            try {
                return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).parse(dateString);
            }
            catch (ParseException e) {
                // TODO handle exception
                Log.v("db", "parseerror by date object: " + dateString);
                return null;
            }
        }
    }

    @TypeConverter
    public static String toDateString(Date date) {
        if (date == null) {
            return null;
        } else {
           return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(date);
        }
    }
}