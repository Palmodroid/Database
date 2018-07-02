package digitalgarden.mecsek.generic.database;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.addColumnToDatabase;
import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull;
import static digitalgarden.mecsek.database.DatabaseMirror.columnFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.database;
import static digitalgarden.mecsek.database.DatabaseMirror.table;

public abstract class GenericTable
    {
    private int tableId;

    public GenericTable( int table_id )
        {
        this.tableId = table_id;
        }

    public void setupContext( Context context )
        {
        this.context = context;
        }

    public int id()
        {
        return tableId;
        }

    public static final int COUNTID = 0x100000;
    public static final int DIRID = 0x200000;
    public static final int ITEMID = 0x300000;

    public int id(int ext)
        {
        return ext + id();
        }

    public abstract String name();

    public abstract void defineColumns();

    private ArrayList<String> createColumns = new ArrayList<>();

    private ArrayList<String> createForeignKeys = new ArrayList<>();

    private ArrayList<String> createLeftOuterJoin = new ArrayList<>();

    private String createUniqueConstraint = "";

    private int searchColumnIndex = -1;
    private int searchColumnIndexFor = -1;

    protected int addColumn(String columnName, String columnType, boolean unique)
        {
        columnName = columnName + "_" + Integer.toString(tableId);

        createColumns.add(columnName + " " + columnType + (unique ? " UNIQUE " : ""));
        return addColumnToDatabase( columnName, name() );
        }

    protected int addColumn(String columnName, String columnType)
        {
        return addColumn( columnName, columnType, false );
        }

    protected int addUniqueColumn(String columnName, String columnType)
        {
        return addColumn( columnName, columnType, true );
        }

    // Foreign key rész
    protected int addForeignKey(String columnName, int referenceTableIndex)
        {
        int index = addColumn(columnName, "INTEGER");
        createForeignKeys.add(" FOREIGN KEY (" + column(index) +
                ") REFERENCES " + table(referenceTableIndex).name() + " (" + column_id() + ") ON DELETE CASCADE ");

        createLeftOuterJoin.add(" LEFT OUTER JOIN " + table(referenceTableIndex).name() +
                " ON " + columnFull( index ) + "=" + columnFull_id(referenceTableIndex) );

        return index;
        }

    protected void addUniqueConstraint(int... columnIndices)
        {
        if ( !createUniqueConstraint.isEmpty() )
            throw new IllegalArgumentException("Unique constraint is already defined in table " + name());
        StringBuilder sb = new StringBuilder();
        for (int columnIndex : columnIndices)
            {
            if ( sb.length()!=0 )
                sb.append(", ");
            sb.append( column( columnIndex));
            }
        createUniqueConstraint = ", UNIQUE ( " + sb.toString() + " ) ";
        }

    protected int addSearchColumnFor(int columnIndex )
        {
        if ( searchColumnIndex != -1 )
            throw new IllegalArgumentException("Search column is already defined in table " + name());
        searchColumnIndex = addColumn("search", "TEXT");
        searchColumnIndexFor = columnIndex;
        return searchColumnIndex;
        }

    public void create(SQLiteDatabase db)
        {
        StringBuilder sb = new StringBuilder("CREATE TABLE ");

        sb.append(name()).append(" (").append( column_id() ).append(" INTEGER PRIMARY KEY");

        for (String createColumn : createColumns)
            sb.append(", ").append(createColumn);

        for (String createForeignKey : createForeignKeys)
            sb.append(", ").append(createForeignKey);

        sb.append( createUniqueConstraint );

        sb.append(")");

        Scribe.note("DB Create: " + sb.toString());

        db.execSQL(sb.toString());
        }

    public void drop(SQLiteDatabase db)
        {
        Scribe.note("DB Drop: " + name());
        db.execSQL("DROP TABLE IF EXISTS " + name());
        }

    public String authority()
        {
        return database().authority();
        }

    public String contentCount()
        {
        return database().contentCount();
        }

    // Itt mi lesz az s-sel a végén? Marad?
    public String contentSubtype()
        {
        return "vnd.digitalgarden.mecsek.contentprovider." + name();
        }

    public String contentType()
        {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + contentSubtype();
        }

    public String contentItemType()
        {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + contentSubtype();
        }

    public Uri contentUri()
        {
        return Uri.parse("content://" + authority() + "/" + name());
        }

    public Uri contentCountUri()
        {
        return Uri.parse(contentUri() + contentCount());
        }


    /***
     * CONTENT PROVIDER
     ***/


    public void defineUriMatcher(UriMatcher sURIMatcher)
        {
        sURIMatcher.addURI( authority(), name(), id(DIRID));
        sURIMatcher.addURI( authority(), name() + "/#", id(ITEMID));
        sURIMatcher.addURI( authority(), name() + contentCount(), id(COUNTID));
        }

    public Uri insert( SQLiteDatabase db, Uri uri, int uriType, ContentValues values )
        {
        if ( uriType == id(DIRID) )
            {
            // Az ekezet nelkuli kereseshez meg egy oszlop hozzakerul
            if (searchColumnIndex != -1)
                {
                values.put(column(searchColumnIndex), StringUtils.normalize(
                        values.getAsString(column(searchColumnIndexFor))));
                }

            // ?? insertOnConflict - importnál jó lehet.
            // Egyébként meg meg kellene kérdezni, hogy felülírja-e az előzőt?
            long id = db.insertOrThrow( name(), null, values );

            return Uri.parse( contentUri() + "/" + id);
            }

        return null;
        }

    public int delete( SQLiteDatabase db, Uri uri, int uriType, String whereClause, String[] whereArgs )
        {
        int rowsDeleted = -1;

        if ( uriType == id(DIRID) )
            {
            rowsDeleted = db.delete( name(), whereClause, whereArgs);
            }
        else if ( uriType == id(ITEMID))
            {
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(whereClause))
                {
                rowsDeleted = db.delete( name(), column_id() + "=" + id, null);
                }
            else
                {
                rowsDeleted = db.delete( name(), column_id() + "=" + id + " and " + whereClause, whereArgs);
                }
            }
        return rowsDeleted;
        }


    public int update(SQLiteDatabase db, Uri uri, int uriType, ContentValues values, String whereClause, String[] whereArgs )
        {
        int rowsUpdated = -1;

        if ( uriType == id(DIRID) )
            {
            throw new IllegalArgumentException("Multiple updates on " + name() + " are not allowed: " + uri);
            }
        else if ( uriType == id(ITEMID))
            {
            if (searchColumnIndex != -1)
                {
                values.put(column(searchColumnIndex), StringUtils.normalize(
                        values.getAsString(column(searchColumnIndexFor))));
                }

            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(whereClause))
                {
                rowsUpdated = db.update( name(),
                        values,
                        column_id()  + "=" + id,
                        null);
                }
            else
                {
                rowsUpdated = db.update( name(),
                        values,
                        column_id()  + "=" + id
                                + " and "
                                + whereClause,
                        whereArgs);
                }
            }
        return rowsUpdated;
        }

    public boolean buildQuery(Uri uri, int uriType, SQLiteQueryBuilder queryBuilder )
        {
        if ( uriType == id(DIRID) || uriType == id(COUNTID) )
            {
            StringBuilder sb = new StringBuilder( name() );
            for (String createLeftOuterJoin : this.createLeftOuterJoin)
                sb.append(createLeftOuterJoin);
            queryBuilder.setTables( sb.toString() );
            }
        else if ( uriType == id(ITEMID) )
            {
            StringBuilder sb = new StringBuilder( name() );
            for (String createLeftOuterJoin : this.createLeftOuterJoin)
                sb.append(createLeftOuterJoin);
            queryBuilder.setTables( sb.toString() );
            // Adding the ID to the original query
            queryBuilder.appendWhere( name() + "." + column_id() + "=" + uri.getLastPathSegment());
            }
        else
            return false;
        return true;
        }

    public String[] buildProjection( int uriType, String[] projection )
        {
        if ( uriType == id(COUNTID) )
            {
            projection = new String[]{"count(*) as count"};
            }
        return projection;
        }

    /*** EXPORT-IMPORT ***/

    private Cursor cursor;
    private Context context;

    public abstract void defineExportImportColumns();

    //private ArrayList<String> createExportImportColumns = new ArrayList<>();

    protected void addExportImportColumn(int columnIndex)
        {
        exportImportColumns.add(column(columnIndex));
        }

    protected void addExportImportForeignKey(int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        ExportImportForeignKey exportImportForeignKey = new ExportImportForeignKey();

        exportImportForeignKey.foreignKeyIndex = foreignKeyIndex;
        exportImportForeignKey.forignTableIndex = foreignTableIndex;

        exportImportForeignKey.foreignColumns = new String[foreignColumnIndices.length];
        for ( int i=0; i<exportImportForeignKey.foreignColumns.length; i++ )
            {
            exportImportForeignKey.foreignColumns[i] = column(foreignColumnIndices[i]);
            }


        exportImportForeignKeys.add(exportImportForeignKey);
        }


    protected String[] getRowData(Cursor cursor)
        {
        ArrayList<String> data = new ArrayList<>();

        for ( ExportImportForeignKey foreignKey : exportImportForeignKeys )
            {
            for ( String column : foreignKey.foreignColumns)
                {
                data.add(cursor.getString( cursor.getColumnIndexOrThrow( column )));
                }
            }

        for ( String column : exportImportColumns )
            {
            data.add(cursor.getString( cursor.getColumnIndexOrThrow( column )));
            }
        return data.toArray( new String [0]);
        }

    protected ContentResolver getContentResolver()
        {
        return context.getContentResolver();
        }

    public int collateRows()
        {
        ArrayList<String> projection = new ArrayList<>();

        for ( ExportImportForeignKey foreignKey : exportImportForeignKeys )
            {
            for ( String column : foreignKey.foreignColumns)
                {
                projection.add( column );
                }
            }

        for ( String column : exportImportColumns )
            {
            projection.add( column );
            }

        cursor = getContentResolver().query( contentUri(),
                projection.toArray( new String [0]), null, null, null);

        if (cursor == null)
            return 0;
        else
            return cursor.getCount();
        }

    public String getNextRow()
        {
        if ( cursor!= null && cursor.moveToNext() )
            {
            StringBuilder builder = new StringBuilder();

            builder.append( StringUtils.convertToEscaped( name() ));

            String[] data = getRowData(cursor);
            for (int n=0; n < data.length; n++)
                {
                builder.append('\t');
// Null ellenőrzés!!!
                builder.append( StringUtils.convertToEscaped( data[n] ));
                }

            builder.append('\n');

            return builder.toString();
            }
        else
            return null;
        }

    public void close()
        {
        if (cursor != null)
            cursor.close();
        }


    public long ID_MISSING = -2L;
    public long ID_NULL = -1L;

    /*
    A keresőrutin bizonyos mezők alapján keres.
    Tegyük be az egész hóbelevancot egy ContentValues tömbbe, ahol a KEY értékeknek megfelelő
    oszlopokban a VALUE értéknek kell szerepelnie.
    */
    public long findRow(int tableIndex, ContentValues values)
        {
        // NULL ellenőrzés vajon szükséges?
        long row = ID_MISSING;

        Set<Map.Entry<String, Object>> valueSet = values.valueSet();

        String[] projection = new String[valueSet.size() + 1];
        StringBuilder selection = new StringBuilder();

        int i = 0;
        projection[i++] = column_id();
        for ( Map.Entry<String, Object> entry : valueSet )
            {
            if ( entry.getValue() == null || ((String)entry.getValue()).isEmpty() )
                return ID_NULL;
            if ( selection.length() != 0 )
                selection.append(" AND ");
            selection.append(entry.getKey()).append("=\'").append((String)entry.getValue()).append("\'");
            projection[i++] = entry.getKey();
            }

        Cursor cursor = getContentResolver()
                .query( table(tableIndex).contentUri(), projection, selection.toString(), null, null);

        if ( cursor != null)
            {
            if (cursor.moveToFirst())
                row = cursor.getLong( cursor.getColumnIndexOrThrow( column_id() ) );
            cursor.close();
            }

        return row;
        }

    /*
        private class ExportImportVersion
        {
        private ArrayList<String> exportImportColumns = new ArrayList<>();

        private class ExportImportForeignKey
            {
            int foreignKeyIndex;
            int forignTableIndex;
            String[] foreignColumns;
            }

        private ArrayList<ExportImportForeignKey> exportImportForeignKeys = new ArrayList<>();
        }

        private ExportImportVersion[] exportImportVersions = new ExportImportVersion[database().version()];

        for ( ExportImportVersion version : exportImportVersions )
            version = new ExportImportVersion - ez megy vajon??
     */

    private ArrayList<String> exportImportColumns = new ArrayList<>();

    private class ExportImportForeignKey
        {
        int foreignKeyIndex;
        int forignTableIndex;
        String[] foreignColumns;
        }

    private ArrayList<ExportImportForeignKey> exportImportForeignKeys = new ArrayList<>();

    public void importRow(String[] records)
        {
        int counter = 1;
        ContentValues values = new ContentValues();

        for (ExportImportForeignKey foreignKey : exportImportForeignKeys)
            {
            ContentValues foreignValues = new ContentValues();

            for (String column : foreignKey.foreignColumns)
                {
                if (counter == records.length)
                    return;
                foreignValues.put(column, StringUtils.revertFromEscaped(records[counter++]));
                }

            long row = findRow( foreignKey.forignTableIndex, foreignValues);

            if ( row == ID_MISSING )
                {
                Scribe.note( "Item does not exists! Row was skipped.");
                return;
                }
            if ( row == ID_NULL )
                {
                values.putNull(column(foreignKey.foreignKeyIndex));
                }
            else
                values.put( column(foreignKey.foreignKeyIndex), row );
            }

        for (String column : exportImportColumns)
            {
            if (counter == records.length)
                return;
            values.put( column, StringUtils.revertFromEscaped(records[counter++] ));
            }

        getContentResolver()
                .insert(contentUri(), values);
        Scribe.debug( name() + "[" + records[1] + "] was inserted.");
        }

//            Scribe.note( "Parameters missing from MEDICATIONS row. Item was skipped.");
    }
