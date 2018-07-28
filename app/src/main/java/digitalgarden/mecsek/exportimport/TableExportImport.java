package digitalgarden.mecsek.exportimport;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import digitalgarden.mecsek.generic.database.GenericTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.Longtime;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.columnType;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.database;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.generic.database.GenericTable.TYPE_DATE;
import static digitalgarden.mecsek.generic.database.GenericTable.TYPE_TEXT;

public class TableExportImport
    {
    private Cursor cursor;
    private GenericTable table; // Ebből csak name és contentUri kell
    private Context context;

    public long ID_MISSING = -2L;
    public long ID_NULL = -1L;


    private class ExportImportForeignKey
        {
        int foreignKeyIndex;
        int forignTableIndex;
        String[] foreignColumns;
        }

    private class ExportImportVersion
        {
        private ArrayList<Integer> exportImportColumns = new ArrayList<>();
        private ArrayList<ExportImportForeignKey> exportImportForeignKeys = new ArrayList<>();
        }

    private ExportImportVersion[] exportImportVersions = new ExportImportVersion[database().version()+1];


    public TableExportImport(GenericTable table )
        {
        this.table = table;

        // Fel kell tölteni, nem lehet üres!
        for (int n = 0; n <= database().version(); n++)
            {
            exportImportVersions[n] = new ExportImportVersion();
            }
        }

    public void setupContext( Context context )
        {
        this.context = context;
        }


    public ExportImportVersion version()
        {
        return exportImportVersions[database().version()];
        }


    public void addColumn(int version, int columnIndex)
        {
        exportImportVersions[version].exportImportColumns.add( columnIndex );
        }

    public void addColumnFromVersion(int firstVersion,  int columnType, int columnIndex)
        {
        addColumnSomeVersions(firstVersion, database().version(), columnIndex);
        }

    public void addColumnSomeVersions(int firstVersion, int lastVersion, int columnIndex)
        {
        for (int n = firstVersion; n <= lastVersion; n++)
            {
            addColumn(n, columnIndex);
            }
        }

    public void addColumnAllVersions(int columnIndex)
        {
        addColumnSomeVersions(0, database().version(), columnIndex);
        }


    public void addForeignKey(int version, int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        exportImportVersions[version].exportImportForeignKeys.add(
                createForeignKey( foreignKeyIndex, foreignTableIndex, foreignColumnIndices) );
        }

    public void addForeignKeySomeVersions(int firstVersion, int lastVersion,
                                          int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        ExportImportForeignKey exportImportForeignKey =
                createForeignKey( foreignKeyIndex, foreignTableIndex, foreignColumnIndices);

        for (int n = firstVersion; n <= lastVersion; n++)
            {
            exportImportVersions[n].exportImportForeignKeys.add(exportImportForeignKey);
            }
        }

    public void addForeignKeyFromVersion(int firstVersion,
                                          int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        ExportImportForeignKey exportImportForeignKey =
                createForeignKey( foreignKeyIndex, foreignTableIndex, foreignColumnIndices);

        for (int n = firstVersion; n <= database().version(); n++)
            {
            exportImportVersions[n].exportImportForeignKeys.add(exportImportForeignKey);
            }
        }

    public void addForeignKeyAllVersions(int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        ExportImportForeignKey exportImportForeignKey =
                createForeignKey( foreignKeyIndex, foreignTableIndex, foreignColumnIndices);

        for (int n = 0; n <= database().version(); n++)
            {
            exportImportVersions[n].exportImportForeignKeys.add(exportImportForeignKey);
            }
        }

    private ExportImportForeignKey createForeignKey(int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        ExportImportForeignKey exportImportForeignKey = new ExportImportForeignKey();

        exportImportForeignKey.foreignKeyIndex = foreignKeyIndex;
        exportImportForeignKey.forignTableIndex = foreignTableIndex;

        exportImportForeignKey.foreignColumns = new String[foreignColumnIndices.length];
        for ( int i=0; i<exportImportForeignKey.foreignColumns.length; i++ )
            {
            exportImportForeignKey.foreignColumns[i] = column(foreignColumnIndices[i]);
            }

        return exportImportForeignKey;
        }



    protected String[] getRowData(Cursor cursor)
        {
        ArrayList<String> data = new ArrayList<>();

        for ( ExportImportForeignKey foreignKey : version().exportImportForeignKeys )
            {
            for ( String column : foreignKey.foreignColumns)
                {
                data.add(cursor.getString( cursor.getColumnIndexOrThrow( column )));
                }
            }

        for ( Integer exportImportColumn : version().exportImportColumns )
            {
            if ( columnType(exportImportColumn) == TYPE_TEXT )
                {
                data.add(cursor.getString(cursor.getColumnIndexOrThrow( column(exportImportColumn))));
                }
            else if ( columnType(exportImportColumn) == TYPE_DATE )
                {
                Longtime longtime = new Longtime();
                longtime.set(cursor.getLong(cursor.getColumnIndexOrThrow( column(exportImportColumn))));
                data.add( longtime.toString(false));
                // Ha hibás, akkor hol lesz jelzés??
                }
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

        for ( ExportImportForeignKey foreignKey : version().exportImportForeignKeys )
            {
            for ( String column : foreignKey.foreignColumns)
                {
                projection.add( column );
                }
            }

        for ( Integer columnIndex : version().exportImportColumns )
            {
            projection.add( column(columnIndex) );
            }

        cursor = getContentResolver().query( table.contentUri(),
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

            builder.append( StringUtils.convertToEscaped( table.name() ));

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

    public void importRow(int version, String[] records)
        {
        int counter = 1;
        ContentValues values = new ContentValues();

        for (ExportImportForeignKey foreignKey : exportImportVersions[version].exportImportForeignKeys)
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

        for (Integer columnIndex : exportImportVersions[version].exportImportColumns)
            {
            if (counter == records.length)
                return;

            if ( columnType(columnIndex) == TYPE_TEXT )
                {
                values.put(column(columnIndex), StringUtils.revertFromEscaped(records[counter]));
                }
            else if ( columnType(columnIndex) == TYPE_DATE )
                {
                Longtime longtime = new Longtime();
                longtime.setDate( records[counter] );
                values.put(column(columnIndex), longtime.get() );
                }

            counter ++;
            }

        getContentResolver()
                .insert(table.contentUri(), values);
        Scribe.debug( table.name() + "[" + records[1] + "] was inserted.");
        }

//            Scribe.note( "Parameters missing from MEDICATIONS row. Item was skipped.");
    }
