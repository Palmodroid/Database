package digitalgarden.mecsek.database.pills;


import android.content.ContentValues;
import android.database.Cursor;

import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GenericTable;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;

public final class PillsTable extends GenericTable
    {
    public PillsTable( int tableId )
        {
        super( tableId );
        }

    @Override
    public String name()
        {
        return "pills";
        }

    public static int NAME;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn("name", "TEXT");
        SEARCH = addSearchColumnFor( NAME );
        }

    @Override
    public void defineExportImportColumns()
        {
        addExportImportColumn( PillsTable.NAME );
        }

    @Override
    public void importRow(String[] records)
        {
        // Mivel csak egy adat van, hossz ellenőrzése nem szükséges

        records[1] = StringUtils.revertFromEscaped( records[1] );

        // Uniqe ellenőrzés kódból. Lehetne adatbázis szinten is, hiba ellenőrzésével
        String[] projection = {
                column(PillsTable.NAME) };
        Cursor cursor = getContentResolver()
                .query( table(PILLS).contentUri(), projection,
                        column(PillsTable.NAME) + "='" + records[1] + "'", null, null);

        // http://stackoverflow.com/a/16108435
        if (cursor == null || cursor.getCount() == 0)
            {
            ContentValues values = new ContentValues();
            values.put( column(PillsTable.NAME), records[1]);

            getContentResolver()
                    .insert( table(PILLS).contentUri(), values);
            Scribe.debug( "Pill [" + records[1] + "] was inserted.");
            }
        else
            Scribe.note( "Pill [" + records[1] + "] already exists! Item was skipped.");

        if ( cursor != null )
            cursor.close();
        }

    }
