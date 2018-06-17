package digitalgarden.mecsek.database;

import android.content.UriMatcher;
import android.net.Uri;

import java.util.ArrayList;

import digitalgarden.mecsek.database.library.LibraryDatabase;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GenericDatabase;
import digitalgarden.mecsek.templates.GenericTable;

import static android.provider.BaseColumns._ID;
import static digitalgarden.mecsek.Debug.DB;


public class DatabaseMirror
    {
    /*** DEFINE DATABASE ***/

    protected static GenericDatabase defineDatabase()
        {
        return new LibraryDatabase();
        }


    /*** START ***/

    public static void start()
        {
        Scribe.locus(DB);

        database = defineDatabase();

        database().defineTables();

        for (GenericTable table : allTables())
            {
            table.defineColumns();
            }

        for (GenericTable table : allTables() )
            {
            table.defineUriMatcher( uriMatcher );
            }
        }


    /*** URIMatcher ***/

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static int match (Uri uri)
        {
        return uriMatcher.match(uri);
        }

    /*** DATABASE ***/

    private static GenericDatabase database;

    public static GenericDatabase database()
        {
        return database;
        }


    /*** TABLES ***/

    private static ArrayList<GenericTable> tables = new ArrayList<>();

    public static int addTableToDatabase(GenericTable table )
        {
        tables.add(table);
        return tables.size()-1;
        }

    public static GenericTable table( int index )
        {
        return tables.get(index);
        }

    public static Iterable<GenericTable> allTables()
        {
        return tables;
        }


    /*** COLUMNS ***/

    private static class Column
        {
        private String tableName;
        private String columnName;

        Column(String columnName, String tableName)
            {
            this.columnName = columnName;
            this.tableName = tableName;
            }
        }

    private static ArrayList<Column> columns = new ArrayList<>();

    public static int addColumnToDatabase(String ColumnName, String tableName )
        {
        columns.add( new Column( ColumnName, tableName ));
        return columns.size() - 1;
        }

    public static String column(int columnIndex )
        {
        return columns.get(columnIndex).columnName;
        }

    public static String columnFull(int columnIndex )
        {
        return columns.get( columnIndex ).tableName + "." + column(columnIndex);
        }

    public static String column_id()
        {
        return _ID;
        }

    public static String columnFull_id(int tableIndex )
        {
        return table( tableIndex ).name() + "." + _ID;
        }
    }
