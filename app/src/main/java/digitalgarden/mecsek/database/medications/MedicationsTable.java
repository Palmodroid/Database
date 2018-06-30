package digitalgarden.mecsek.database.medications;

import android.content.ContentValues;

import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GenericTable;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.MEDICATIONS;
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

        //addUniqueColumn
        //addUniqueContstraint(NAME, DOB, TAJ);
        }

    public void add(int... ints)
        {
        ints[ints.length-1]= 5;
        }

    @Override
    public void defineExportImportColumns()
        {
        addExportImportColumn( MedicationsTable.NAME );

        addExportImportColumn( PillsTable.NAME );
        // addExportImportForeignKey( PILL_ID, PILLS, PillsTable.NAME );

        addExportImportColumn( PatientsTable.NAME );
        addExportImportColumn( PatientsTable.DOB );
        addExportImportColumn( PatientsTable.TAJ );
        // addExportImportForeignKey( PATIENT_ID, PATIENTS, PatientsTable.NAME, PatientsTable.DOB, PatientsTable.TAJ );
        }

    @Override
    public void importRow(String[] records)
        {
        // Két adat miatt itt szükséges a hossz ellenőrzése
        if ( records.length < 6 )
            {
            Scribe.note( "Parameters missing from MEDICATIONS row. Item was skipped.");
            return;
            }

        ContentValues pillValues = new ContentValues();
        pillValues.put( column(PillsTable.NAME), StringUtils.revertFromEscaped(records[2]));

        long pillId = findRow( PILLS, pillValues );
        if ( pillId == ID_MISSING )
            {
            Scribe.note( "Pill [" + records[2] + "] does not exists! Item was skipped.");
            return;
            }

        ContentValues patientValues = new ContentValues();
        patientValues.put( column(PatientsTable.NAME), StringUtils.revertFromEscaped(records[3]));
        patientValues.put( column(PatientsTable.DOB), StringUtils.revertFromEscaped(records[4]));
        patientValues.put( column(PatientsTable.TAJ), StringUtils.revertFromEscaped(records[5]));

        long patientId = findRow( PATIENTS, patientValues );
        if ( patientId == ID_MISSING )
            {
            Scribe.note( "Patient [" + records[3] + "] does not exists! Item was skipped.");
            return;
            }

        ContentValues values = new ContentValues();

        if ( pillId == ID_NULL )
            values.putNull( column(MedicationsTable.PILL_ID) );
        else
            values.put( column(MedicationsTable.PILL_ID), pillId );

        if ( patientId == ID_NULL )
            values.putNull( column(MedicationsTable.PATIENT_ID) );
        else
            values.put( column(MedicationsTable.PATIENT_ID), patientId );

        records[1] = StringUtils.revertFromEscaped( records[1] );
        values.put( column(MedicationsTable.NAME), records[1] );

        getContentResolver()
                .insert( table(MEDICATIONS).contentUri(), values);
        Scribe.debug( "Medication [" + records[1] + "] was inserted.");
        }

    }

