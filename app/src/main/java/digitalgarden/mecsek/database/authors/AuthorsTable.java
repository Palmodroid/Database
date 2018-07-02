package digitalgarden.mecsek.database.authors;


import digitalgarden.mecsek.generic.database.GenericTable;

public final class AuthorsTable extends GenericTable
    {
    public AuthorsTable( int tableId )
        {
        super( tableId );
        }

    @Override
    public String name()
        {
        return "authors";
        }

    public static int NAME;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addUniqueColumn("name", "TEXT");
        SEARCH = addSearchColumnFor(NAME);
        }

    @Override
    public void defineExportImportColumns()
        {
        addExportImportColumn( AuthorsTable.NAME );
        }

    /*
    @Override
    public void importRow(String[] records)
        {
        // Mivel csak egy adat van, hossz ellenőrzése nem szükséges

        records[1] = StringUtils.revertFromEscaped( records[1] );

        // Uniqe ellenőrzés kódból. Lehetne adatbázis szinten is, hiba ellenőrzésével
        String[] projection = {
                column(AuthorsTable.NAME) };
        Cursor cursor = getContentResolver()
                .query( table(AUTHORS).contentUri(), projection,
                        column(AuthorsTable.NAME) + "='" + records[1] + "'", null, null);

        // http://stackoverflow.com/a/16108435
        if (cursor == null || cursor.getCount() == 0)
            {
            ContentValues values = new ContentValues();
            values.put( column(AuthorsTable.NAME), records[1]);

            getContentResolver()
                    .insert( table(AUTHORS).contentUri(), values);
            Scribe.debug( "Author [" + records[1] + "] was inserted.");
            }
        else
            Scribe.note( "Author [" + records[1] + "] already exists! Item was skipped.");

        if ( cursor != null )
            cursor.close();
        }
        */

    }
