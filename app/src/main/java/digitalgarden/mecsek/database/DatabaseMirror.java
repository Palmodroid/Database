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
            table.defineFields();
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


    /*** FIELDS ***/

    private static class Field
        {
        private String fieldName;
        private String tableName;

        Field(String fieldName, String tableName)
            {
            this.fieldName = fieldName;
            this.tableName = tableName;
            }
        }

    private static ArrayList<Field> fields = new ArrayList<>();

    public static int addFieldToDatabase(String fieldName, String tableName )
        {
        fields.add( new Field ( fieldName, tableName ));
        return fields.size() - 1;
        }

    public static String field( int fieldIndex )
        {
        return fields.get(fieldIndex).fieldName;
        }

    public static String fieldFull( int fieldIndex )
        {
        return fields.get( fieldIndex ).tableName + "." + field(fieldIndex);
        }

    public static String field_id()
        {
        return _ID;
        }

    public static String fieldFull_id( int tableIndex )
        {
        return table( tableIndex ).name() + "." + _ID;
        }
    }
