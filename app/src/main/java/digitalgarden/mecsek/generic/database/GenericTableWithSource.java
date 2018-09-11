package digitalgarden.mecsek.generic.database;


public abstract class GenericTableWithSource extends GenericTable
    {
    private static final String SOURCE_TABLE_COLUMN_NAME = "srctbl";
    private static final String SOURCE_ROW_COLUMN_NAME = "srcrow";

    public int SOURCE_TABLE = -1;
    public int SOURCE_ROW;

    public GenericTableWithSource( int table_id )
        {
        super( table_id );
        }

    protected void addSourceColumns()
        {
        SOURCE_TABLE = addColumn( TYPE_TEXT, SOURCE_TABLE_COLUMN_NAME);
        SOURCE_ROW = addColumn( TYPE_KEY, SOURCE_ROW_COLUMN_NAME);
        }

    protected boolean hasSourceColumns()
        {
        return SOURCE_TABLE >= 0;
        }
    }
