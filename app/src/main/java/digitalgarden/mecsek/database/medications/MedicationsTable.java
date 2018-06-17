package digitalgarden.mecsek.database.medications;

import digitalgarden.mecsek.templates.GenericTable;

import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;

public final class MedicationsTable extends GenericTable
    {
    public MedicationsTable( int tableId )
        {
        super( tableId );
        }

    @Override
    public String name()
        {
        return "medications";
        }

    public static int NAME;
    public static int PILL_ID;
    public static int PATIENT_ID;
    public static int SEARCH;

    @Override
    public void defineColumns()
        {
        NAME = addColumn( "name", "TEXT" );
        PILL_ID = addForeignKey( "pill_id", PILLS );
        PATIENT_ID = addForeignKey( "patient_id", PATIENTS );
        SEARCH = addSearchColumnFor( NAME );
        }

    }

