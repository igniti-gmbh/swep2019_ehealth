package uni.jena.swep.ehealth.data_visualisation;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class XAxisFormatterDay extends ValueFormatter {
    /**
     * This class formats system time to date time (hh:mm).
     * Only usable for values on the same day.
     */
    private long reference_timestamp;

    public XAxisFormatterDay(long reference_timestamp) {
        super();
        this.reference_timestamp = reference_timestamp;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        //return new Date(((long) value) + reference_timestamp).toString();
        Date date = new Date(((long) value) + reference_timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        return sdf.format(date);
    }
}
