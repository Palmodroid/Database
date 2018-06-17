package digitalgarden.mecsek.database.pills;


// res:
// http://stackoverflow.com/a/5796606

import digitalgarden.mecsek.templates.GenericControllActivity;
import digitalgarden.mecsek.templates.GenericEditFragment;
import digitalgarden.mecsek.templates.GenericListFragment;


public class PillsControllActivity extends GenericControllActivity
	implements PillsListFragment.OnListReturnedListener, PillsEditFragment.OnFinishedListener
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new PillsEditFragment();
		}


	@Override
	protected GenericListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericListFragment.SELECTED_ITEM, PillsListFragment.SELECT_DISABLED);
		return PillsListFragment.newInstance( initiallySelectedItem );
		}

	}
