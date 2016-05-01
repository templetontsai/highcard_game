import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ProcessTimeStamp {
	public synchronized static long getProcessTimestamp()
	{
		return new Date().getTime();
	}
}
