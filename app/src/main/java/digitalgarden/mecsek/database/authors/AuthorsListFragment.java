package digitalgarden.mecsek.database.authors;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.templates.GeneralListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.field;
import static digitalgarden.mecsek.database.DatabaseMirror.field_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;


public class AuthorsListFragment extends GeneralListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GeneralListFragment newInstance( long select )
		{
		GeneralListFragment listFragmenet = new AuthorsListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		args.putStringArray( FILTERED_COLUMN, new String[] { field(AuthorsTable.SEARCH) });
		args.putString( ORDERED_COLUMN, field(AuthorsTable.NAME) );

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

	
	protected int getLoaderId()
		{
		return table(AUTHORS).id();
		}

	@Override
	protected Uri getContentUri()
		{
		return table(AUTHORS).contentUri();
		}

	@Override
	protected String[] getProjection()
		{
		String[] projection = new String[] {
				field_id(),
				field(AuthorsTable.NAME)};

		return projection;
		}
	
	@Override
	protected int getRowView()
		{
		return R.layout.author_list_row_view;
		}

	@Override
	protected String[] getFrom()
		{
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] {
				field(AuthorsTable.NAME),
				field_id() };

		return from;
		}

	@Override
	protected int[] getTo()
		{
		// the XML defined views which the data will be bound to
		int[] to = new int[] {
			R.id.author,
			R.id.id };

		return to;
		}

	@Override
	protected void addExamples()
		{
		ContentValues values = new ContentValues();

		values.put( field(AuthorsTable.NAME), "Láng Attila D.");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( field(AuthorsTable.NAME), "Gárdonyi Géza");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( field(AuthorsTable.NAME), "Molnár Ferenc");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( field(AuthorsTable.NAME), "Szabó Magda");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( field(AuthorsTable.NAME), "Fekete István");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);
		}
	}
