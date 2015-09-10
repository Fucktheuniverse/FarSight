package ru.farsight;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.support.v4.app.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.math.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends ActionBarActivity{
    final static String fileName="check";
    public GoogleMap map; // Might be null if Google Play services APK is not available.
    public UiSettings uiSettings;
    String OverPaint="";
    String NewTileLoc="";
    private LocationManager locationManager;
    public Location location;
    String ReadingFile = "";
    String OurLoc = "";
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    private ProgressBar progressBar;// = (ProgressBar) findViewById(R.id.mana_bar);
   /* File file = new File(getFilesDir(),"OurLocation");
    File Tfile = new File(getFilesDir(),fileName);*/
    final static String OurLocation="OurLocation";
    public boolean ability = false;
    public int progress=50;
    TextView prog_mana;
    final static String ManaStore="MP";
    public String ManaPoints="";

    private DrawerLayout myDrawerLayout;
    private ListView myDrawerList;
    private ActionBarDrawerToggle myDrawerToggle;
    // navigation drawer title
    private CharSequence myDrawerTitle;
    // used to store app title
    private CharSequence myTitle;
    private String[] viewsNames;
    Button FarSightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LOG ", "onCreate Started");
        setContentView(R.layout.activity_maps);
        myTitle =  getTitle();
        myDrawerTitle = getResources().getString(R.string.menu);
        // load slide menu items
        viewsNames = getResources().getStringArray(R.array.map_views_array);
        myDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        myDrawerList = (ListView) findViewById(R.id.left_drawer);

        myDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, viewsNames));

        // enabling action bar app icon and behaving it as toggle button
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        myDrawerToggle = new ActionBarDrawerToggle(this, myDrawerLayout,
                R.string.open_menu,
                R.string.close_menu
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(myTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(myDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        myDrawerLayout.setDrawerListener(myDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }

        myDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        setUpMapIfNeeded();
        FarSightButton=(Button) findViewById(R.id.button_farsight);
        FarSightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FarSightButton.setPressed(true);
                if(progress-70>=0){
                    //progress+=70;
                    ability = true;}
                else {Toast.makeText(getApplicationContext(), "Not enough mana. Wait a bit or use a potion.", Toast.LENGTH_SHORT).show();
                    Log.d("LOG", "Not enough MANA!");
                }
            }
        });


    }




    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(
                AdapterView<?> parent, View view, int position, long id
        ) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new FirstFragment();
                break;
            case 1:
                fragment = new SecondFragment();
                break;
            case 2:
                this.finish();
                break;
            default:
                break;
        }

        if (fragment != null) {
            android.app.FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            myDrawerList.setItemChecked(position, true);
            myDrawerList.setSelection(position);
            setTitle(viewsNames[position]);
            myDrawerLayout.closeDrawer(myDrawerList);

        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if navigation drawer is opened, hide the action items
        boolean drawerOpen = myDrawerLayout.isDrawerOpen(myDrawerList);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_farsight).setVisible(false);
        /*menu.findItem(R.id.action_settings).setVisible(drawerOpen);
        menu.findItem(R.id.action_farsight).setVisible(drawerOpen);*/
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        myTitle = title;
        getSupportActionBar().setTitle(myTitle);
    }
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        myDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        myDrawerToggle.onConfigurationChanged(newConfig);
    }





    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LOG ", "onResume Started");
        progressBar = (ProgressBar) findViewById(R.id.mana_bar);
        prog_mana = (TextView) findViewById(R.id.prog_mana);
        if (readMP(ManaStore)!=null)
        {
            String[] mana=readMP(ManaStore).split(" ");
            int[] manamas = new int[mana.length];
            for (int i=0;i<mana.length;i++)
            {
                manamas[i]= Integer.valueOf(mana[i]);
            }
            progress=manamas[0];
            //progressBar.setMax(100);
            progressBar.setProgress(progress);
            prog_mana.setText(""+progress+" Mana");
        }
        setUpMapIfNeeded();
      //  onMapReady();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //What happening when Settings button pushed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (myDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        switch (id)
        {
            case(R.id.action_settings):
                this.finish();
                break;
            case (R.id.action_farsight):
                progress=100;
                ManaPoints=""+progress;
                SaveMP();
                progressBar.setProgress(progress);
                prog_mana.setText("" + progress + " Mana");
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //READ BLOCK
    public String readData(String fileName)
    {   Log.d("LOG", "Read " + ReadingFile + " started");

        try
        {
            //Log.d("LOG", "Try block started");
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(fileName)));
            String line;
            //Log.d("LOG", "Read buffer");
            while((line=reader.readLine())!=null)
            {
                Log.d("LOG", "Read successful");
                return line;
            }
        } catch (FileNotFoundException e) {
            Log.d("LOG", "File not Found!");
            e.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return "IOex";
        }
        return null;
    }
    public void SaveData()
    {   try
        {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(fileName, MODE_PRIVATE)));
            writer.write(NewTileLoc);
            writer.close();
            Log.d("LOG", "Writing completed");
        }
        catch(FileNotFoundException el)
        {el.printStackTrace();}
    catch (IOException ep) {ep.printStackTrace();}
    }

    @Override
    protected void onPause() {
        super.onPause();
   //     locationManager.removeUpdates(locationListener);
        Log.d("LOG ", "onPause computation started");
        ReadingFile = fileName + " First onPause";
        String[] str = readData(fileName).split(" ");
        double[] mas = new double[str.length];
        for (int i = 0; i < str.length; i++) {
            mas[i] = Double.valueOf(str[i]);

        }
        ReadingFile = OurLocation+" First onPause";
        if (readData(OurLocation)!=null&&readData(OurLocation)!="IOex") {
            ReadingFile = OurLocation + " if OurLocation not null";
            String[] ourLocate = readData(OurLocation).split(" ");

            double[] locmas = new double[ourLocate.length];
            for (int L = 0; L < ourLocate.length; L++) {
                locmas[L] = Double.valueOf(ourLocate[L]);
                // Log.d("LOG","Our Location read in massive");
            }
            Log.d("LOG", "check " + mas[0] + " " + mas[1]);
            ReadingFile = OurLocation + " show coordinates";
            Log.d("LOG", readData(OurLocation));
            for (int i = 0; i < mas.length; i += 2) {
                double XXX = mas[i];
                double YYY = mas[i + 1];
                double XLU = XXX;
                XLU -= 0.00462 * 1.13;
                double XRU = XXX;
                XRU += 0.00462 * 1.13;
                double XLD = XXX;
                XLD -= 0.00462 * 1.13;
                double XRD = XXX;
                XRD += 0.00462 * 1.13;
                double YLU = YYY;
                YLU += 0.0033372 * 1.142;
                double YRU = YYY;
                YRU += 0.0033372 * 1.142;
                double YLD = YYY;
                YLD -= 0.0033372 * 1.142;
                double YRD = YYY;
                YRD -= 0.0033372 * 1.142;
                for (int L = 0; L < locmas.length; L += 2) {
                    //Log.d("LOG ", "For Started");
                    if ((locmas[L] >= XLU && locmas[L + 1] <= YLU) &&
                            (locmas[L] <= XRU && locmas[L + 1] <= YRU) &&
                            (locmas[L] >= XLD && locmas[L + 1] >= YLD) &&
                            (locmas[L] <= XRD && locmas[L + 1] >= YRD)) {
                        Log.d("LOG", "" + mas[i + 1] + ", " + mas[i]);
                        mas[i] = 0;
                        mas[i + 1] = 0;
                        Log.d("LOG ", "Square checked");
                    }
                }
            }
            int last = locmas.length;
            for (int T = 0; T < mas.length; T++)
            {
                if (mas[T] != 0) {
                    NewTileLoc += mas[T] + " ";
                }
                if(mas[T]==0) Log.d("LOG ", "Elements "+T+" were null");
                if (T == mas.length - 1)
                {
                    Log.d("LOG ", "Last elem added to new massive");
                    SaveData();
                }
            }
            OurLoc = "" + locmas[last-2]+ " " + locmas[last-1] + " ";
            Log.d("LOG ", OurLoc);
            SaveOurLocation();
        }
    }
    public void SaveOurLocation()  {
        try
        {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(OurLocation, MODE_PRIVATE)));
            writer.write(OurLoc);
            writer.close();
            Log.d("LOG", "Our Location file now empty");
        }

        catch(
                FileNotFoundException el
                )

        {
            el.printStackTrace();
        } catch (IOException ep) {
            ep.printStackTrace();
        }

    }
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
            Log.d("LOG", "Locationlistener start");
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                Log.d("LOG", "Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                Log.d("LOG","Status: " + String.valueOf(status));
            }
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
           // RePaint(location);
           // Log.d("LOG ",formatLocation(location));
        }
        else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
           // RePaint(location);
            //Log.d("LOG ", formatLocation(location));
        }
    }

    private void checkEnabled() {
        Log.d("LOG ", "Enabled: "
                 + locationManager
                 .isProviderEnabled(LocationManager.GPS_PROVIDER));
        Log.d("LOG ", "Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }


    public void onMapReady() {
        map.setOnMapClickListener(OnClickMapSettings());
        Log.d("LOG ", "onMapReady Started");
        ReadingFile = fileName + " First OnMap";
        String[] str = readData(fileName).split(" ");
        double[] mas = new double[str.length];
        for (int i = 0; i < str.length; i++) {
            mas[i] = Double.valueOf(str[i]);

        }
        ReadingFile = OurLocation+" First OnMap";
        if (readData(OurLocation)!=null&&readData(OurLocation)!="IOex") {
            ReadingFile = OurLocation + " if OurLocation not null";
            String[] ourLocate = readData(OurLocation).split(" ");

            double[] locmas = new double[ourLocate.length];
            for (int L = 0; L < ourLocate.length; L++) {
                locmas[L] = Double.valueOf(ourLocate[L]);
                // Log.d("LOG","Our Location read in massive");
            }
            Log.d("LOG", "check " + mas[0] + " " + mas[1]);
            ReadingFile = OurLocation + " show coordinates";
            Log.d("LOG", readData(OurLocation));
            for (int i = 0; i < mas.length; i += 2) {
                double XXX = mas[i];
                double YYY = mas[i + 1];
                double XLU = XXX;
                XLU -= 0.00462 * 1.13;
                double XRU = XXX;
                XRU += 0.00462 * 1.13;
                double XLD = XXX;
                XLD -= 0.00462 * 1.13;
                double XRD = XXX;
                XRD += 0.00462 * 1.13;
                double YLU = YYY;
                YLU += 0.0033372 * 1.142;
                double YRU = YYY;
                YRU += 0.0033372 * 1.142;
                double YLD = YYY;
                YLD -= 0.0033372 * 1.142;
                double YRD = YYY;
                YRD -= 0.0033372 * 1.142;
                for (int L = 0; L < locmas.length; L += 2) {
                    //Log.d("LOG ", "For Started");
                    if ((locmas[L] >= XLU && locmas[L + 1] <= YLU) &&
                            (locmas[L] <= XRU && locmas[L + 1] <= YRU) &&
                            (locmas[L] >= XLD && locmas[L + 1] >= YLD) &&
                            (locmas[L] <= XRD && locmas[L + 1] >= YRD)) {
                        Log.d("LOG", "" + mas[i + 1] + ", " + mas[i]);
                        mas[i] = 0;
                        mas[i + 1] = 0;
                        Log.d("LOG ", "Square checked");
                    }
                }
            }
        }
        for(int i=0;i<mas.length;i+=2)
        {
                    if(mas[i]!=0)
                    {
                    double Y = mas[i];
                    double X = mas[i + 1];
                    LatLng NEWARK = new LatLng(X, Y);
                    GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                            .image(BitmapDescriptorFactory.fromResource(R.drawable.newfog))
                            .zIndex(1)
                            .position(NEWARK, 850f, 850f);
                    map.addGroundOverlay(newarkMap);
                    }
                    else
                    {Log.d("LOG","Tile "+i+" nulled");}
        }

        Log.d("LOG","Painting completed");
        Log.d("LOG", "Provider check");
    }

    private OnMapClickListener OnClickMapSettings() {
        return new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                double X = latLng.latitude;
                double Y = latLng.longitude;
            Log.d("LOG","Clicked on "+X+" "+Y);
                if(ability==true)
                {
                    OurLoc = readData(OurLocation)+Y+" "+X+" ";
                    Log.d("LOG", "Now " + OurLoc);
                    SaveOurLocation();
                    progress-=70;
                    ManaPoints=""+progress;
                    SaveMP();
                    progressBar.setProgress(progress);
                    prog_mana.setText("" + progress + " Mana");
                    ability =  false;
                }
            }
        };
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #map} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            Log.d("LOG ", "Map is NULL!!!");
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                Log.d("LOG ", "Map not null");
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        Log.d("LOG ", "setUpMap Started");

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
     /* float transp = 0.5f;
        LatLngBounds newarkBounds = new LatLngBounds(
                new LatLng(43.021234, 131.840579),       // South west corner
                new LatLng(43.226729, 132.100082));      // North east corner
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.skin))
                //.zIndex(0)
                //.transparency(transp)
                .positionFromBounds(newarkBounds);
        map.addGroundOverlay(newarkMap);
        CameraUpdateFactory.zoomTo(14);
*/
        if (readData(OurLocation)!= null&&readData(OurLocation)!="IOex") {

            ReadingFile = OurLocation + " if OurLocation not null";
            String[] ourLocate = readData(OurLocation).split(" ");
            double[] locmas = new double[2];
            for (int L = 0; L <2; L++) {
                int point = ourLocate.length-2+L;
                locmas[L] = Double.valueOf(ourLocate[point]);
                // Log.d("LOG","Our Location read in massive");
            }
            double XX = locmas[1];
            double YY = locmas[0];
            LatLng Vladivostok = new LatLng(XX, YY);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(Vladivostok, 17));
        }
        else
        {
            double XX = 43.133237;
            double YY = 131.941687;
            LatLng Vladivostok = new LatLng(XX, YY);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(Vladivostok, 10));
        }
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.markericon))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(43.114544, 131.945626))
                .title("VMC")
                .snippet("Vladivostok Marine College"));
            onMapReady();
        }


    public void SaveMP()
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(ManaStore, MODE_PRIVATE)));
            writer.write(ManaPoints);
            writer.close();
            Log.d("LOG", "Writing completed");
        }
        catch(FileNotFoundException el)
        {
            el.printStackTrace();
        }
        catch (IOException ep)
        {
            ep.printStackTrace();
        }
    }
    //Function allowing ro read data
    public String readMP(String MP)
    {   Log.d("LOG", "Read " + ManaStore + " started");

        try
        {
            //Log.d("LOG", "Try block started");
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(ManaStore)));
            String line;
            //Log.d("LOG", "Read buffer");
            while((line=reader.readLine())!=null)
            {
                Log.d("LOG", "You have "+line+" MP");
                return line;
            }
        } catch (FileNotFoundException e) {
            Log.d("LOG", "File not Found!");
            e.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return "IOex";
        }
        return null;
    }



public class MyTimerTask extends TimerTask {
    @Override
    public void run() {

        Log.d("LOG", "Thread active");

    }

    ;
}

}
