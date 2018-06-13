package digitalgarden.mecsek.database.pills;


import digitalgarden.mecsek.templates.GenericTable;

public final class PillsTable extends GenericTable
    {
    public PillsTable( int tableId )
        {
        super( tableId );
        }

    @Override
    public String name()
        {
        return "pills";
        }

    public static int NAME;
    public static int SEARCH;

    @Override
    public void defineFields()
        {
        NAME = addField("name", "TEXT");
        SEARCH = addSearchFieldFor( NAME );
        }
    }
