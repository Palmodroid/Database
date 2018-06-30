package digitalgarden.mecsek.database.books;

import android.content.ContentValues;

import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GenericTable;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.BOOKS;


// http://martin.cubeactive.com/android-using-joins-with-a-provider-sqlite/
public final class BooksTable extends GenericTable
    {
    public BooksTable( int tableId )
        {
        super( tableId );
        }

    @Override
    public String name()
        {
        return "books";
        }

    public static int TITLE;
    public static int AUTHOR_ID;
    public static int NOTE;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        TITLE = addColumn("title", "TEXT");
        NOTE = addColumn("note", "TEXT");
        SEARCH = addSearchColumnFor(TITLE);
        AUTHOR_ID = addForeignKey("author_id", AUTHORS);
        }

    @Override
    public void defineExportImportColumns()
        {
        addExportImportColumn( AuthorsTable.NAME );
        addExportImportColumn( BooksTable.TITLE );
        }

   @Override
    public void importRow(String[] records)
        {
        // Két adat miatt itt szükséges a hossz ellenőrzése
        if ( records.length < 3 )
            {
            Scribe.note( "Parameters missing from BOOKS row. Item was skipped.");
            return;
            }

        ContentValues authorValues = new ContentValues();
        authorValues.put( column(AuthorsTable.NAME), StringUtils.revertFromEscaped(records[1]));

        long authorId = findRow( AUTHORS, authorValues );
        if ( authorId == ID_MISSING )
            {
            Scribe.note( "Author [" + records[1] + "] does not exists! Item was skipped.");
            return;
            }

        ContentValues values = new ContentValues();

        if ( authorId == ID_NULL )
            values.putNull( column(BooksTable.AUTHOR_ID) );
        else
            values.put( column(BooksTable.AUTHOR_ID), authorId );

        records[2] = StringUtils.revertFromEscaped( records[2] );
        values.put( column(BooksTable.TITLE), records[2] );

        getContentResolver()
                .insert( table(BOOKS).contentUri(), values);
        Scribe.debug( "Book [" + records[2] + "] was inserted.");
        }

    }
