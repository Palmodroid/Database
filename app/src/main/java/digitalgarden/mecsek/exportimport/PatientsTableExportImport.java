package digitalgarden.mecsek.exportimport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;


public class PatientsTableExportImport extends GeneralTableExportImport
	{
	public PatientsTableExportImport(Context context)
		{
		super(context);
		// TODO Auto-generated constructor stub
		}

	@Override
	protected Uri getContentUri()
		{
		return table(PATIENTS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		return new String[] {
				field(PatientsTable.NAME),
                field(PatientsTable.DOB),
                field(PatientsTable.TAJ),
                field(PatientsTable.PHONE),
                field(PatientsTable.NOTE) };
		}

	@Override
	protected String[] getRowData(Cursor cursor)
		{
		return new String[] {
				cursor.getString( cursor.getColumnIndexOrThrow( field(PatientsTable.NAME) )),
				cursor.getString( cursor.getColumnIndexOrThrow( field(PatientsTable.DOB) )),
				cursor.getString( cursor.getColumnIndexOrThrow( field(PatientsTable.TAJ) )),
				cursor.getString( cursor.getColumnIndexOrThrow( field(PatientsTable.PHONE) )),
				cursor.getString( cursor.getColumnIndexOrThrow( field(PatientsTable.NOTE) )) };
		}

	@Override
	protected String getTableName()
		{
		return table(PATIENTS).name();
		}
	
	@Override
	public void importRow(String[] records)
		{
        // Több adat miatt itt szükséges a hossz ellenőrzése
        if ( records.length < 6 )
            {
            Scribe.note( "Parameters missing from PATIENTS row. Item was skipped.");
            return;
            }

        ContentValues values = new ContentValues();

        records[1] = StringUtils.revertFromEscaped( records[1] );
        values.put( field(PatientsTable.NAME), records[1] );

        records[2] = StringUtils.revertFromEscaped( records[2] );
        values.put( field(PatientsTable.DOB), records[2] );

        records[3] = StringUtils.revertFromEscaped( records[3] );
        values.put( field(PatientsTable.TAJ), records[3] );

        records[4] = StringUtils.revertFromEscaped( records[4] );
        values.put( field(PatientsTable.PHONE), records[4] );

        records[5] = StringUtils.revertFromEscaped( records[5] );
        values.put( field(PatientsTable.NOTE), records[5] );

        getContentResolver()
                .insert( table(PATIENTS).contentUri(), values);
        Scribe.debug( "Patient [" + records[1] + "] was inserted.");
		}

	}
