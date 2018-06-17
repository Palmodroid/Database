package digitalgarden.mecsek.database.authors;


// res:
// http://stackoverflow.com/a/5796606


import digitalgarden.mecsek.templates.GenericControllActivity;
import digitalgarden.mecsek.templates.GenericEditFragment;
import digitalgarden.mecsek.templates.GenericListFragment;

public class AuthorsControllActivity extends GenericControllActivity
	implements AuthorsListFragment.OnListReturnedListener, AuthorsEditFragment.OnFinishedListener
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new AuthorsEditFragment();
		}


	@Override
	protected GenericListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericListFragment.SELECTED_ITEM, AuthorsListFragment.SELECT_DISABLED);
		return AuthorsListFragment.newInstance( initiallySelectedItem );
		}

	}
