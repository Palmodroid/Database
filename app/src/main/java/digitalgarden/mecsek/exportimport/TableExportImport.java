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


/**
 * Minden egyes GenericTable-hez tartozik egy TableExportImport osztály is.
 * (Ezt a GenericTable.tableExportImport privát változó tárolja, melyet a
 * GenericTable.exportImport() metódussal kérhetünk el.
 * Fontos! A TableExportrImport osztálynak szüksége van a context-re (hogy elkérje a
 * contentResolver-t) Ezt a DatabaseMirror.start() metódus tölti fel a setupContext() metódussal)
 *
 * Minden egyes verziónak külön exportimport szabályt állíthatunk fel (vagyis megmondhatjuk, hogy
 * abban a verzióban melyik oszlopokat exportáljuk/importáljuk.
 * Az egyes verzióban exportálni/importálni tervezett oszlopokat az <b>exportImportVersions[]</b>
 * tömb tárolja. (Eggyel több eleme van, mint az utolsó verziószámnak, hiszen azt is tárolnia kell)
 *
 * Minden egyes szabály tárolja:
 *
 * exportImportColumns listát, mely az egyes - exportálni kívánt - oszlopok indexét tartalmazza.
 *
 * exportImportExternKey listát, mely tartalmazza:
 *      externKeyIndex - foreign key oszlop indexét,
 *      externTableIndex - foreign table indexét
 *      externColumns - foreign táblában lévő foreign oszlopok NEVÉT.
 *
 * exportImportForeignKey listát, mely tartalmazza:
 *      foreignKeyIndex - foreign key oszlop indexét,
 *      foreignTableIndex - foreign table indexét
 *      foreignColumns - foreign táblában lévő foreign oszlopok NEVÉT.
 *
 *  FONTOS!
 *  
 *  A foreign key és az extern key is egy külső tábla egyetlen sorára hivatkozik.
 *  DE! 
 *  A foreign key csak "kiválasztja" ezt a sort (vagyis több key is hivatkozhat ugyanarra a 
 *  sorra) Emiatt a foreign táblát előbb importáljuk és abban már minden sornak meg kell lennie.
 *  Az extern key által hivatkozott sor része ennek az adatsornak, csak másik táblában tároljuk.
 *  (Emiatt pl. lehetnek azonosak is a sorok ebben a táblában) 
 *  Ilyenkor nem kikeressük a megfelelő sort az extern táblában, hanem létrehozunk egy új sort az
 *  hivatkozást tartalmazó adatsor számára.
 *  Fontos, hogy az extern táblákban NEM szabad exportálni (vagy legalábbis importálni) azokat a 
 *  sorokat, amelyekhez hivatkozó adatsorok tartoznak.
 *  
 * AZ exportálni kívánt részeket az add... metódusokkal adjuk hozzá.
 */
public class TableExportImport
    {
    private Cursor cursor;
    private GenericTable table; // Ebből csak name és contentUri kell
    private Context context;

    public long ID_MISSING = -2L;
    public long ID_NULL = -1L;


    private class ExportImportExternKey
        {
        // Extern Key column index - a saját táblában
        int externKeyIndex;
        // Extern Table index - az idegen tábla // importhoz kell, export a join táblát használja
        int externTableIndex;
        // Extern Table szükséges oszlopainak neve
        String[] externColumns;

        private ExportImportExternKey(int externKeyIndex, int externTableIndex, int... externColumnIndices)
            {
            this.externKeyIndex = externKeyIndex;
            this.externTableIndex = externTableIndex;

            this.externColumns = new String[externColumnIndices.length];
            for ( int i=0; i<this.externColumns.length; i++ )
                {
                this.externColumns[i] = column(externColumnIndices[i]);
                }
            }
        }


    private class ExportImportForeignKey
        {
        // Foreign Key column index - a saját táblában
        int foreignKeyIndex;
        // Foreign Table index - az idegen tábla // importhoz kell, export a join táblát használja
        int foreignTableIndex;
        // Foreign Table szükséges oszlopainak neve
        String[] foreignColumns;

        private ExportImportForeignKey(int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
            {
            this.foreignKeyIndex = foreignKeyIndex;
            this.foreignTableIndex = foreignTableIndex;

            this.foreignColumns = new String[foreignColumnIndices.length];
            for ( int i=0; i<this.foreignColumns.length; i++ )
                {
                this.foreignColumns[i] = column(foreignColumnIndices[i]);
                }
            }
        }

    private class ExportImportVersion
        {
        private ArrayList<ExportImportForeignKey> exportImportForeignKeys = new ArrayList<>();
        private ArrayList<Integer> exportImportColumns = new ArrayList<>();
        private ArrayList<ExportImportExternKey> exportImportExternKeys = new ArrayList<>();
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
                new ExportImportForeignKey( foreignKeyIndex, foreignTableIndex, foreignColumnIndices) );
        }

    public void addForeignKeySomeVersions(int firstVersion, int lastVersion,
                                          int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        ExportImportForeignKey exportImportForeignKey =
                new ExportImportForeignKey( foreignKeyIndex, foreignTableIndex, foreignColumnIndices);

        for (int n = firstVersion; n <= lastVersion; n++)
            {
            exportImportVersions[n].exportImportForeignKeys.add(exportImportForeignKey);
            }
        }

    public void addForeignKeyFromVersion(int firstVersion,
                                          int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        ExportImportForeignKey exportImportForeignKey =
                new ExportImportForeignKey( foreignKeyIndex, foreignTableIndex, foreignColumnIndices);

        for (int n = firstVersion; n <= database().version(); n++)
            {
            exportImportVersions[n].exportImportForeignKeys.add(exportImportForeignKey);
            }
        }

    public void addForeignKeyAllVersions(int foreignKeyIndex, int foreignTableIndex, int... foreignColumnIndices)
        {
        ExportImportForeignKey exportImportForeignKey =
                new ExportImportForeignKey( foreignKeyIndex, foreignTableIndex, foreignColumnIndices);

        for (int n = 0; n <= database().version(); n++)
            {
            exportImportVersions[n].exportImportForeignKeys.add(exportImportForeignKey);
            }
        }


    public void addExternKey(int version, int externKeyIndex, int externTableIndex, int... externColumnIndices)
        {
        exportImportVersions[version].exportImportExternKeys.add(
                new ExportImportExternKey( externKeyIndex, externTableIndex, externColumnIndices) );
        }

    public void addExternKeySomeVersions(int firstVersion, int lastVersion,
                                          int externKeyIndex, int externTableIndex, int... externColumnIndices)
        {
        ExportImportExternKey exportImportExternKey =
                new ExportImportExternKey( externKeyIndex, externTableIndex, externColumnIndices);

        for (int n = firstVersion; n <= lastVersion; n++)
            {
            exportImportVersions[n].exportImportExternKeys.add(exportImportExternKey);
            }
        }

    public void addExternKeyFromVersion(int firstVersion,
                                         int externKeyIndex, int externTableIndex, int... externColumnIndices)
        {
        ExportImportExternKey exportImportExternKey =
                new ExportImportExternKey( externKeyIndex, externTableIndex, externColumnIndices);

        for (int n = firstVersion; n <= database().version(); n++)
            {
            exportImportVersions[n].exportImportExternKeys.add(exportImportExternKey);
            }
        }

    public void addExternKeyAllVersions(int externKeyIndex, int externTableIndex, int... externColumnIndices)
        {
        ExportImportExternKey exportImportExternKey =
                new ExportImportExternKey( externKeyIndex, externTableIndex, externColumnIndices);

        for (int n = 0; n <= database().version(); n++)
            {
            exportImportVersions[n].exportImportExternKeys.add(exportImportExternKey);
            }
        }


    protected ContentResolver getContentResolver()
        {
        return context.getContentResolver();
        }


    /**
     * Lekérdezi a join tábla összes sorát az exportálni kívánt oszlopokra. A cursor értéket
     * az osztályban tárolja. Ezt majd a getNextRow() fogja felhasználni, és a close() bezárni.
     * @return sorok száma
     */
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

        for ( ExportImportExternKey externKey : version().exportImportExternKeys )
            {
            for ( String column : externKey.externColumns)
                {
                projection.add( column );
                }
            }

        cursor = getContentResolver().query( table.contentUri(),
                projection.toArray( new String [0]), null, null, null);

        if (cursor == null)
            return 0;
        else
            return cursor.getCount();
        }


    /**
     *
     * @return
     */
    String getNextRow()
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

        for ( ExportImportExternKey externKey : version().exportImportExternKeys )
            {
            for ( String column : externKey.externColumns)
                {
                data.add(cursor.getString( cursor.getColumnIndexOrThrow( column )));
                }
            }

        return data.toArray( new String [0]);
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
            int foreignTableIndex;
            String[] foreignColumns;
            }

        private ArrayList<ExportImportForeignKey> exportImportForeignKeys = new ArrayList<>();
        }

        private ExportImportVersion[] exportImportVersions = new ExportImportVersion[database().version()];

        for ( ExportImportVersion version : exportImportVersions )
            version = new ExportImportVersion - ez megy vajon??
     */
/////////////////////NINCS BENNE AZ EXTERN RÉSZ//////////////////////////////////
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

            long row = findRow( foreignKey.foreignTableIndex, foreignValues);

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
