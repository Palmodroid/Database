package digitalgarden.mecsek.database.books;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.authors.AuthorsControllActivity;
import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.formtypes.EditTextField;
import digitalgarden.mecsek.formtypes.ForeignKey;
import digitalgarden.mecsek.formtypes.ForeignTextField;
import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GeneralEditFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;
import static digitalgarden.mecsek.database.library.LibraryDatabase.BOOKS;


public class BooksEditFragment extends GeneralEditFragment
	{
	private EditTextField editTextTitle;
	private ForeignKey authorId = new ForeignKey( table(AUTHORS).contentUri() );
	// Míg a címet a szöveg azonosítja, a szerzőt az id
	// Nem lehet null, ezért -1 azonosítja a null-t Ezt ellenőrizni kell!!
	private ForeignTextField foreignTextAuthor;
	
	
	@Override
	protected Uri getTableContentUri()
		{
		return table(BOOKS).contentUri();
		}

	@Override
	protected int getFormLayout()
		{
		return R.layout.book_edit_fragment_form;
		}
	
	@Override
	protected void setupFormLayout( View view )
		{
		Scribe.note("BooksEditFragment setupFormLayout");

		// EditTextField
        editTextTitle = (EditTextField) view.findViewById( R.id.edittext_title );
        editTextTitle.connect( this );
		
        // ForeignKey
        authorId.connect( this );
        authorId.setupSelector( AuthorsControllActivity.class,
        		getActivity().getString( R.string.select_author ),
        		editTextTitle );
        
        // ForeignTextField
       	foreignTextAuthor = (ForeignTextField) view.findViewById(R.id.edittext_author);
        foreignTextAuthor.link( authorId, field(AuthorsTable.NAME) );
		}

	@Override
	protected void setupFieldsData(long id)
		{
		Scribe.note("BooksEditFragment setupFieldsData");

		String[] projection = {
				field(BooksTable.AUTHOR_ID),
				field(BooksTable.TITLE) };
		Cursor cursor = getActivity().getContentResolver().query(getItemContentUri(), projection, null, null, null);

		if (cursor != null) // Ez vajon kell? 
			{
			cursor.moveToFirst();

			int column = cursor.getColumnIndexOrThrow( field(BooksTable.AUTHOR_ID) );
			if ( cursor.isNull( column ) )
				authorId.setValue( -1L );
			else
				authorId.setValue( cursor.getLong( column ) );
			editTextTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(  field(BooksTable.TITLE) )));

			// Always close the cursor
			cursor.close();
			}
		}

	@Override
	protected ContentValues getFieldsData()
		{
		Scribe.note("BooksEditFragment getFieldsData");
		
		String title = editTextTitle.getText().toString();

	    ContentValues values = new ContentValues();
	    if (authorId.getValue() >= 0)
	    	values.put( field(BooksTable.AUTHOR_ID), authorId.getValue());
	    else
	    	values.putNull( field(BooksTable.AUTHOR_ID) );
	    values.put( field(BooksTable.TITLE), title);

	    return values;
		}

	@Override
	protected void saveFieldData(Bundle data)
		{
		data.putLong("AUTHOR_ID", authorId.getValue() );
		}

	@Override
	protected void retrieveFieldData(Bundle data)
		{
		authorId.setValue( data.getLong( "AUTHOR_ID" ) );
		}

	@Override
	protected void checkReturningSelector(int requestCode, long selectedId)
		{
		authorId.checkReturningSelector( requestCode, selectedId );
		Scribe.note("Author id: " + selectedId + " was selected in onActivityResult");
		}
	}
