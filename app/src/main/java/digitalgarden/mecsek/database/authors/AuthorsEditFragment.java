package digitalgarden.mecsek.database.authors;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.books.BooksControllActivity;
import digitalgarden.mecsek.formtypes.EditTextField;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GenericEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;


public class AuthorsEditFragment extends GenericEditFragment
	{
	private EditTextField nameField;

	@Override
	protected Uri getTableContentUri()
		{
		return table(AUTHORS).contentUri();
		}

	@Override
	protected int getFormLayout()
		{
		return 	R.layout.author_edit_fragment_form;
		}

	@Override
	protected void setupFormLayout( View view )
		{
		Scribe.note("AuthorsEditFragment setupFormLayout");


        nameField = (EditTextField) view.findViewById( R.id.edittext_name );
        nameField.connect( this );

    	setupListButton( BooksControllActivity.class,
    			getActivity().getString( R.string.button_books_list ), 
    			getActivity().getString( R.string.books_of ),
    			nameField );
		}

	@Override
	protected void setupFieldsData(long id)
		{
		Scribe.note("AuthorsEditFragment setupFieldsData");

		String[] projection = {
				column(AuthorsTable.NAME) };
		Cursor cursor = getActivity().getContentResolver().query(getItemContentUri(), projection, null, null, null);

		if (cursor != null) // Ez vajon kell? 
			{
			cursor.moveToFirst();

			nameField.setText(cursor.getString(cursor.getColumnIndexOrThrow( column(AuthorsTable.NAME) )));

			// Always close the cursor
			cursor.close();
			}
		}

	@Override
	protected ContentValues getFieldsData()
		{
		Scribe.note("AuthorsEditFragment getFieldsData");

		String name = nameField.getText().toString();

	    ContentValues values = new ContentValues();
	    values.put( column(AuthorsTable.NAME), name);
	    
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
