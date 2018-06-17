package digitalgarden.mecsek.database.patients;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.formtypes.EditTextField;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GenericEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PATIENTS;


public class PatientsEditFragment extends GenericEditFragment
	{
	private EditTextField nameField;
	private EditTextField dobField;
	private EditTextField tajField;
	private EditTextField phoneField;
	private EditTextField noteField;

	@Override
	protected Uri getTableContentUri()
		{
		return table( PATIENTS).contentUri();
		}

	@Override
	protected int getFormLayout()
		{
		return 	R.layout.patient_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout( View view )
		{
		Scribe.note("PatientsEditFragment setupFormLayout");
		
        nameField = (EditTextField) view.findViewById( R.id.edittext_patient_name );
        nameField.connect( this );

		dobField = (EditTextField) view.findViewById( R.id.edittext_patient_dob );
		dobField.connect( this );

		tajField = (EditTextField) view.findViewById( R.id.edittext_patient_taj );
		tajField.connect( this );

		phoneField = (EditTextField) view.findViewById( R.id.edittext_patient_phone );
		phoneField.connect( this );

		noteField = (EditTextField) view.findViewById( R.id.edittext_patient_note );
		noteField.connect( this );
		}

	@Override
	protected void setupFieldsData(long id)
		{
		Scribe.note("PatientsEditFragment setupFieldsData");

		String[] projection = {
				column(PatientsTable.NAME),
				column(PatientsTable.DOB),
				column(PatientsTable.TAJ),
				column(PatientsTable.PHONE),
				column(PatientsTable.NOTE) };
		Cursor cursor = getActivity().getContentResolver().query(getItemContentUri(), projection, null, null, null);

		if (cursor != null) // Ez vajon kell? 
			{
			cursor.moveToFirst();

			nameField.setText(cursor.getString(cursor.getColumnIndexOrThrow(  column(PatientsTable.NAME ))));
			dobField.setText(cursor.getString(cursor.getColumnIndexOrThrow(  column(PatientsTable.DOB ))));
			tajField.setText(cursor.getString(cursor.getColumnIndexOrThrow(  column(PatientsTable.TAJ ))));
			phoneField.setText(cursor.getString(cursor.getColumnIndexOrThrow(  column(PatientsTable.PHONE ))));
			noteField.setText(cursor.getString(cursor.getColumnIndexOrThrow(  column(PatientsTable.NOTE ))));

			// Always close the cursor
			cursor.close();
			}
		}

	@Override
	protected ContentValues getFieldsData()
		{
		Scribe.note("PatientsEditFragment getFieldsData");

		String name = nameField.getText().toString();
		String dob = dobField.getText().toString();
		String taj = tajField.getText().toString();
		String phone = phoneField.getText().toString();
		String note = noteField.getText().toString();

	    ContentValues values = new ContentValues();
		values.put( column(PatientsTable.NAME), name);
		values.put( column(PatientsTable.DOB), dob);
		values.put( column(PatientsTable.TAJ), taj);
		values.put( column(PatientsTable.PHONE), phone);
		values.put( column(PatientsTable.NOTE), note);

	    return values;
		}

	@Override
	protected void saveFieldData(Bundle data)
		{
		// Itt csak EditTExt van, azt nem kell elmenteni	
		}

	@Override
	protected void retrieveFieldData(Bundle data)
		{
		// Itt csak EditTExt van, azt nem kell elmenteni
		}

	@Override
	protected void checkReturningSelector(int requestCode, long selectedId)
		{
		// Nincs ForeignKey, ezzel nem kell foglalkoznunk
		}
	}
