package digitalgarden.mecsek.database.pills;


// res:
// http://stackoverflow.com/a/5796606

import digitalgarden.mecsek.templates.GeneralControllActivity;
import digitalgarden.mecsek.templates.GeneralEditFragment;
import digitalgarden.mecsek.templates.GeneralListFragment;


public class PillsControllActivity extends GeneralControllActivity
	implements PillsListFragment.OnListReturnedListener, PillsEditFragment.OnFinishedListener
	{

	@Override
	protected GeneralEditFragment createEditFragment()
		{
		return new PillsEditFragment();
		}


	@Override
	protected GeneralListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GeneralListFragment.SELECTED_ITEM, PillsListFragment.SELECT_DISABLED);
		return PillsListFragment.newInstance( initiallySelectedItem );
		}

	}
