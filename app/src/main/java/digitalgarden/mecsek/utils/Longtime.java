package digitalgarden.mecsek.utils;

import java.util.Calendar;

/**
 * Egy időpontot long értéken tárol (sqlite számára)
 * Az időpontot kevert számrendszerben tárolja,
 * - a pontosággal együtt
 * - sorrend megtartásával
 */

public class Longtime
    {                                       // Ha bármelyik megegyezik offsettel (vagy kisebb),
    // Akkor a további részek nem számítanak
    private static final int YEAR = 0;      // 1601 - 2999
    private static final int MONTH = 1;     // 1 - 12 13 - next month
    private static final int DAY = 2;       // 1 - 31 32 - next day

    private static final int DAY_NAME = 3;  // 0 - 6

    private static final int HOUR = 4;      // 0 - 23
    private static final int MIN = 5;       // 0 - 59
    private static final int SEC = 6;       // 0 - 59
    private static final int MILL = 7;      // 0 - 999

    private static final int TIME_PARTS = 8;

    private static String[] dayString = {"Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat", "Vasárnap"};

    private static String[] monthString = {"január", "február", "március", "április", "május", "június",
            "július", "augusztus", "szeptember", "október", "november", "december", "NEXT MONTH"};

    private static final int[] range =
            {1400, 14, 33, 8, 25, 61, 61, 1001};

    // AZ "extra" is benne van a "range"-ben, de ellenőrzéskor nem engedi használni.
    // Ez arra szolgál, hogy megtaláljuk pl. az egy hónaphoz tartozó elmeket, mert így mindig van egy nagyobb

    private static final int[] extra =
            {0, 1, 1, 0, 0, 0, 0, 0};
    private static final int[] start =
            {1600, 0, 0, -1, -1, -1, -1, -1};

    private int[] part = new int[TIME_PARTS];

    /** az idő long formában - 0L, ha nincs megadva időpont */
    private long time = 0L;

    private static int TWO_DIGIT_YEAR_START = Calendar.getInstance().get( Calendar.YEAR ) - 80;

    /** checkParts() ellenőrzése után áll be, true, ha valamelyik időérték invalid */
    private boolean error = false;

    /** String parsing után áll be, true, ha autoimatikus kitöltés történt */
    private boolean auto = false;


    public Longtime() {}

    public Longtime( long time )
        {
        set (time);
        }


    public long get()
        {
        return time;
        }

    public boolean isError()
        {
        return error;
        }

    public boolean isAuto()
        {
        return auto;
        }


    public void set(long time)
        {
        // long esetében nincs ellenőrzés

        this.time = time;
        auto = false;
        convertLong2Parts();
        }


    public boolean set(int... parts)
        {
        int n, size;

        if (parts.length < part.length)
            size = part.length;
        else
            size = parts.length;

        for (n = 0; n < size; n++)
            {
            part[n] = parts[n];
            }

        zeroAbove(n);

        auto = false;
        convertParts2Long();
        return error;
        }


    public void set() // Timestamp
        {
        Calendar now = Calendar.getInstance();

        part[YEAR] = now.get( Calendar.YEAR );
        part[MONTH] = now.get( Calendar.MONTH );
        part[DAY] = now.get( Calendar.DAY_OF_MONTH );

        part[DAY_NAME] = -1;
        // Calendar Vasárnappal kezdődik, nem hétfővel,
        // egyszerűbb az ellenőrzést elvégezni

        part[HOUR] = now.get( Calendar.HOUR_OF_DAY );
        part[MIN] = now.get( Calendar.MINUTE );
        part[SEC] = now.get( Calendar.SECOND );
        part[MILL] = now.get( Calendar.MILLISECOND );

        auto = false;
        convertParts2Long();
        }


    public boolean setDate( String string, int twoDigitYearStart )
        {
        int[] ints = Utils.splitInts( string );
        Calendar now = null;
        auto = false;

        if ( ints.length >= 3 )
            {
            if ( ints[0] < 100 )
                {
                auto = true;
                ints[0] += twoDigitYearStart - twoDigitYearStart % 100;
                if ( ints[0] <= twoDigitYearStart)
                    {
                    ints[0] += 100;
                    }
                }

            part[YEAR] = ints[0];
            part[MONTH] = ints[1];
            part[DAY] = ints[2];
            }
        else if ( ints.length != 0 ) // YEAR, MONTH, DAY mind 0
            {
            auto = true;
            now = Calendar.getInstance();
            part[YEAR] = now.get(Calendar.YEAR);

            if (ints.length == 2)
                {
                part[MONTH] = ints[0];
                part[DAY] = ints[1];
                }
            else
                {
                part[MONTH] = now.get(Calendar.MONTH);

                if (ints.length == 1)
                    {
                    part[DAY] = ints[0];
                    }
                // else // ints.length = 0
                //    {
                //    part[DAY] = now.get(Calendar.DAY_OF_MONTH);
                //    }
                }
            }

        zeroAbove( DAY_NAME );

        convertParts2Long();
        return error || auto;
        }


    public boolean setDate( String string )
        {
        return setDate( string, TWO_DIGIT_YEAR_START );
        }


    private void zeroAbove( int index )
        {
        for (int n = index; n < part.length; n++)
            {
            part[n] = start[n];
            }
        }


    private void convertParts2Long()
        {
        checkParts();

        time = 0L;
        for ( int n = 0; n < TIME_PARTS; n++ )
            {
            time *= range[n];
            time += (part[n] - start[n]);

            // Scribe.debug("Time: " + time);
            }
        }


    private void convertLong2Parts()
        {
        long time = this.time;

        for ( int n = TIME_PARTS -1 ; n >= 0 ; n-- )
            {
            part[n] = (int)(time % range[n]) + start[n];
            time /= range[n];

            // Scribe.debug("Time: " + time );
            }
        error = false; // megegyezés alapján nincs check
        }

    /**
     * Ellenőrzi, hogy az idő részeinek érvényességét.
     * A hónap ill. napvégét jelentő értékeket nem fogadja el, csak a valós idő-értékeket.
     * A nap nevét beállítja.
     * Ha hibát talál, akkor a hibás értékre csökkenti a pontosságot, és az 'error' flag-et
     * beállítja.
     * convertParts2Long() mindig meghívja!
     * convertLong2Parts() esetén - megegyezés szerint - nem kell meghívni.
     * @return true, ha nem talált hibát, false, ha az időérték hibás volt, és javítani kellett
     */
    private boolean checkParts()
        {
        error = false;
        boolean zero = false;

        for ( int n = 0; n < TIME_PARTS; n++ )
            {
            if (zero)
                {
                if (part[n] > start[n])
                    {
                    error = true;
                    }
                part[n] = start[n];
                }
            else // non-zero
                {
                if (n == DAY_NAME) // mire ide értünk, year, month és day már ellenőrzött
                    {
                    part[n] = dayName();
                    }
                else if (part[n] <= start[n])  // most válik zero-vá, ez nem hiba
                    {
                    zero = true;
                    part[n] = start[n];
                    }
                else if ((n == DAY && part[n] > lengthOfMonth())
                        || part[n] > range[n] - extra[n] + start[n] - 1) // túl nagy
                    {
                    error = true;
                    zero = true;
                    part[n] = start[n];
                    }
                }
            }

        return error;
        }


    // 4-gyel osztható, kivéve, ha 100-zal osztható, de mégis, ha 400-zal osztható
    private boolean isLeapYear()
        {
        return part[YEAR] % 400 == 0 || part[YEAR] % 100 != 0 && part[YEAR] % 4 == 0;
        }


    private int lengthOfMonth()
        {
        if ( part[MONTH] == 2 )
            return isLeapYear() ? 29 : 28;
        return part[MONTH] <= 7 ? 30 + part[MONTH] % 2 : 31 - part[MONTH] % 2;
        }


    private int daysSinceEpoch()
        {
        // Epoch 1601.01.01 - Csak 400-zal osztható év utáni lehet !!
        // És ez pont hétfő! 2001 is.
        // Epoch előtt nem tud számolni!!!

        int year = part[YEAR];
        int month = part[MONTH];
        int day = part[DAY];

        if ( month > 2 )
            day -= isLeapYear() ? 1 : 2;

        day += month * 30; // A mostani hónapot vegyük majd ki!

        if ( month >= 8 ) month++;

        //  1  2  3  4  5  6  7  8  9 10 11 12 month
        //  0  1  1  2  2  3  3  4  5  5  6  6

        //  1  2  3  4  5  6  7  x  9 10 11 12 13month
        //  0  1  1  2  2  3  3  4  4  5  5  6  6

        day += month / 2;

        year -= 1601;

        day += year * 365 + year / 4 - year / 100 + year / 400;

        return day - 31;
        }


    private int dayName()
        {
        return daysSinceEpoch() % 7;
        }


    @Override
    public String toString()
        {
        return toString( true );
        }

    public String toString( boolean isTextEnabled )
        {
        StringBuilder builder = new StringBuilder( 24 );

        if ( part[YEAR] > start[YEAR] )
            {
            builder.append(part[YEAR]).append('.');

            if (part[MONTH] > start[MONTH])
                {
                builder.append(Integer.toString(part[MONTH] + 100).substring(1)).append('.');

                if ( isTextEnabled )
                    {
                    builder.append(" (").append( monthString[part[MONTH]-1]).append(") ");
                    }

                if (part[DAY] > start[DAY])
                    {
                    builder.append(Integer.toString(part[DAY] + 100).substring(1)).append('.');

                    if (isTextEnabled && part[DAY_NAME] > start[DAY_NAME])
                        {
                        builder.append(' ').append(dayString[part[DAY_NAME]]);
                        }

                    if (part[HOUR] > start[HOUR])
                        {
                        builder.append(' ').append(Integer.toString(part[HOUR] + 100).substring(1));

                        if (part[MIN] > start[MIN])
                            {
                            builder.append(':').append(Integer.toString(part[MIN] + 100).substring(1));

                            if (part[SEC] > start[SEC])
                                {
                                builder.append(':').append(Integer.toString(part[SEC] + 100).substring(1));

                                if (part[MILL] > start[MILL])
                                    {
                                    builder.append('.').append(Integer.toString(part[MILL] + 1000).substring(1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return builder.toString();
        }
    }
