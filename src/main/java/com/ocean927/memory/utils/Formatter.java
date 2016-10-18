/*
 * 
 */
package com.ocean927.memory.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.ocean927.memory.impl.MemoryManagerException;

/**
 * The Class Formatter.
 */
public class Formatter {

	/**
	 * prevent users instantiating this class.
	 */	
	private Formatter() {
		
	}
	/**
	 * Fl.
	 *
	 * @param l the l
	 * @return the string
	 */
	public static String fl(long l) {
		return formatLongWithThousands(l);
	}
	
	/**
	 * Dbl.
	 *
	 * @param d the d
	 * @return the string
	 */
	public static String dbl(double d) {
		return df2.format(d);
	}

	/**
	 * Format long with thousands.
	 *
	 * @param l the l
	 * @return the string
	 */
	public static String formatLongWithThousands(long l) {
		String res = "";
		boolean minus = false;
		if (l < 0) {
			minus = true;
			l = l * (-1);
		}
		// start
		byte b[] = Long.toString(l).getBytes();
		int index = 0;
		for (int i = b.length - 1; i >= 0; i--) {
			index++;
			if (index == 4) {
				index = 1;
				res = (char) b[i] + "," + res;
			} else {
				res = (char) b[i] + res;
			}
		}
		// minus
		if (minus == true) {
			res = "-" + res;
		}
		return res;
	}

	/**
	 * Pad in front.
	 *
	 * @param input the input
	 * @param width the width
	 * @param pad the pad
	 * @return the string
	 */
	// different type of split, take N characeters
	public static String padInFront(String input, int width, String pad) {
		StringBuilder sb = new StringBuilder();
		if (input != null && pad != null) {
			int pad_n = width - input.length();
			if (pad_n > 0) {
				for (int i = 1; i <= pad_n; i++) {
					sb.append(pad);
				}
			}
			//
			sb.append(input);
		}
		return sb.toString();
	}

	/**
	 * Gets the date F 1.
	 *
	 * @return the date F 1
	 */
	public final static String getDateF1() {
		String res = null;
		synchronized (formatter1) {
			res = formatter1.format(new java.util.Date());
		}
		return res;
	}

	/**
	 * R dbl.
	 *
	 * @param value the value
	 * @param places the places
	 * @return the double
	 */
	public static double rDbl(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * Parses the memory.
	 *
	 * @param value the value
	 * @return the long
	 * @throws MemoryManagerException the memory manager exception
	 */
	public static long parseMemory(String value) throws MemoryManagerException {
		long b = 0;
		value = trim(value);
		if (value.endsWith("KB") || value.endsWith("Kb") || value.endsWith("kb")) {
			value = value.substring(0, value.length() - 2);
			value = trim(value);
			b = Long.parseLong(value);
			b = b * 1024l;
		} else if (value.endsWith("MB") || value.endsWith("Mb") || value.endsWith("mb") || value.endsWith("m")
				|| value.endsWith("M")) {
			value = value.substring(0, value.length() - 2);
			value = trim(value);
			b = Long.parseLong(value);
			b = b * 1024 * 1024l;
		} else if (value.endsWith("GB") || value.endsWith("Gb") || value.endsWith("gb") || value.endsWith("g")
				|| value.endsWith("G")) {
			value = value.substring(0, value.length() - 2);
			value = trim(value);
			b = Long.parseLong(value);
			b = b * 1024 * 1024 * 1024l;
		} else {
			b = Long.parseLong(value);
		}
		return b;
	}

	/**
	 * Trim.
	 *
	 * @param in the in
	 * @return the string
	 */
	public static String trim(String in) {
		if (in != null) {
			String out = in.replaceAll("^\\s+|\\s+$", "");
			return out;
		} else {
			return null;
		}
	}

	/**
	 * Gets the diff nanoseconds.
	 *
	 * @param end the end
	 * @param beg the beg
	 * @return the diff nanoseconds
	 */
	public final static String getDiffNanoseconds(long end, long beg) {
		return getTimeDifferenceNanoseconds((new StringBuilder()), end, beg);
	}

	/**
	 * Gets the time difference nanoseconds.
	 *
	 * @param uptime_sb the uptime sb
	 * @param this_time the this time
	 * @param from_this_time the from this time
	 * @return the time difference nanoseconds
	 */
	public final static String getTimeDifferenceNanoseconds(StringBuilder uptime_sb, long this_time,
			long from_this_time) {
		return getTimeDifferenceNanoseconds(uptime_sb, this_time - from_this_time);
	}

	/**
	 * Gets the time difference.
	 *
	 * @param uptime_sb the uptime sb
	 * @param time_l the time l
	 * @return the time difference
	 */
	public final static String getTimeDifference(StringBuilder uptime_sb, long time_l) {
		if (uptime_sb == null) {
			uptime_sb = new StringBuilder();
		}
		long rmd = 0, days = 0, hh = 0, min = 0, sec = 0;
		int mmm = 0;
		boolean flagNegative = false;
		if (time_l < 0) {
			flagNegative = true;
			time_l = (-1) * time_l;
		}
		// HOURS
		rmd = (long) time_l % MILLISECONDS_IN_ONE_DAY;
		if (rmd > 0) {
			days = (int) ((double) time_l / (double) MILLISECONDS_IN_ONE_DAY);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= MILLISECONDS_IN_ONE_DAY) {
			days = (int) ((double) time_l / (double) MILLISECONDS_IN_ONE_DAY);
			time_l = 0;
		}
		// MINUTES
		rmd = (long) time_l % MILLISECONDS_IN_ONE_HOUR;
		if (rmd > 0) {
			hh = (int) ((double) time_l / (double) MILLISECONDS_IN_ONE_HOUR);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= MILLISECONDS_IN_ONE_HOUR) {
			hh = (int) ((double) time_l / (double) MILLISECONDS_IN_ONE_HOUR);
			time_l = 0;
		}
		// SECONDS
		rmd = (long) time_l % MILLISECONDS_IN_ONE_MINUTE;
		if (rmd > 0) {
			min = (int) ((double) time_l / (double) MILLISECONDS_IN_ONE_MINUTE);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= MILLISECONDS_IN_ONE_MINUTE) {
			min = (int) ((double) time_l / (double) MILLISECONDS_IN_ONE_MINUTE);
			time_l = 0;
		}
		// MILLISECONDS
		rmd = (long) time_l % MILLISECONDS_IN_ONE_SECOND;
		if (rmd > 0) {
			sec = (int) ((double) time_l / (double) MILLISECONDS_IN_ONE_SECOND);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= MILLISECONDS_IN_ONE_SECOND) {
			sec = (int) ((double) time_l / (double) MILLISECONDS_IN_ONE_SECOND);
			time_l = 0;
		}
		// remainder
		mmm = (int) (time_l);
		//
		if (days > 0) {
			uptime_sb.append(padNumber((int) days, 2) + " days & ");
			uptime_sb.append(padNumber((int) hh, 2) + ":");
			uptime_sb.append(padNumber((int) min, 2) + ":");
			uptime_sb.append(padNumber((int) sec, 2) + ".");
			uptime_sb.append(padNumber(mmm, 3));
		} else {
			uptime_sb.append(padNumber((int) hh, 2) + ":");
			uptime_sb.append(padNumber((int) min, 2) + ":");
			uptime_sb.append(padNumber((int) sec, 2) + ".");
			uptime_sb.append(padNumber(mmm, 3));
		}
		if (flagNegative) {
			uptime_sb.insert(0, "-");
		}
		return uptime_sb.toString();
	}
	
	/**
	 * Gets the diff.
	 *
	 * @param end the end
	 * @param beg the beg
	 * @return the diff
	 */
	public final static String getDiff(long end, long beg) {
		return getTimeDifference( (new StringBuilder()), end-beg );
	}

	/**
	 * Gets the diff.
	 *
	 * @param delta the delta
	 * @return the diff
	 */
	public final static String getDiff(long delta) {
		return getTimeDifference((new StringBuilder()), delta);
	}

	/**
	 * Gets the diff nanoseconds.
	 *
	 * @param time_l the time l
	 * @return the diff nanoseconds
	 */
	public final static String getDiffNanoseconds(long time_l) {
		return getTimeDifferenceNanoseconds(new StringBuilder(), time_l);
	}

	/**
	 * Gets the time difference nanoseconds.
	 *
	 * @param uptime_sb the uptime sb
	 * @param time_l the time l
	 * @return the time difference nanoseconds
	 */
	public final static String getTimeDifferenceNanoseconds(StringBuilder uptime_sb, long time_l) {
		if (uptime_sb == null) {
			uptime_sb = new StringBuilder();
		}
		long rmd = 0, days = 0, hh = 0, min = 0, sec = 0;
		int milliseconds = 0;
		int microseconds = 0;
		int nanos = 0;
		boolean flagNegative = false;
		if (time_l < 0) {
			flagNegative = true;
			time_l = (-1) * time_l;
		}
		// DAYS
		rmd = (long) time_l % NANOS_IN_ONE_DAY;
		if (rmd > 0) {
			days = (int) ((double) time_l / (double) NANOS_IN_ONE_DAY);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= NANOS_IN_ONE_DAY) {
			days = (int) ((double) time_l / NANOS_IN_ONE_DAY);
			time_l = 0;
		}
		// HOURS
		rmd = (long) time_l % NANOS_IN_ONE_HOUR;
		if (rmd > 0) {
			hh = (int) ((double) time_l / (double) NANOS_IN_ONE_HOUR);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= NANOS_IN_ONE_HOUR) {
			hh = (int) ((double) time_l / (double) NANOS_IN_ONE_HOUR);
			time_l = 0;
		}
		// MINUTEs
		rmd = (long) time_l % NANOS_IN_ONE_MINUTE;
		if (rmd > 0) {
			min = (int) ((double) time_l / (double) NANOS_IN_ONE_MINUTE);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= NANOS_IN_ONE_MINUTE) {
			min = (int) ((double) time_l / (double) NANOS_IN_ONE_MINUTE);
			time_l = 0;
		}
		// SECONDs
		rmd = (long) time_l % NANOS_IN_ONE_SECOND;
		if (rmd > 0) {
			sec = (int) ((double) time_l / (double) NANOS_IN_ONE_SECOND);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= NANOS_IN_ONE_SECOND) {
			sec = (int) ((double) time_l / (double) NANOS_IN_ONE_SECOND);
			time_l = 0;
		}
		// MILLI-SECONDs
		rmd = (long) time_l % NANOS_IN_ONE_MILLISECOND;
		if (rmd > 0) {
			milliseconds = (int) ((double) time_l / (double) NANOS_IN_ONE_MILLISECOND);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= NANOS_IN_ONE_MILLISECOND) {
			milliseconds = (int) ((double) time_l / (double) NANOS_IN_ONE_MILLISECOND);
			time_l = 0;
		}
		// MICRO-SECONDs
		rmd = (long) time_l % NANOS_IN_ONE_MICROSECOND;
		if (rmd > 0) {
			microseconds = (int) ((double) time_l / (double) NANOS_IN_ONE_MICROSECOND);
			time_l = rmd;
		} else if (rmd == 0 && time_l >= NANOS_IN_ONE_MICROSECOND) {
			microseconds = (int) ((double) time_l / (double) NANOS_IN_ONE_MICROSECOND);
			time_l = 0;
		}
		// remainder
		nanos = (int) time_l;
		if (days > 0) {
			uptime_sb.append(padNumber((int) days, 2) + " days & ");
			uptime_sb.append(padNumber((int) hh, 2) + ":");
			uptime_sb.append(padNumber((int) min, 2) + ":");
			uptime_sb.append(padNumber((int) sec, 2) + ".");
			uptime_sb.append(padNumber((int) milliseconds, 3) + ".");
			uptime_sb.append(padNumber((int) microseconds, 3) + ".");
			uptime_sb.append(padNumber((int) nanos, 3));
		} else {
			uptime_sb.append(padNumber((int) hh, 2) + ":");
			uptime_sb.append(padNumber((int) min, 2) + ":");
			uptime_sb.append(padNumber((int) sec, 2) + ".");
			uptime_sb.append(padNumber((int) milliseconds, 3) + ".");
			uptime_sb.append(padNumber((int) microseconds, 3) + ".");
			uptime_sb.append(padNumber((int) nanos, 3));
		}
		if (flagNegative) {
			uptime_sb.insert(0, "-");
		}
		return uptime_sb.toString();
	}

	/**
	 * Pad number.
	 *
	 * @param number the number
	 * @param how_many_digits the how many digits
	 * @return the string
	 */
	public static String padNumber(int number, int how_many_digits) {
		String res = null;
		String str = Integer.toString(number);
		if ((how_many_digits - str.length()) >= 0) {
			res = "";
			for (int i = 0; i < how_many_digits - str.length(); i++) {
				res += "0";
			} // end for
			res += str;
		} else {
			res = str;
		}
		str = null;
		return res;
	}

	/** The Constant NANOS_IN_ONE_MICROSECOND. */
	public final static long NANOS_IN_ONE_MICROSECOND = 1000;
	
	/** The Constant NANOS_IN_ONE_MILLISECOND. */
	public final static long NANOS_IN_ONE_MILLISECOND = 1000 * NANOS_IN_ONE_MICROSECOND;
	
	/** The Constant NANOS_IN_ONE_SECOND. */
	public final static long NANOS_IN_ONE_SECOND = 1000 * NANOS_IN_ONE_MILLISECOND;
	
	/** The Constant NANOS_IN_ONE_MINUTE. */
	public final static long NANOS_IN_ONE_MINUTE = 60 * NANOS_IN_ONE_SECOND;
	
	/** The Constant NANOS_IN_ONE_HOUR. */
	public final static long NANOS_IN_ONE_HOUR = 60 * NANOS_IN_ONE_MINUTE;
	
	/** The Constant NANOS_IN_ONE_DAY. */
	public final static long NANOS_IN_ONE_DAY = 24 * NANOS_IN_ONE_HOUR;
	
	/** The Constant MILLISECONDS_IN_ONE_DAY. */
	//
	public final static int MILLISECONDS_IN_ONE_DAY = 1000 * 60 * 60 * 24;
	
	/** The Constant MILLISECONDS_IN_ONE_HOUR. */
	public final static int MILLISECONDS_IN_ONE_HOUR = 1000 * 60 * 60;
	
	/** The Constant MILLISECONDS_IN_ONE_MINUTE. */
	public final static int MILLISECONDS_IN_ONE_MINUTE = 1000 * 60;
	
	/** The Constant MILLISECONDS_IN_ONE_SECOND. */
	public final static int MILLISECONDS_IN_ONE_SECOND = 1000;
	
	/** The Constant MICROSECONDS_IN_ONE_SECOND. */
	public final static int MICROSECONDS_IN_ONE_SECOND = 1000 * 1000;
	
	/** The Constant NANOSECONDS_IN_ONE_SECOND. */
	public final static int NANOSECONDS_IN_ONE_SECOND = 1000 * 1000 * 1000;
	
	/** The Constant NANOSECONDS_IN_ONE_MICROSECOND. */
	public final static int NANOSECONDS_IN_ONE_MICROSECOND = 1000;
		
	/** The df 2. */
	private static DecimalFormat df2 = new DecimalFormat(".##");
	
	/** The Constant formatter1. */
	private final static SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

}
