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
    public void defineFields()
        {
        NAME = addField( "name", "TEXT");
        DOB = addField( "dob", "TEXT");
        TAJ = addField( "taj", "TEXT");
        PHONE = addField( "phone", "TEXT");
        NOTE = addField( "note", "TEXT");
        SEARCH = addSearchFieldFor( NAME );
        }
    }
