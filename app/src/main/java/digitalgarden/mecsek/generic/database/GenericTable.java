package digitalgarden.mecsek.generic.database;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;

import digitalgarden.mecsek.exportimport.TableExportImport;
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

    private TableExportImport tableExportImport;

    public GenericTable( int table_id )
        {
        this.tableId = table_id;
        this.tableExportImport = new TableExportImport( this );
        }

    public int id()
        {
        return tableId;
        }

    public TableExportImport exportImport()
        {
        return tableExportImport;
        }

    public abstract void defineExportImportColumns();

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
            long id = db.insert( name(), null, values );

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

    }
