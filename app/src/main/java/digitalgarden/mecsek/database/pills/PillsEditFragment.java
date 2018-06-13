package digitalgarden.mecsek.database.pills;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.medications.MedicationsControllActivity;
import digitalgarden.mecsek.formtypes.EditTextField;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GeneralEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.PILLS;


public class PillsEditFragment extends GeneralEditFragment
	{
	private EditTextField pillNameField;

	@Override
	protected Uri getTableContentUri()
		{
		return table(PILLS).contentUri();
		}

	@Override
	protected int getFormLayout()
		{
		return 	R.layout.pill_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout( View view )
		{
		Scribe.note("PillsEditFragment setupFormLayout");
		
        pillNameField = (EditTextField) view.findViewById( R.id.edittext_pill_name );
        pillNameField.connect( this );

		setupListButton( MedicationsControllActivity.class,
    			getActivity().getString( R.string.button_medication_list ),
    			getActivity().getString( R.string.medications_with ),
    			pillNameField );
    	}

	@Override
	protected void setupFieldsData(long id)
		{
		Scribe.note("PillsEditFragment setupFieldsData");

		String[] projection = {
				field(PillsTable.NAME) };
		Cursor cursor = getActivity().getContentResolver().query(getItemContentUri(), projection, null, null, null);

		if (cursor != null) // Ez vajon kell? 
			{
			cursor.moveToFirst();

			pillNameField.setText(cursor.getString(cursor.getColumnIndexOrThrow( field(PillsTable.NAME) )));

			// Always close the cursor
			cursor.close();
			}
		}

	@Override
	protected ContentValues getFieldsData()
		{
		Scribe.note("PillsEditFragment getFieldsData");

		String name = pillNameField.getText().toString();

	    ContentValues values = new ContentValues();
	    values.put( field(PillsTable.NAME), name);
	    
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
