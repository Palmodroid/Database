package digitalgarden.mecsek.database.patients;


// res:
// http://stackoverflow.com/a/5796606

import digitalgarden.mecsek.templates.GenericControllActivity;
import digitalgarden.mecsek.templates.GenericEditFragment;
import digitalgarden.mecsek.templates.GenericListFragment;

public class PatientsControllActivity extends GenericControllActivity
	implements PatientsListFragment.OnListReturnedListener, PatientsEditFragment.OnFinishedListener
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new PatientsEditFragment();
		}


	@Override
	protected GenericListFragment createListFragment()
		{
		long initiallySelectedItem = getIntent().getLongExtra(GenericListFragment.SELECTED_ITEM, PatientsListFragment.SELECT_DISABLED);
		return PatientsListFragment.newInstance( initiallySelectedItem );
		}

	}
