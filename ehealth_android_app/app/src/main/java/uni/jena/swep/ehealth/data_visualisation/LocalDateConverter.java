package uni.jena.swep.ehealth.data_visualisation;

import androidx.room.TypeConverter;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.Date;

public class LocalDateConverter {
    @TypeConverter
    public static LocalDate fromTimestamp(Long value) {
        return value == null ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()).toLocalDate();
    }

    @TypeConverter
    // TODO use something else than date
    public static Long dateToTimestamp(LocalDate date) {
        return date == null ? null : date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
