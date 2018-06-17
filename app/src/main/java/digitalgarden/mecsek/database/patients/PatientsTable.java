package digitalgarden.mecsek.database.patients;

import digitalgarden.mecsek.templates.GenericTable;


public final class PatientsTable extends GenericTable
    {
    public PatientsTable( int tableId )
        {
        super( tableId );
        }

    @Override
    public String name()
        {
        return "patients";
        }

    public static int NAME;
    public static int DOB;
    public static int TAJ;
    public static int PHONE;
    public static int NOTE;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn( "name", "TEXT");
        DOB = addColumn( "dob", "TEXT");
        TAJ = addColumn( "taj", "TEXT");
        PHONE = addColumn( "phone", "TEXT");
        NOTE = addColumn( "note", "TEXT");
        SEARCH = addSearchColumnFor( NAME );
        }
    }
