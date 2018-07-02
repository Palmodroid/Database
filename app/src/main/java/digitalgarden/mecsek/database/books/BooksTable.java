package digitalgarden.mecsek.database.books;

import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.generic.database.GenericTable;

import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;


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
        addExportImportColumn( BooksTable.TITLE );
        addExportImportForeignKey( AUTHOR_ID, AUTHORS, AuthorsTable.NAME );
        }
    }
