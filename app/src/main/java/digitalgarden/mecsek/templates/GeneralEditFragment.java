package digitalgarden.mecsek.templates;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.scribe.Scribe;


public abstract class GeneralEditFragment extends Fragment
	{
	public final static String EDITED_ITEM = "edited item";
	public final static long NEW_ITEM = -1L;
	
	// Az űrlapot két adat azonosítja:
	// - a tábla neve: getTableContentUri()
	// - a sor azonosítója: getItemId() ((NEW_ITEM, ha üres űrlapról van szó))
	// !! Ez mindig konstans egy adott űrlapnál/EditFragment-nél!!
	// A konkrét sorra a getItemContentUri()-val is hivatkozhatunk
	protected abstract Uri getTableContentUri();
	
	protected long getItemId()
		{
		// Ez korábban egy külső változó is azonosította, így munkásabb, de nincs külön hivatkozás
        Bundle args = getArguments();
		return (args != null) ? args.getLong(EDITED_ITEM, NEW_ITEM) : NEW_ITEM;
		}
	
	protected Uri getItemContentUri()
		{
       	return Uri.parse( getTableContentUri() + "/" + getItemId());
		}


	// Leszármazottak által biztosított metódusok
	// A beépítésre kerülő űrlap azonosítóját adja vissza
	protected abstract int getFormLayout();

	// Az űrlap mezőinek megfelelő objektumok itt kerülnek létrehozásra, ill. összekapcsolásra az adatbázissal
	protected abstract void setupFormLayout( View view );

	// Az űrlap mezőit id alapján feltöltjük
	protected abstract void setupFieldsData( long id );
	
	// Az űrlap mezőinek értékét egy ContentValue-ba tesszük
	protected abstract ContentValues getFieldsData();
	
	// http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
	// alapján egy custom view is elmentheti az állapotát. (Még nem dolgoztam ki)
	// DE! Itt nem a customView-t, hanem a ForeignKey-t kell elmenteni, erre szolgál ez a két metódus 
	protected abstract void saveFieldData(Bundle data);
	protected abstract void retrieveFieldData(Bundle data);

	// ForeignKey-eket kell végigellenőrizni, melyikhez tartozó ForeignTextField adta ki az Activity hívást
    protected abstract void checkReturningSelector( int requestCode, long selectedId );


    // edited: értéke true-ra vált, ha valamelyik field-et módosítottuk
	// setEdited beállítja, isEdited lekérdezi. Törlés szükségtelen, hiszen nem vonjuk vissza a módosításokat
	// a beállítást az egyes Field-ek végzik el. 
	// Arra vigyázni kell, hogy a felhasználó csak onResumed állapotban állíthat be értéket 
	// viszont a meghívott Activity más állapotban is visszatérhet!
	private boolean edited = false;

	public void setEdited()
		{
		Scribe.note("General EDIT Fragment: EDITED was set to TRUE");
		edited = true;
		}
	
	public boolean isEdited()
		{
		return edited;
		}

	
	// A szerkesztés végén ide térünk vissza
	OnFinishedListener onFinishedListener;
	
	public interface OnFinishedListener 
		{
		public void onFinished();
		}
	
	@Override
    public void onAttach(Activity activity) 
    	{
    	super.onAttach(activity);

    	try 
        	{
        	onFinishedListener = (OnFinishedListener) activity;
        	} 
        catch (ClassCastException e) 
        	{
            throw new ClassCastException(activity.toString() + " must implement OnFinishedListener");
        	}
    	}

	@Override
    public void onDetach() 
    	{
    	super.onDetach();
    	
    	onFinishedListener = null;
    	}


	// Az egyes UI elemeket tartalmazó változók
	private Button buttonAdd;
	private Button buttonUpdate;
	private Button buttonList;
	private Button buttonCancel;
	
	private AlertDialog confirmationDialog;

	// Az osztály példányosítása után a getCode() minden egyes meghívásra új értéket ad vissza
	// Ez teszi lehetővé, hogy a ForeignKey miatti Activity hívások request kódja mindig megfeleljen
	private int codeGenerator = 0;
	
	public int getCode()
		{
		codeGenerator++;
		Scribe.note("General EDIT Fragment: Code generated: " + codeGenerator);
		return codeGenerator;
		}
	
	
	// Az űrlapot getFormLayout() alapján illeszti be
	// Az űrlap mezőkkel történő összekapcsolását setFormLayout() végzi el. (Ezt megelőzően codeGenerator-t nullázuk!)
	// Az egyes mezők viszont NEM itt kapnak alapértéket!!
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
		{
		Scribe.note("General EDIT Fragment onCreateView");
		
		// Az alapvető layout egy "stub"-ot tart fenn a form számára. Ezt itt töltjük fel tartalommal
        View view = inflater.inflate(R.layout.general_edit_fragment, container, false);
        ViewStub form = (ViewStub) view.findViewById(R.id.stub);
        form.setLayoutResource( getFormLayout() );
        form.inflate(); 

        buttonAdd = (Button) view.findViewById(R.id.button_add);
        buttonUpdate = (Button) view.findViewById(R.id.button_update);

        if ( getItemId() < 0)
        	{
        	Scribe.note(" Id < 0 -> ADD Button activated");
	        buttonAdd.setOnClickListener(new OnClickListener()
	    		{
	    		public void onClick(View view) 
	    			{
	    			addItem();
	    			} 
	    		});
	        buttonUpdate.setVisibility(View.GONE);
        	}
        else
        	{
        	Scribe.note(" Id valid -> UPDATE Button activated");
	        buttonUpdate.setOnClickListener(new OnClickListener()
	    		{
	    		public void onClick(View view) 
	    			{
	    			updateItem();
	    			} 
	    		});
	        buttonAdd.setVisibility(View.GONE);
        	}

        buttonList = (Button) view.findViewById(R.id.button_list);
        
        buttonCancel = (Button) view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new OnClickListener()
    		{
    		public void onClick(View view)
    			{
    			Scribe.note("General EDIT Fragment: CANCEL button");
    			cancelEdit();
    			}
    		});

        codeGenerator = 0;
		setupFormLayout( view );

        return view;
		}


	// Kilistázhatjuk egy tábla azon elemeit, melyek a mi elemünkre hivatkoznak
	// listingActivity: GeneralControllActivity megfelelő táblához tartozó leszármazottja
	// buttonTitle: mi kerüljön a gombra? (Eredetileg List)
	// listTitle: a címsor eleje
	// listOwner: a mi elemünket azonosító (legjobban jellemző) TextView
	// ?? Ez még nem a legtökéletesebb, mert mi van, ha több listát akarunk ??
	// LIMITED_ITEM_hez tartozik a COLUMN is, csak abból most csak egyetlen van
	protected void setupListButton( final Class<?> listingActivity, final String buttonTitle, final String listTitle, final TextView listOwner )
		{
    	Scribe.note("Genaral EDIT Fragment: ListButton was set: " + buttonTitle );

		buttonList.setVisibility( View.VISIBLE );
		buttonList.setText( buttonTitle );
		buttonList.setOnClickListener(new OnClickListener()
    		{
    		public void onClick(View view) 
    			{
    			Intent intent = new Intent(getActivity(), listingActivity);
    			intent.putExtra( GeneralControllActivity.TITLE, listTitle + listOwner.getText() );
    			intent.putExtra( GeneralListFragment.LIMITED_ITEM, getItemId() );
    			startActivity( intent );
    			} 
    		});
		}
	
	
	// Az egyes gombokért felelős akciók
	// Ha kellenek az adatok (add/update), azokat a getFieldsData() adja meg
	private void addItem()
		{
		Scribe.note("Genaral EDIT Fragment: ADD button");
        Activity activity = getActivity();
		if (activity != null) 
			{
			try
				{
		    	getActivity().getContentResolver().insert( getTableContentUri(), getFieldsData() );
				}
			catch (Exception e)
				{
				Toast.makeText(getActivity(), "ERROR: Add item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
				}
			onFinishedListener.onFinished();
			}
		else
			Scribe.note("IMPOSSIBLE! ACTIVITY MISSING!!!");
		}

	private void updateItem()
		{
		Scribe.note("Genaral EDIT Fragment: UPDATE button");
        Activity activity = getActivity();
		if (getItemId() >= 0 && activity != null)
			{
			try
				{
		    	getActivity().getContentResolver().update( getItemContentUri(), getFieldsData(), null, null);
				}
			catch (Exception e)
				{
				Toast.makeText(getActivity(), "ERROR: Update item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
				}
			onFinishedListener.onFinished();
			}
		else
			Scribe.note("ID < 0 or ACTIVITY MISSING!!!");
		}
	
	private void deleteItem()
		{
		Scribe.note("Genaral EDIT Fragment: DELETE menu");
        Activity activity = getActivity();
		if (getItemId() >= 0 && activity != null)
			{
			try
				{
		    	getActivity().getContentResolver().delete(getItemContentUri(), null, null);
				}
			catch (Exception e)
				{
				Toast.makeText(getActivity(), "ERROR: Delete item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
				}
			onFinishedListener.onFinished();
			}
		else
			Scribe.note("ID < 0 or ACTIVITY MISSING!!!");
		}

	// Ez az adatfel- és visszatöltés leglényegesebb része:
	// Ha edited==TRUE, akkor edited-del együtt az összes változónk érvényes, nincs teendő
	// 		(pl. amikor visszatérünk a ForeignKey miatt meghívott Activity-ből)
	// Különben: Ha savedInstanceState != null, akkor vannak elmentett értékeink, töltsük vissza őket
	// retrieveFieldData()
	//		(pl. elfordítás miatti újraindítás)
	// Különben: első indítás (vagy legalábbis nem volt edit), töltsük fel az alapértelmezett értékeket
	// setupFieldsData
	@Override
	public void onActivityCreated (Bundle savedInstanceState)
		{
		super. onActivityCreated(savedInstanceState);
		Scribe.note("Genaral EDIT Fragment onActivity Created");

    	// Itt kell jelezni, ha a Fragment rendelkezik menüvel
    	setHasOptionsMenu(true);
		
		// onActivityCreate onCreateView UTÁN kerül meghívásra !!
		// Itt adunk értéket az UI elemeknek (EditText-ről gondoskodik a program)

		// Az ok a kiválasztás volt. Minden OK, csak a kiválasztott elemet kell feltölteni
		if ( isEdited() )
			{
			Scribe.note( "Data was reserved: Id: " + getItemId() + ", isEdited: " + edited );
			// a feltöltés korábban megtörtént!
			}
		
		// Újraindítás történt, értékekeket kivesszük a rendelkezésre álló csomagból 
		else if (savedInstanceState != null)
			{
			// Korábbi megjegyzés:
			// Az add miatt ez újra meghívásra kerül, de nincs párja, vagyis bemenete, ezért lesz értéke mindig null.
			// Az alapértéket meg kell adni eredendőan, aztán itt csak átállítjuk.
			
			// KETSZER kerul ez a cucc meghivasra, de csak egyszer kap saved... erteket!
			edited = savedInstanceState.getBoolean("IS_EDITED");
			
			Scribe.note("Data from savedInstanceState retrieved: Id: " + getItemId() + ", isEdited: " + edited );
			retrieveFieldData( savedInstanceState );
			}
 
		else // alapértéket - elvileg csak itt kell beállítani
			{
	        if (getItemId() >= 0L)
	        	{
	    		setupFieldsData( getItemId() );
	        	}
			Scribe.note("Data set from Arguments: Id: " + getItemId() + ", isEdited: " + edited );
			}
		
       	}
			
	@Override
	public void onSaveInstanceState(Bundle outState)
		{
		super.onSaveInstanceState(outState);
		outState.putBoolean("IS_EDITED", edited);
		
		// Ez elvileg eredeti módon is jó, de így jobban összhangban van a párjával
		saveFieldData(outState);
		
		Scribe.note("General EDIT Fragment onSaveInst.: isEdited out:" + edited);
		}

	// A hagyományos módon megnyitott confirmationDialog-ot elfordításkor le kell választani!
    @Override
    public void onDestroyView() 
    	{
     	super.onDestroyView();

     	// Ha véletlenül meg van nyitva a megerősítő kérdés, akkor tüntessük el!
       	if (confirmationDialog != null)
    		{
    		Scribe.note("General EDIT Fragment: confirmationDialog was removed in onDestroyView!");
    		confirmationDialog.dismiss();
    		confirmationDialog = null;
    		}
    	}
    
    // Itt jelezzük, ha megszakítani kívánjuk a szerkesztést
    // isEdited() esetén egy dialogusablakban meg kell erősíteni a szándékunkat
    protected void cancelEdit()
    	{
    	Scribe.note("General EDIT Fragment: cancelEdit was started");
    	
        Activity activity = getActivity();
        if (activity == null)
        	{
        	Scribe.note("cancelEdit: Fragment's ACTIVITY MISSING!!!");
        	return;
        	}

		Scribe.note("CancelEdit: isEdited = " + isEdited() );

		if ( isEdited() )
			{
			if (confirmationDialog != null)
				confirmationDialog.dismiss();
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( getActivity() );
			alertDialogBuilder.setTitle( R.string.confirmation_title );
			// alertDialogBuilder.setMessage( "Click yes to exit!" );
			alertDialogBuilder.setCancelable(false);
			alertDialogBuilder.setPositiveButton( R.string.confirmation_yes, new DialogInterface.OnClickListener() 
				{
				public void onClick(DialogInterface dialog,int id) 
					{
					Scribe.note("Data was edited, user was asked, till exiting from EDIT Fragment...");
					onFinishedListener.onFinished();
					}
				});
			alertDialogBuilder.setNegativeButton( R.string.confirmation_no, new DialogInterface.OnClickListener() 
				{
				public void onClick(DialogInterface dialog,int id) 
					{
					Scribe.note("Data was edited, exit canceled, returning to EDIT Fragment...");

					dialog.dismiss(); // ez ide nem is kell talán...
					// kevésbé lényeges, de különben onDestroyView()-ban ismét dismiss-eli
					confirmationDialog = null;
					}
				});
			// create alert dialog
			confirmationDialog = alertDialogBuilder.create();
			// show it
			confirmationDialog.show();
			}
		else
			{
			Scribe.note("Data was not edited, exiting from EDIT Fragment...");
			onFinishedListener.onFinished();
			}
    	}

    // Két lehetőséget: Add as new ill. Delete csak a menüben ajánlunk fel
    // Add as new egyébként ugyanaz, mint az Add billentyű (csak ez Update esetén nem látszik.
	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
		{
		inflater.inflate(R.menu.general_edit_menu, menu);
		}
		
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    	{
        switch (item.getItemId()) 
        	{
        	case R.id.menu_add_as_new:
        		addItem();
	    		return true;

        	case R.id.menu_delete:
        		deleteItem();
	    		return true;
	    		
        	default:
            	return super.onOptionsItemSelected(item);
	        }
	    }

	// Ez a rész lehet, h. minden más ELŐTT kerül végrehajtásra! Ezért fontos edited-et TRUE-ra állítani
    // A ForeignTextField érintésére meghívásra kerül egy új Activity, ahol kiválaszthatjuk az új elemet
    // Ez azonban itt tér vissza, és a (TextField alapján megadott) requestCode alapján kell végigellenőrizni a ForeignKey mezőket
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
		{
		super.onActivityResult(requestCode, resultCode, data);
		Scribe.note("General EDIT Fragment onActivityResult");
		
		if ( resultCode == Activity.RESULT_OK )
			{
			long selectedId = data.getLongExtra(GeneralListFragment.SELECTED_ITEM, GeneralListFragment.SELECTED_NONE);
			checkReturningSelector( requestCode, selectedId );
			}
		}
   	}
