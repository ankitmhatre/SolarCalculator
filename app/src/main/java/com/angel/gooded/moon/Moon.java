package com.angel.gooded.moon;


import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static com.angel.gooded.moon.AstroFormat.ABOVE_HORIZON;
import static com.angel.gooded.moon.AstroFormat.BELOW_HORIZON;

public class Moon implements Constants {
    /*
     * Example (from United States Naval Observatory): For 1996.11.02 (San
     * Francisco), Moonset 12:33, Moonrise 23:26
     */
    public static final double MOONRISE = Astro.sin(+8.0 / 60.0);

    /**
     * Compute the string corresponding to this Sunrise/Sunset.
     *
     * @return The proper string: null string if no event.
     * @paramhichEvent If there is an event, this will be prepended to the time.
     * @paramriseSetValue the time of sunrise or sunset
     */
    public static String getRiseSetString(double eventValue/* String whichEvent
     * "Moonrise"
     * or
     * "Moonset"
     */
    ) {
        if (Astro.isEvent(eventValue)) {
            return (AstroFormat.hm(eventValue));
        } else {
            return ("null");
        }
    }

    /**
     * Compute the times of sunrise and sunset for a specific date and location.
     *
     * @param timezoneOffset Observer's timezone (msec East of Greenwich)
     * @param latitude       Observer's latitude (North is positive).
     * @param longitude      Observer's longitude (East is positive).
     * @return double[] double[RISE] is the local civil time of sunrise
     * double[SET] is the local civil time of sunset Note the following
     * special result values: ABOVE_HORIZON The moon does not set
     * BELOW_HORIZON The moon does not rise Note that these are
     * independent: there are days each month when the moon rises and does
     * not set (and vice-versa).
     * @paramdate Java date
     */
    public static double[] riseSet(Date javaDate, long timezoneOffset,
                                   double latitude, double longitude) {
        double DATE = Astro.midnightMJD(javaDate);
        double ZONE = ((double) timezoneOffset) / 86400000.0;

        DATE -= ZONE;
        double sinLatitude = Astro.sin(latitude);
        double cosLatitude = Astro.cos(latitude);
        double yMinus = sinAltitude(DATE, 0.0, longitude, cosLatitude, sinLatitude)
                - MOONRISE;
        boolean aboveHorizon = (yMinus > 0.0);
        double rise = BELOW_HORIZON;
        double set = BELOW_HORIZON;
        ;
        if (aboveHorizon) {
            rise = ABOVE_HORIZON;
            set = ABOVE_HORIZON;
        }
        for (double hour = 1.0; hour <= 24.0; hour += 2.0) {
            double yThis = sinAltitude(DATE, hour, longitude, cosLatitude,
                    sinLatitude)
                    - MOONRISE;
            double yPlus = sinAltitude(DATE, hour + 1.0, longitude, cosLatitude,
                    sinLatitude)
                    - MOONRISE;
            /*
             * .________________________________________________________________. |
             * Quadratic interpolation through the three points: | | [-1, yMinus], [0,
             * yThis], [+1, yNext] | | (These must not lie on a straight line.) | |
             * Note: I've in-lined this as it returns several values. |
             * .________________________________________________________________.
             */
            double root1 = 0.0;
            double root2 = 0.0;
            int nRoots = 0;
            double A = (0.5 * (yMinus + yPlus)) - yThis;
            double B = (0.5 * (yPlus - yMinus));
            double C = yThis;
            double xExtreme = -B / (2.0 * A);
            double yExtreme = (A * xExtreme + B) * xExtreme + C;
            double discriminant = (B * B) - 4.0 * A * C;
            if (discriminant >= 0.0) { /* Intersects x-axis? */
                double DX = 0.5 * Math.sqrt(discriminant) / Math.abs(A);
                root1 = xExtreme - DX;
                root2 = xExtreme + DX;
                if (Math.abs(root1) <= +1.0)
                    nRoots++;
                if (Math.abs(root2) <= +1.0)
                    nRoots++;
                if (root1 < -1.0)
                    root1 = root2;
            }
            /*
             * .________________________________________________________________. |
             * Quadratic interpolation result: | | nRoots Number of roots found (0, 1,
             * or 2) | | If nRoots == zero, there is no event in this range. | | root1
             * First root (nRoots >= 1) | | root2 Second root (nRoots == 2) | | yMinus
             * Y-value at interpolation start. If < 0, root1 is | | a moonrise event. | |
             * yExtreme Maximum value of y (nRoots == 2) -- this determines | |
             * whether a 2-root event is a rise-set or a set-rise. |
             * .________________________________________________________________.
             */
            switch (nRoots) {
                case 0: /* No root at this hour */
                    break;
                case 1: /* Found either a rise or a set */
                    if (yMinus < 0.0) {
                        rise = hour + root1;
                    } else {
                        set = hour + root1;
                    }
                    break;
                case 2: /* Found both a rise and a set */
                    if (yExtreme < 0.0) {
                        rise = hour + root2;
                        set = hour + root1;
                    } else {
                        rise = hour + root1;
                        set = hour + root2;
                    }
                    break;
            }
            yMinus = yPlus;
            if (Astro.isEvent(rise) && Astro.isEvent(set))
                break;
        }
        double result[] = new double[2];
        result[0] = rise;
        result[1] = set;
        return (result);
    }

    public static JSONObject riseSetString(double[] riseSet) {
        return (Moon.riseSetString(riseSet[0], riseSet[1]));
    }

    public static JSONObject riseSetString(double moonrise, double moonset) {
        JSONObject json = new JSONObject();

        try {
            if (moonrise <= moonset) {
                json.put("moonrisefirst", true);
            } else {
                json.put("moonrisefirst", true);
            }
            json.put("moonrise", getRiseSetString(moonrise));
            json.put("moonset", getRiseSetString(moonset));


        } catch (JSONException e) {
            e.printStackTrace();

        }

        return json;


    }

    /**
     * Compute the time of moonrise and moonset for a specific time and location.
     *
     * @param date           Java date
     * @param timezoneOffset Observer's timezone (msec East of Greenwich)
     * @param latitude       Observer's latitude (North is positive).
     * @param longitude      Observer's longitude (East is positive).
     * @return a text string with the times. Note: this is a little messy - the
     * problem is the moon takes slightly more than 24 hours to orbit the
     * earth, hence there are days that have no moonrise or moonset. The
     * U.S. Naval Observatory web page displays times for tomorrow or
     * yesterday to fill in the blanks.
     */
    public static JSONObject riseSetString(Date date, long timezoneOffset,
                                       double latitude, double longitude) {
        double[] riseSet = Moon.riseSet(date, timezoneOffset, latitude, longitude);
        return (Moon.riseSetString(riseSet));
    }

    /**
     * Compute the sine of the altitude of the object for this date, hour, and
     * location. cosLatitude and sinLatitude pre-compute the observer's location.
     *
     * @param MJD0        Modified Julian Date at midnight
     * @param hour        Hour past midnight.
     * @param longitude   Observer's longitude, East is positive.
     * @param cosLatitude Cosine(observer's latitude)
     * @param sinLatitude Sine(observer's latitude)
     * @result The sine of the object's altitude above the horizon.
     * <p>
     * Note: this overrides Sun.sinAltitude and contains the moon orbital
     * computation.
     */
    protected static double sinAltitude(double MJD0, double hour,
                                        double longitude, double cosLatitude, double sinLatitude) {
        double MJD = MJD0 + (hour / 24.0);
        double[] moon = Astro.lunarEphemeris(MJD);
        double TAU = 15.0 * (Astro.LMST(MJD, longitude) - moon[Astro.RA]);
        double result = sinLatitude * Astro.sin(moon[Astro.DEC]) + cosLatitude
                * Astro.cos(moon[Astro.DEC]) * Astro.cos(TAU);
        return (result);
    }
}