package digitalgarden.mecsek.database.authors;


import digitalgarden.mecsek.templates.GenericTable;

public final class AuthorsTable extends GenericTable
    {
    public AuthorsTable(int tableId )
        {
        super( tableId );
        }

    @Override
    public String name()
        {
        return "authors";
        }

    public static int NAME;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn("name", "TEXT");
        SEARCH = addSearchColumnFor(NAME);
        }

    }
