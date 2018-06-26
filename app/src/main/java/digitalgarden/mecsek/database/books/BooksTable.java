package digitalgarden.mecsek.database.books;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GenericTable;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
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
    protected Uri getContentUri()
        {
        return table(BOOKS).contentUri();
        }

    @Override
    protected String[] getProjection()
        {
        return new String[] {
                column(AuthorsTable.NAME),
                column(BooksTable.TITLE) };
        }

    @Override
    protected String[] getRowData(Cursor cursor)
        {
        return new String[] {
                cursor.getString( cursor.getColumnIndexOrThrow( column(AuthorsTable.NAME ))),
                cursor.getString( cursor.getColumnIndexOrThrow( column(BooksTable.TITLE ))) };
        }

    @Override
    public String getTableName()
        {
        return table(BOOKS).name();
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

        long authorId = findAuthorId( records[1] );
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


    private long ID_MISSING = -2L;
    private long ID_NULL = -1L;

    // Ezt a keresőrutint nehéz generalizálni, mert az azonosító paraméterek típusa is különböző lehet
    private long findAuthorId(String authorName)
        {
        if ( authorName == null )
            {
            return ID_NULL;
            }

        long authorId = ID_MISSING;

        String[] projection = {
                column_id(),
                column(AuthorsTable.NAME) };
        Cursor cursor = getContentResolver()
                .query(table(AUTHORS).contentUri(), projection,
                        column(AuthorsTable.NAME) + "=\'" + StringUtils.revertFromEscaped( authorName ) + "\'",
                        null, null);

        if ( cursor != null)
            {
            if (cursor.moveToFirst())
                authorId = cursor.getLong( cursor.getColumnIndexOrThrow( column_id() ) );
            cursor.close();
            }

        return authorId;
        }

    }
