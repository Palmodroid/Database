package digitalgarden.mecsek.exportimport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import digitalgarden.mecsek.database.medications.MedicationsTable;
import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.database.pills.PillsTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.MEDICATIONS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class MedicationsTableExportImport extends GeneralTableExportImport
	{
	public MedicationsTableExportImport(Context context)
		{
		super(context);
		// TODO Auto-generated constructor stub
		}

	@Override
	protected Uri getContentUri()
		{
		return table(MEDICATIONS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		return new String[] {
                column(MedicationsTable.NAME),
                column(PillsTable.NAME),
                column(PatientsTable.NAME),
                column(PatientsTable.DOB),
                column(PatientsTable.TAJ) };
		}

	@Override
	protected String[] getRowData(Cursor cursor)
		{
		return new String[] {
                cursor.getString( cursor.getColumnIndexOrThrow( column(MedicationsTable.NAME) )),
                cursor.getString( cursor.getColumnIndexOrThrow( column(PillsTable.NAME) )),
                cursor.getString( cursor.getColumnIndexOrThrow( column(PatientsTable.NAME) )),
                cursor.getString( cursor.getColumnIndexOrThrow( column(PatientsTable.DOB) )),
                cursor.getString( cursor.getColumnIndexOrThrow( column(PatientsTable.TAJ) ))
        };
		}

	@Override
	protected String getTableName()
		{
		return table(MEDICATIONS).name();
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
		
		long pillId = findPillId( records[2] );
		if ( pillId == ID_MISSING )
			{
			Scribe.note( "Pill [" + records[2] + "] does not exists! Item was skipped.");
			return;
			}

        long patientId = findPatientId( records[3], records[4], records[5] );
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

	
	private long ID_MISSING = -2L;
	private long ID_NULL = -1L;
	
	// Ezt a keresőrutint nehéz generalizálni, mert az azonosító paraméterek típusa is különböző lehet
	private long findPillId(String pillName)
		{
		if ( pillName == null )
			{
    		return ID_NULL;
			}

		long pillId = ID_MISSING;
		
		String[] projection = {
			    column_id(),
                column(PillsTable.NAME) };
		Cursor cursor = getContentResolver()
			.query( table(PILLS).contentUri(), projection,
                    column(PillsTable.NAME) + "=\'" + StringUtils.revertFromEscaped( pillName ) + "\'",
				   null, null);

		if ( cursor != null)
			{
			if (cursor.moveToFirst())
				pillId = cursor.getLong( cursor.getColumnIndexOrThrow( column_id() ) );
			cursor.close();
			}
		
		return pillId;
		}
 
    // Ezt a keresőrutint nehéz generalizálni, mert az azonosító paraméterek típusa is különböző lehet
    private long findPatientId(String patientName, String patientDob, String patientTaj)
        {
        if ( patientName == null || patientDob == null || patientTaj == null)
            {
            return ID_NULL;
            }

        long patientId = ID_MISSING;

        String[] projection = {
                column_id(),
                column(PatientsTable.NAME),
                column(PatientsTable.DOB),
                column(PatientsTable.TAJ) };
        Cursor cursor = getContentResolver()
                .query( table(PATIENTS).contentUri(), projection,
                        column(PatientsTable.NAME) + "=\'" + StringUtils.revertFromEscaped( patientName ) + "\' AND " +
                                column(PatientsTable.DOB) + "=\'" + StringUtils.revertFromEscaped( patientDob ) + "\' AND " +
                                column(PatientsTable.TAJ) + "=\'" + StringUtils.revertFromEscaped( patientTaj ) + "\'",
                        null, null);

        if ( cursor != null)
            {
            if (cursor.moveToFirst())
                patientId = cursor.getLong( cursor.getColumnIndexOrThrow( column_id() ) );
            cursor.close();
            }

        return patientId;
        }
	}
