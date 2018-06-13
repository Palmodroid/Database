package digitalgarden.mecsek.formtypes;

import android.content.Intent;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import digitalgarden.mecsek.scribe.Scribe;
import digitalgarden.mecsek.templates.GeneralControllActivity;
import digitalgarden.mecsek.templates.GeneralEditFragment;
import digitalgarden.mecsek.templates.GeneralListFragment;


public class ForeignKey
	{
	// A külső tábla, melyre hivatkozunk
	private Uri foreignTable;
	
    // A külső táblában hivatkozott elem id-je
    private long foreignId;
    
	// Ezeket a listener-eket kell értesíteni változás esetén
    // (vagyis ezek a ForeignTextField-ek)
    private Set<ForeignField> foreignFields;

    // A form, amelyhez a ForeignKey (és a hozzá tartozó Field-ek) kötődnek
	private GeneralEditFragment form;

	// ForeignKey és kapcsolódó mezők közös selectorCode-ja, vagyis a selectorActivity requestCode-ja
	private int selectorCode = -1;

	// És a selector adatai
	private Class<?> selectorActivity; 
	private String selectorTitle; 
	private TextView selectorOwner;
	
	// Ha az érték megváltozik, akkor ezen az interface-n keresztül jelezzük
	public static interface ForeignField 
		{
		void onValueChanged(long newId);
		void setOnTouchListener(View.OnTouchListener touchListener);
	    }
	
	// Konstruktor, alapértelmezetten nincs elem hozzárendelve
	// foreignTable: melyre a ForeignKey mutat
	public ForeignKey( Uri foreignTable ) 
		{
		foreignId = -1L;
		this.foreignTable = foreignTable;
		}

	// Set: értékadás a listener-ek értesítésével
	public void setValue( long newId ) 
		{
		foreignId = newId;
		for (ForeignField field:foreignFields) 
			{
			field.onValueChanged( foreignId );
	        }
	    }

	// Get: tábla lekérdezés
	public Uri getTable() 
		{
		return foreignTable;
	    }

	// Get: id lekérdezés
	public long getValue() 
		{
		return foreignId;
	    }

	// Létrehozás (EditFragment-tel együtt) után
	// először csatolni kell a form-hoz (setupFormLayout-ban)
	public void connect(final GeneralEditFragment form) 
		{
		this.form = form;
		// Összekapcsoláskor nullázzuk a korábbi kapcsolatokat
		// Ezt elvileg a DestroyView-ben kellene, de itt egyszerűbbnek tűnt.
		foreignFields = new HashSet<ForeignField>();
		// és új selectorCode-ot kérünk.
		// mivel a ForeignKey-ek mindig azonos sorban kérik, ezért ugyanaz a ForeignKey mindig ugyanazt az értéket kapja
		selectorCode = form.getCode();
		}

    // majd külön beállítjuk hozzá a selectort
    // selectorActivity - a megfelelő táblához tartozó GeneralControllActivity
    // selectorTitle - selector címének eleje
    // selectorOwner - a jelenlegi elemet leginkább jellemző TextView
    public void setupSelector(final Class<?> selectorActivity, final String selectorTitle, final TextView selectorOwner)
    	{
    	this.selectorActivity = selectorActivity;
    	this.selectorTitle = selectorTitle;
    	this.selectorOwner = selectorOwner;
    	}

	public GeneralEditFragment getForm()
		{
		return form;
		}
	
	public int getSelectorCode()
		{
		return selectorCode;
		}
	
	// Listener hozzáadása. Az éppen aktuális értékkel frissíti is a hozzáadott Listener-t
	// FONTOS! Ezt mindig a "túloldal" hívja meg!! (Vagyis a mező adatja hozzá magát)
	public void setForeignField( ForeignField field ) 
		{
	    // link csak akkor lehetséges, ha a ForeignKey már az űrlaphoz kötött!!
		// és a selector-t beállítottuk
		if (form == null || selectorActivity == null)
			{
			Scribe.error("Foreign Key was not connected to GeneralEditFragment or Selector was not set!");
			throw new IllegalArgumentException("Foreign Key was not connected to GeneralEditFragment or Selector was not set!"); 
			}
		
		foreignFields.add(field);
		field.onValueChanged( foreignId );
		
		// Beállítjuk, hogy érintésre a megfelelő selectorActivity elinduljon
		field.setOnTouchListener( new View.OnTouchListener()
			{
			@Override
			public boolean onTouch(View v, MotionEvent event)
				{
				if (event.getAction() == MotionEvent.ACTION_UP)
					{
					Scribe.note("ForeignTextField: Selector started!");
					Intent intent = new Intent( getForm().getActivity(), selectorActivity);
					intent.putExtra( GeneralControllActivity.TITLE, selectorTitle + selectorOwner.getText() );
					intent.putExtra( GeneralListFragment.SELECTED_ITEM, getValue() );
					getForm().startActivityForResult( intent, getSelectorCode() );
					}
				return true; // nem engedjük mást sem csinálni
				}
			});
	    }

	// ForeignKey ált. selectorActivity-ból való visszatérés során változik. 
	// Ezzel a metódussal nézhetjük meg, hogy visszatérés után a konkrét példánynak kell-e változnia
	public void checkReturningSelector(int selectorCode, long id)
		{
		if (this.selectorCode == selectorCode && id != getValue())
			{
			setValue(id);
			form.setEdited();
			}
		}
	}
