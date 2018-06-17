package digitalgarden.mecsek.database.medications;


// res:
// http://stackoverflow.com/a/5796606

import android.content.Intent;

import digitalgarden.mecsek.templates.GenericControllActivity;
import digitalgarden.mecsek.templates.GenericEditFragment;
import digitalgarden.mecsek.templates.GenericListFragment;


public class MedicationsControllActivity extends GenericControllActivity
	implements MedicationsListFragment.OnListReturnedListener, MedicationsEditFragment.OnFinishedListener
	{

	@Override
	protected GenericEditFragment createEditFragment()
		{
		return new MedicationsEditFragment();
		}


	@Override
	protected GenericListFragment createListFragment()
		{
		long pillIdLimit = getIntent().getLongExtra(GenericListFragment.LIMITED_ITEM, -1L);
		return MedicationsListFragment.newInstance( pillIdLimit );
		}

    // Ez ahhoz kell, hogy a Fragment megkapja a hívást
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
        super.onActivityResult(requestCode, resultCode, data);
        }
    }
