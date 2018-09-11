package digitalgarden.mecsek.database.calendar;


import digitalgarden.mecsek.generic.database.GenericTableWithSource;

public final class CalendarTable extends GenericTableWithSource
    {
    public CalendarTable(int tableId )
        {
        super( tableId );
        }

    @Override
    public String name()
        {
        return "cal";
        }

    public static int DATE;
    public static int NOTE;

    @Override
    public void defineColumns()
        {
        DATE = addColumn( TYPE_DATE, "date" );
        NOTE = addColumn( TYPE_TEXT, "note" );
        addSourceColumns();
        }

    @Override
    public void defineExportImportColumns()
        {
        exportImport().addColumnAllVersions( CalendarTable.NOTE );
        exportImport().addColumnAllVersions( CalendarTable.DATE );
        }

    }
