package digitalgarden.mecsek.database.books;

import android.content.Intent;

import digitalgarden.mecsek.templates.GeneralControllActivity;
import digitalgarden.mecsek.templates.GeneralEditFragment;
import digitalgarden.mecsek.templates.GeneralListFragment;


// res:
// http://stackoverflow.com/a/5796606


public class BooksControllActivity extends GeneralControllActivity
	implements BooksListFragment.OnListReturnedListener, BooksEditFragment.OnFinishedListener
	{

	@Override
	protected GeneralEditFragment createEditFragment()
		{
		return new BooksEditFragment();
		}


	@Override
	protected GeneralListFragment createListFragment()
		{
		long authorIdLimit = getIntent().getLongExtra(GeneralListFragment.LIMITED_ITEM, -1L);
		return BooksListFragment.newInstance( authorIdLimit );
		}
	
	// Ez ahhoz kell, hogy a Fragment megkapja a hívást
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
		{
		super.onActivityResult(requestCode, resultCode, data);
		}

	}
