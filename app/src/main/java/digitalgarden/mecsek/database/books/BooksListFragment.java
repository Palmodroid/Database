package digitalgarden.mecsek.database.books;


import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.database.authors.AuthorsTable;
import digitalgarden.mecsek.templates.GeneralListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.fieldFull;
import static digitalgarden.mecsek.database.DatabaseMirror.fieldFull_id;
import static digitalgarden.mecsek.database.DatabaseMirror.field_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.BOOKS;


public class BooksListFragment extends GeneralListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GeneralListFragment newInstance( long limit )
		{
		GeneralListFragment listFragmenet = new BooksListFragment();

		Bundle args = new Bundle();
		
		// args.putLong( SELECTED_ITEM , SELECT_DISABLED ); Nincs szelektálás!
		
		args.putLong( LIMITED_ITEM, limit );
		args.putString( LIMITED_COLUMN, fieldFull(BooksTable.AUTHOR_ID));
		args.putString( ORDERED_COLUMN, fieldFull(AuthorsTable.NAME));
		// args.putString( FILTERED_COLUMN, BooksTable.FULL_SEARCH);
		args.putStringArray( FILTERED_COLUMN, new String[] {fieldFull(AuthorsTable.SEARCH), fieldFull(BooksTable.SEARCH)});
		
		listFragmenet.setArguments(args);

		return listFragmenet;
		}

	protected int getLoaderId()
		{
		return table(BOOKS).id();
		}

	@Override
	protected Uri getContentUri()
		{
		return table(BOOKS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		String[] projection = new String[] {
				fieldFull_id(BOOKS),
				fieldFull(AuthorsTable.NAME),
				fieldFull(BooksTable.TITLE) };

		return projection;
		}
	
	@Override
	protected int getRowView()
		{
		return R.layout.book_list_row_view;
		}

	@Override
	protected String[] getFrom()
		{
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] {
				field(AuthorsTable.NAME),
				field(BooksTable.TITLE),
				field_id() };

		return from;
		}

	@Override
	protected int[] getTo()
		{
		// the XML defined views which the data will be bound to
		int[] to = new int[] {
				R.id.author,
				R.id.title,
				R.id.id };

		return to;
		}

	@Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.putNull( field(BooksTable.AUTHOR_ID) );
		values.put( field(BooksTable.TITLE), "Urania");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( field(BooksTable.AUTHOR_ID) );
		values.put( field(BooksTable.TITLE), "Elrontottam!");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( field(BooksTable.AUTHOR_ID) );
		values.put( field(BooksTable.TITLE), "Egri csillagok");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( field(BooksTable.AUTHOR_ID) );
		values.put( field(BooksTable.TITLE), "A Pál utcai fiúk");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( field(BooksTable.AUTHOR_ID) );
		values.put( field(BooksTable.TITLE), "Abigél");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( field(BooksTable.AUTHOR_ID) );
		values.put( field(BooksTable.TITLE), "Tüskevár");
		getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( field(BooksTable.AUTHOR_ID) );
		values.put( field(BooksTable.TITLE), "Ábel a rengetegben");
    	getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);

		values.putNull( field(BooksTable.AUTHOR_ID) );
		values.put( field(BooksTable.TITLE), "Példa Fibinek");
		getActivity().getContentResolver().insert( table(BOOKS).contentUri(), values);
		}

	}
