package digitalgarden.mecsek.templates;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;

import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.addFieldToDatabase;
import static digitalgarden.mecsek.database.DatabaseMirror.database;
import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.fieldFull;
import static digitalgarden.mecsek.database.DatabaseMirror.fieldFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.field_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;

public abstract class GenericTable
    {
    private int tableId;

    public GenericTable(int table_id)
        {
        this.tableId = table_id;
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

    public abstract void defineFields();

    private ArrayList<String> createFields = new ArrayList<>();

    private ArrayList<String> createForeignKeys = new ArrayList<>();

    private ArrayList<String> createLeftOuterJoin = new ArrayList<>();

    private int searchFieldIndex = -1;
    private int searchFieldIndexFor = -1;

    protected int addField(String fieldName, String type)
        {
        fieldName = fieldName + "_" + Integer.toString(tableId);

        createFields.add(fieldName + " " + type);
        return addFieldToDatabase( fieldName, name() );
        }

    // Foreign key rész
    protected int addForeignKey(String fieldName, int referenceTableIndex)
        {
        int index = addField(fieldName, "INTEGER");
        createForeignKeys.add(" FOREIGN KEY (" + field(index) +
                ") REFERENCES " + table(referenceTableIndex).name() + " (" + field_id() + ") ");

        createLeftOuterJoin.add(" LEFT OUTER JOIN " + table(referenceTableIndex).name() +
                " ON " + fieldFull( index ) + "=" + fieldFull_id(referenceTableIndex) );

        return index;
        }

    protected int addSearchFieldFor( int fieldIndex )
        {
        if ( searchFieldIndex != -1 )
            throw new IllegalArgumentException("Search field already defined in table " + name());
        searchFieldIndex = addField("search", "TEXT");
        searchFieldIndexFor = fieldIndex;
        return searchFieldIndex;
        }

    public void create(SQLiteDatabase db)
        {
        StringBuilder sb = new StringBuilder("CREATE TABLE ");

        sb.append(name()).append(" (").append( field_id() ).append(" INTEGER PRIMARY KEY");

        for (String createField : createFields)
            sb.append(", ").append(createField);

        for (String createForeignKey : createForeignKeys)
            sb.append(", ").append(createForeignKey);

        sb.append(")");

        db.execSQL(sb.toString());
        }

    public void drop(SQLiteDatabase db)
        {
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
            if (searchFieldIndex != -1)
                {
                values.put(field(searchFieldIndex), StringUtils.normalize(
                        values.getAsString(field(searchFieldIndexFor))));
                }

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
                rowsDeleted = db.delete( name(), field_id() + "=" + id, null);
                }
            else
                {
                rowsDeleted = db.delete( name(), field_id() + "=" + id + " and " + whereClause, whereArgs);
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
            if (searchFieldIndex != -1)
                {
                values.put(field(searchFieldIndex), StringUtils.normalize(
                        values.getAsString(field(searchFieldIndexFor))));
                }

            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(whereClause))
                {
                rowsUpdated = db.update( name(),
                        values,
                        field_id()  + "=" + id,
                        null);
                }
            else
                {
                rowsUpdated = db.update( name(),
                        values,
                        field_id()  + "=" + id
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
            queryBuilder.appendWhere( name() + "." + field_id() + "=" + uri.getLastPathSegment());
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
