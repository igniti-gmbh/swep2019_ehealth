package uni.jena.swep.ehealth.data_visualisation;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class XAxisFormatterDayHours extends ValueFormatter {
    public XAxisFormatterDayHours() {
        super();
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        Date date = new Date(((long) value*1000));
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        return sdf.format(date);
    }
}
