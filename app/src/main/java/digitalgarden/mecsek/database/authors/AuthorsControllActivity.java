package digitalgarden.mecsek.database.authors;


// res:
// http://stackoverflow.com/a/5796606


import digitalgarden.mecsek.templates.GeneralControllActivity;
import digitalgarden.mecsek.templates.GeneralEditFragment;
import digitalgarden.mecsek.templates.GeneralListFragment;

public class AuthorsControllActivity extends GeneralControllActivity
	implements AuthorsListFragment.OnListReturnedListener, AuthorsEditFragment.OnFinishedListener
	{

	@Override
	protected GeneralEditFragment createEditFragment()
		{
		return new AuthorsEditFragment();
		}


	@Override
	protected GeneralListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GeneralListFragment.SELECTED_ITEM, AuthorsListFragment.SELECT_DISABLED);
		return AuthorsListFragment.newInstance( initiallySelectedItem );
		}

	}
