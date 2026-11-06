
package com.example.listprocessor;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    ItemList the_list;

    private TextView tv;

    // Sample web data file URL:
    // https://web.njit.edu/~wh282/it114/project1.txt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        tv = findViewById(R.id.text_main);
        tv.setText("Cruise list is empty. Choose an option from the menu.");

        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .permitAll()
                        .build()
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void showInput(String title, String hint,
                           java.util.function.Consumer<String> action) {

        EditText input = new EditText(this);
        input.setHint(hint);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK",
                        (d, w) -> action.accept(
                                input.getText().toString().trim()))
                .show();
    }

    private void message(String s) {
        Snackbar.make(
                findViewById(R.id.main),
                s,
                Snackbar.LENGTH_LONG
        ).show();
    }

    private Cruise readCruise(BufferedReader br) throws Exception {
        return readCruise(br, br.readLine());
    }

    private Cruise readCruise(BufferedReader br,
                              String firstLine) throws Exception {

        if (firstLine == null) {
            throw new Exception("Missing cruise data");
        }

        String shipName = br.readLine();
        String code = br.readLine();
        String region = br.readLine();
        String length = br.readLine();
        String price = br.readLine();
        String gratuity = br.readLine();

        if (shipName == null || code == null || region == null ||
                length == null || price == null || gratuity == null) {
            throw new Exception("Incomplete cruise record");
        }

        Cruise c = new Cruise();

        c.setCruiseLine(firstLine.trim());
        c.setShipName(shipName.trim());
        c.setCode(code.trim());
        c.setRegion(region.trim());
        c.setLength(Integer.parseInt(length.trim()));
        c.setPrice(Double.parseDouble(price.trim()));
        c.setGratuity(Double.parseDouble(gratuity.trim()));

        if (c.getLength() <= 0 || c.getCode().isEmpty()) {
            throw new Exception("Invalid cruise data");
        }

        return c;
    }

    // OPTION 1: Display cruise list
    public void onOption1(MenuItem i) {

        if (the_list.isEmpty()) {
            tv.setText("No cruises in the list.");
            return;
        }

        tv.setText("");

        for (Cruise c : the_list) {
            tv.append(c.getCruiseLine() + ", "
                    + c.getShipName() + ", "
                    + c.getCode() + ", $"
                    + String.format(Locale.US, "%.2f", c.getPrice())
                    + "\n");
        }
    }

    // OPTION 2: Load cruises from web file
    public void onOption2(MenuItem i) {

        showInput("Load cruises from web file",
                "Enter full URL", value -> {

                    try {
                        if (value.isEmpty()) {
                            throw new Exception("URL cannot be empty");
                        }

                        URL url = new URL(value);

                        java.util.LinkedList<Cruise> loaded =
                                new java.util.LinkedList<>();

                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(url.openStream()))) {

                            String first;

                            while ((first = br.readLine()) != null) {
                                if (first.trim().isEmpty()) {
                                    continue;
                                }

                                loaded.add(readCruise(br, first));
                            }
                        }

                        if (loaded.isEmpty()) {
                            throw new Exception("File contained no cruise data");
                        }

                        // Replace the current list only after successful loading.
                        the_list.clear();
                        the_list.addAll(loaded);

                        tv.setText("Loaded " + the_list.size() + " cruises.");

                    } catch (Exception e) {
                        message("Load error: "
                                + e.getClass().getSimpleName()
                                + ": " + e.getMessage());
                    }
                });
    }

    // OPTION 3: Add cruise from assets file
    public void onOption3(MenuItem i) {

        showInput("Add cruise from assets",
                "Enter assets filename", value -> {

                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(getAssets().open(value)))) {

                        Cruise c = readCruise(br);

                        if (findCruise(c.getCode()) != null) {
                            message("That cruise code already exists.");
                            return;
                        }

                        the_list.add(c);

                        tv.setText("Added " + c.getCruiseLine()
                                + " " + c.getShipName());

                    } catch (Exception e) {
                        message("Could not read assets file: " + e.getMessage());
                    }
                });
    }

    private Cruise findCruise(String code) {

        for (Cruise c : the_list) {
            if (c.getCode().equalsIgnoreCase(code)) {
                return c;
            }
        }

        return null;
    }

    // OPTION 4: Show cruise details
    public void onOption4(MenuItem i) {

        showInput("Show cruise details",
                "Enter cruise code", code -> {

                    Cruise c = findCruise(code);

                    if (c == null) {
                        message("Cruise code not found.");
                        return;
                    }

                    tv.setText("Cruise line: " + c.getCruiseLine()
                            + "\nShip name: " + c.getShipName()
                            + "\nCode: " + c.getCode()
                            + "\nRegion: " + c.getRegion()
                            + "\nLength: " + c.getLength() + " days"
                            + "\nPrice: $" + String.format(
                            Locale.US, "%.2f", c.getPrice())
                            + "\nTotal gratuity: $" + String.format(
                            Locale.US, "%.2f", c.getTotalGratuity())
                            + "\nTotal cost: $" + String.format(
                            Locale.US, "%.2f", c.getTotalCost()));
                });
    }

    // OPTION 5: Remove cruise
    public void onOption5(MenuItem i) {

        showInput("Remove a cruise",
                "Enter cruise code", code -> {

                    Cruise c = findCruise(code);

                    if (c == null) {
                        message("Cruise code not found.");
                        return;
                    }

                    the_list.remove(c);
                    tv.setText("Removed cruise " + code + ".");
                });
    }

    // OPTION 6: Most expensive cruise per day
    public void onOption6(MenuItem i) {

        if (the_list.isEmpty()) {
            message("The list is empty.");
            return;
        }

        Cruise most = the_list.getFirst();

        for (Cruise c : the_list) {

            if ((double) c.getPrice() / c.getLength()
                    > (double) most.getPrice() / most.getLength()) {
                most = c;
            }
        }

        tv.setText("Most expensive per day:\n"
                + most.getCruiseLine() + ", "
                + most.getShipName() + ", "
                + most.getCode()
                + "\nPer-day cost: $"
                + String.format(Locale.US, "%.2f",
                (double) most.getPrice() / most.getLength()));
    }

    // OPTION 7: Average cruise length
    public void onOption7(MenuItem i) {

        if (the_list.isEmpty()) {
            message("The list is empty.");
            return;
        }

        double sum = 0;

        for (Cruise c : the_list) {
            sum += c.getLength();
        }

        tv.setText("Average cruise length: "
                + String.format(Locale.US, "%.1f",
                sum / the_list.size())
                + " days");
    }

    // OPTION 8: Average per-day price
    public void onOption8(MenuItem i) {

        if (the_list.isEmpty()) {
            message("The list is empty.");
            return;
        }

        double prices = 0;
        double days = 0;

        for (Cruise c : the_list) {
            prices += c.getPrice();
            days += c.getLength();
        }

        if (days <= 0) {
            message("Invalid cruise lengths.");
            return;
        }

        tv.setText("Average per-day price: $"
                + String.format(Locale.US, "%.2f", prices / days));
    }
}
