package digitalgarden.mecsek.exportimport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import digitalgarden.mecsek.database.patients.PatientsTable;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.utils.StringUtils;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
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
				column(PatientsTable.NAME),
                column(PatientsTable.DOB),
                column(PatientsTable.TAJ),
                column(PatientsTable.PHONE),
                column(PatientsTable.NOTE) };
		}

	@Override
	protected String[] getRowData(Cursor cursor)
		{
		return new String[] {
				cursor.getString( cursor.getColumnIndexOrThrow( column(PatientsTable.NAME) )),
				cursor.getString( cursor.getColumnIndexOrThrow( column(PatientsTable.DOB) )),
				cursor.getString( cursor.getColumnIndexOrThrow( column(PatientsTable.TAJ) )),
				cursor.getString( cursor.getColumnIndexOrThrow( column(PatientsTable.PHONE) )),
				cursor.getString( cursor.getColumnIndexOrThrow( column(PatientsTable.NOTE) )) };
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
        values.put( column(PatientsTable.NAME), records[1] );

        records[2] = StringUtils.revertFromEscaped( records[2] );
        values.put( column(PatientsTable.DOB), records[2] );

        records[3] = StringUtils.revertFromEscaped( records[3] );
        values.put( column(PatientsTable.TAJ), records[3] );

        records[4] = StringUtils.revertFromEscaped( records[4] );
        values.put( column(PatientsTable.PHONE), records[4] );

        records[5] = StringUtils.revertFromEscaped( records[5] );
        values.put( column(PatientsTable.NOTE), records[5] );

        getContentResolver()
                .insert( table(PATIENTS).contentUri(), values);
        Scribe.debug( "Patient [" + records[1] + "] was inserted.");
		}

	}
