package digitalgarden.mecsek.generic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import java.util.ArrayList;

import digitalgarden.mecsek.R;
import digitalgarden.mecsek.formtypes.EditTextField;
import digitalgarden.mecsek.formtypes.ForeignKey;
import digitalgarden.mecsek.formtypes.ForeignTextField;
import digitalgarden.mecsek.scribe.Scribe;

import static digitalgarden.mecsek.database.DatabaseMirror.column;
import static digitalgarden.mecsek.database.DatabaseMirror.table;


/**
 * [Main] Table -  a "fő" tábla, mely idegen (Foreign) táblákra hivatkozhat
 * Foreign Key -   a "fő" táblában lévő column, mely az idegen tábla egy adatsorára mutat.
 * Foreign Table - az idegen tábla. Ennek egy sorát jelöli ki a Key.
 * FONTOS!! A ForeignKey egy adatsort mutat meg, de nem tartalmazza a ForeignTable-t!
 * (Ezt csak az adatbázis struktúrájából ismerjük.)
 *
 * Az EDIT Fragment egy konkrét adatsor szerkesztését végzi. A View mezőit feltölti a kiválasztott
 * adatsor adataival, ill. a szerkesztés befejezésekor (UPDATE, ADD) a megváltozott mezőket vissza-
 * tölti az adatsorba.
 * Adatok előemelése és a mezők kitöltése: PULL DATA
 * Adatok visszahelyezése a mezők alapján: PUSH DATA
 *
 * EdiTextField -
 *      összeköt egy EditText mezőt egy Text típusú oszloppal
 * ForeignTextField -
 *      összeköt egy TextView mezőt egy Text típusú oszloppal, ForeignKey alapján
 *      A mező értéke a ForeignKey által kiválasztott adatsorból származik; közvetlenül nem
 *      változtatható meg. Érintésre a ForeignKey Selector-a indul el.
 * ForeignKey -
 *      Ténylegesen nem jelenik meg a mezőkben. A ForeignKey oszlop adatsorának megfelelő érték.
 *      Ennek alapján kerülnek kiválasztásra a ForeignTextField-ek
 *      Beállít egy Selector-t (LIST), ami segít kiválasztani a megfelelő sort.
 *
 * ??? Hol definiáljuk a Foreign Table-t, ill. egyáltalán van-e rá szükség ???
 * Igen, kell; mert nem a kapcsolt, hanem csak az idegen táblából keresi ki a megfelelő értéket.
 * Talán logikusabb a ForeignTextField-ben megadni, mint tábla-oszlop párt.
 * ^^^^
 * Ez lesz a megoldás!
 *
 * A Selector is ismeri a Foreign Table-t. (Mivel több selector is lehet, ezért a Table nem ismeri
 * a selectort) Ennek alapján viszont felesleges a ForeignTable definiálása, mert a Selector tartalmazza.
 * ??? Vajon lehet olyan helyzet, hogy nem kell Selector ???
 *
 * A msik probléma, hogy a Selector egy Activity, amiből a tábla csak akkor érhető el, ha van példánya.
 * Vagy static-nak kell definiálni, de akkor meg nem tudjuk kikényszeríteni a létrehozását, hacsak nem
 * egy throw utasítással.
 *
 */
public abstract class GenericEditFragment extends Fragment
	{
    public final static String EDITED_ITEM = "edited item";
    public final static long NEW_ITEM = -1L;

    private ArrayList<EditTextField> editTextFields = new ArrayList<>();

    private ArrayList<ForeignKey> foreignKeys = new ArrayList<>();

    // Az űrlapot két adat azonosítja:
    // - a tábla neve: getTableContentUri()
    // - a sor azonosítója: getItemId() ((NEW_ITEM, ha üres űrlapról van szó))
    // !! Ez mindig konstans egy adott űrlapnál/EditFragment-nél!!
    // A konkrét sorra a getItemContentUri()-val is hivatkozhatunk

    /**
     * @return index of the table
     */
    protected abstract int defineTableIndex();

    // Leszármazottak által biztosított metódusok
    // A beépítésre kerülő űrlap azonosítóját adja vissza
    protected abstract int defineFormLayout();

    // Az űrlap mezőinek megfelelő objektumok itt kerülnek létrehozásra, ill. összekapcsolásra az adatbázissal
    protected abstract void setupFormLayout();

    public EditTextField addEditTextField( int editTextFieldId, int columnIndex )
        {
        EditTextField editTextField = (EditTextField) view.findViewById( editTextFieldId );
        editTextField.connect( this, columnIndex );
        editTextFields.add( editTextField );

        return editTextField;
        }

    public ForeignKey addForeignKey( int foreignKeyIndex, Class<?> selectorActivity,
                                     String selectorTitle, TextView selectorTitleOwner )
        {
        ForeignKey foreignKey = new ForeignKey( foreignKeyIndex );
        foreignKey.connect( this );
        foreignKey.setupSelector( selectorActivity, selectorTitle, selectorTitleOwner );
        foreignKeys.add( foreignKey );
        return foreignKey;
        }

    /**
     * A textField-et összekapcsolja az idegen tábla idegen oszlopával. A sort a foreignKey
     * választja ki.
     * @param foreignKey
     * @param foreignTextFieldId
     * @param foreignTableIndex
     * @param foreignColumnIndex
     * @return a szöveges mező
     */
    public ForeignTextField addForeignTextField(ForeignKey foreignKey, int foreignTextFieldId,
                                                int foreignTableIndex, int foreignColumnIndex )
        {
        ForeignTextField foreignTextField = (ForeignTextField) view.findViewById( foreignTextFieldId );
        foreignTextField.link( foreignKey, foreignTableIndex, foreignColumnIndex );
        // Ezt teljes egészében a foreignKey kezeli
        return foreignTextField;
        }

    protected long getRowIndex()
        {
        // Ez korábban egy külső változó is azonosította, így munkásabb, de nincs külön hivatkozás
        Bundle args = getArguments();
        return (args != null) ? args.getLong(EDITED_ITEM, NEW_ITEM) : NEW_ITEM;
        }

    protected Uri getItemContentUri()
        {
        return Uri.parse( table(defineTableIndex()).contentUri() + "/" + getRowIndex());
        }

    protected void checkReturningSelector(int requestCode, long selectedId)
        {
        for (ForeignKey key : foreignKeys)
            {
            key.checkReturningSelector( requestCode, selectedId );
            }
        }

    protected void saveForeignKeyData(Bundle data)
        {
        for (ForeignKey key : foreignKeys)
            {
            data.putLong( column(key.getForeignKeyIndex()), key.getValue() );
            }
        }

    protected void retrieveForeignKeyData(Bundle data)
        {
        for (ForeignKey key : foreignKeys)
            {
            key.setValue( data.getLong( column(key.getForeignKeyIndex()) ) );
            }
        }

    // Az űrlap mezőit id alapján feltöltjük
    protected void pullData( )
        {
        Scribe.note("pullData");

        if (getRowIndex() < 0L)
            return; // Még nem létező adat, nem kell kitölteni

        ArrayList<String> projection = new ArrayList<>();
        for (EditTextField field : editTextFields)
            {
            projection.add( column( field.getFieldIndex() ));
            }
        for (ForeignKey key : foreignKeys)
            {
            projection.add( column( key.getForeignKeyIndex()) );
            }

        //https://stackoverflow.com/questions/4042434/converting-arrayliststring-to-string-in-java
        Cursor cursor = getActivity().getContentResolver().query( getItemContentUri(),
                projection.toArray(new String[0]), null, null, null );

        if (cursor != null) // Ez vajon kell?
            {
            cursor.moveToFirst();

            for (EditTextField field : editTextFields)
                {
                field.pullData(cursor);
                }
            for (ForeignKey key : foreignKeys)
                {
                key.pullData(cursor);
                }

            // Always close the cursor
            cursor.close();
            }
        }

    // Az űrlap mezőinek értékét egy ContentValue-ba tesszük
    protected ContentValues pushData()
        {
        Scribe.note("pushData");

        ContentValues values = new ContentValues();

        for (EditTextField field : editTextFields)
            {
            field.pushData( values );
            }
        for (ForeignKey key : foreignKeys)
            {
            key.pushData( values );
            }
        return values;
        }


    /**********************************************************************************************/


	// http://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
	// alapján egy custom view is elmentheti az állapotát. (Még nem dolgoztam ki)
	// DE! Itt nem a customView-t, hanem a ForeignKey-t kell elmenteni, erre szolgál ez a két metódus 
	//protected abstract void saveForeignKeyData(Bundle data);
	//protected abstract void retrieveForeignKeyData(Bundle data);
    // Ld. fent

	// ForeignKey-eket kell végigellenőrizni, melyikhez tartozó ForeignTextField adta ki az Activity hívást
//    protected abstract void checkReturningSelector( int requestCode, long selectedId );

    // edited: értéke true-ra vált, ha valamelyik column-et módosítottuk
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

    // Ha EDIT befejezte a ténykedését, akkor mindenképp itt tér vissza
    // Most csak az ADD adja vissza a létrehozott item id-jét, hogy
    // SELECT mode-ban kiléphessünk. A többi -1L lesz
	public interface OnFinishedListener 
		{
		public void onFinished( long rowId );
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

    private View view;

	// Az űrlapot defineFormLayout() alapján illeszti be
	// Az űrlap mezőkkel történő összekapcsolását setFormLayout() végzi el. (Ezt megelőzően codeGenerator-t nullázuk!)
	// Az egyes mezők viszont NEM itt kapnak alapértéket!!
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
		{
		Scribe.note("General EDIT Fragment onCreateView");
		
		// Az alapvető layout egy "stub"-ot tart fenn a form számára. Ezt itt töltjük fel tartalommal
        view = inflater.inflate(R.layout.general_edit_fragment, container, false);
        ViewStub form = (ViewStub) view.findViewById(R.id.stub);
        form.setLayoutResource( defineFormLayout() );
        form.inflate(); 

        buttonAdd = (Button) view.findViewById(R.id.button_add);
        buttonUpdate = (Button) view.findViewById(R.id.button_update);

        if ( getRowIndex() < 0)
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
		setupFormLayout();

        return view;
		}


	// Kilistázhatjuk egy tábla azon elemeit, melyek a mi elemünkre hivatkoznak
	// listingActivity: GenericControllActivity megfelelő táblához tartozó leszármazottja
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
    			intent.putExtra( GenericControllActivity.TITLE, listTitle + listOwner.getText() );
    			intent.putExtra( GenericListFragment.LIMITED_ITEM, getRowIndex() );
    			startActivity( intent );
    			} 
    		});
		}
	
	
	// Az egyes gombokért felelős akciók
	// Ha kellenek az adatok (add/update), azokat a pushData() adja meg
	private void addItem()
		{
		Scribe.note("Genaral EDIT Fragment: ADD button");
        Activity activity = getActivity();
        long rowId = -1L;
		if (activity != null) 
			{
			try
				{
		    	Uri uri = getActivity().getContentResolver().insert( table(defineTableIndex()).contentUri(), pushData() );
                rowId = ContentUris.parseId(uri);
				}
			catch (Exception e)
				{
				Toast.makeText(getActivity(), "ERROR: Add item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
				}
            Scribe.debug("ROw ID of inserted item is: " + rowId);
            onFinishedListener.onFinished( rowId );
			}
		else
			Scribe.note("IMPOSSIBLE! ACTIVITY MISSING!!!");
		}

	private void updateItem()
		{
		Scribe.note("Genaral EDIT Fragment: UPDATE button");
        Activity activity = getActivity();
		if (getRowIndex() >= 0 && activity != null)
			{
			try
				{
		    	getActivity().getContentResolver().update( getItemContentUri(), pushData(), null, null);
				}
			catch (Exception e)
				{
				Toast.makeText(getActivity(), "ERROR: Update item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
				}
			onFinishedListener.onFinished(-1L);
			}
		else
			Scribe.note("ID < 0 or ACTIVITY MISSING!!!");
		}
	
	private void deleteItem()
		{
		Scribe.note("Genaral EDIT Fragment: DELETE menu");
        Activity activity = getActivity();
		if (getRowIndex() >= 0 && activity != null)
			{
			try
				{
		    	getActivity().getContentResolver().delete(getItemContentUri(), null, null);
				}
			catch (Exception e)
				{
				Toast.makeText(getActivity(), "ERROR: Delete item (" + e.toString() + ")", Toast.LENGTH_SHORT).show();
				}
			onFinishedListener.onFinished(-1L);
			}
		else
			Scribe.note("ID < 0 or ACTIVITY MISSING!!!");
		}

	// Ez az adatfel- és visszatöltés leglényegesebb része:
	// Ha edited==TRUE, akkor edited-del együtt az összes változónk érvényes, nincs teendő
	// 		(pl. amikor visszatérünk a ForeignKey miatt meghívott Activity-ből)
	// Különben: Ha savedInstanceState != null, akkor vannak elmentett értékeink, töltsük vissza őket
	// retrieveForeignKeyData()
	//		(pl. elfordítás miatti újraindítás)
	// Különben: első indítás (vagy legalábbis nem volt edit), töltsük fel az alapértelmezett értékeket
	// pullData
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
			Scribe.note( "Data was reserved: Id: " + getRowIndex() + ", isEdited: " + edited );
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
			
			Scribe.note("Data from savedInstanceState retrieved: Id: " + getRowIndex() + ", isEdited: " + edited );
			retrieveForeignKeyData( savedInstanceState );
			}
 
		else // alapértéket - elvileg csak itt kell beállítani
			{
            pullData();
			Scribe.note("Data set from Arguments: Id: " + getRowIndex() + ", isEdited: " + edited );
			}
		
       	}
			
	@Override
	public void onSaveInstanceState(Bundle outState)
		{
		super.onSaveInstanceState(outState);
		outState.putBoolean("IS_EDITED", edited);
		
		// Ez elvileg eredeti módon is jó, de így jobban összhangban van a párjával
		saveForeignKeyData(outState);
		
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
					onFinishedListener.onFinished( -1L );
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
			onFinishedListener.onFinished( -1L );
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
			long selectedId = data.getLongExtra(GenericListFragment.SELECTED_ITEM, GenericListFragment.SELECTED_NONE);
			checkReturningSelector( requestCode, selectedId );
			}
		}
   	}
