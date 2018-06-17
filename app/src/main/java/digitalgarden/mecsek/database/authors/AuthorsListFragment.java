package digitalgarden.mecsek.database.authors;

import android.content.ContentValues;
import android.os.Bundle;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.templates.GenericListFragment;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.column_id;
import static digitalgarden.mecsek.database.DatabaseMirror.table;
import static digitalgarden.mecsek.database.library.LibraryDatabase.AUTHORS;


public class AuthorsListFragment extends GenericListFragment
	{
	// static factory method
	// http://www.androiddesignpatterns.com/2012/05/using-newinstance-to-instantiate.html
	public static GenericListFragment newInstance(long select )
		{
		GenericListFragment listFragmenet = new AuthorsListFragment();
	
		Bundle args = new Bundle();

		args.putLong( SELECTED_ITEM, select );
		// args.putString( LIMITED_COLUMN, null); Sem ez, sem LIMITED_ITEM nem kell!

		args.putStringArray( FILTERED_COLUMN, new String[] { column(AuthorsTable.SEARCH) });
		args.putString( ORDERED_COLUMN, column(AuthorsTable.NAME) );

		listFragmenet.setArguments(args);
		
		return listFragmenet;
		}

    @Override
    protected int defineTableIndex()
        {
        return AUTHORS;
        }

	@Override
	protected String[] getProjection()
		{
		String[] projection = new String[] {
				column_id(),
				column(AuthorsTable.NAME)};

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
				column(AuthorsTable.NAME),
				column_id() };

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

		values.put( column(AuthorsTable.NAME), "Láng Attila D.");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( column(AuthorsTable.NAME), "Gárdonyi Géza");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( column(AuthorsTable.NAME), "Molnár Ferenc");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( column(AuthorsTable.NAME), "Szabó Magda");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);

		values.put( column(AuthorsTable.NAME), "Fekete István");
		getActivity().getContentResolver().insert( table(AUTHORS).contentUri(), values);
		}
	}
