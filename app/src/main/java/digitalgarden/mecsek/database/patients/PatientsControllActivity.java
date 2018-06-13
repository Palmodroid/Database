package digitalgarden.mecsek.database.patients;


// res:
// http://stackoverflow.com/a/5796606

import digitalgarden.mecsek.templates.GeneralControllActivity;
import digitalgarden.mecsek.templates.GeneralEditFragment;
import digitalgarden.mecsek.templates.GeneralListFragment;

public class PatientsControllActivity extends GeneralControllActivity
	implements PatientsListFragment.OnListReturnedListener, PatientsEditFragment.OnFinishedListener
	{

	@Override
	protected GeneralEditFragment createEditFragment()
		{
		return new PatientsEditFragment();
		}


	@Override
	protected GeneralListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GeneralListFragment.SELECTED_ITEM, PatientsListFragment.SELECT_DISABLED);
		return PatientsListFragment.newInstance( initiallySelectedItem );
		}

	}
