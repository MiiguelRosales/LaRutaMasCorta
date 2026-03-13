package com.example.larutamascorta;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Graph graph;
    private Spinner spinnerOrigen, spinnerDestino;
    private View cardResultado;
    private TextView tvDistanciaInfo, tvParadasInfo;
    private TextView tvTiempoInfo, tvCostoInfo, tvTipoCaminoInfo, tvCombustibleInfo;
    private LinearLayout itineraryContainer;
    private MapView mapView;
    private ScrollView scrollView;

    private static final double AVG_SPEED_KMH = 82.0;
    private static final double STOP_MINUTES = 12.0;
    private static final double FUEL_EFFICIENCY_KM_PER_L = 12.5;
    private static final double FUEL_PRICE_MXN_PER_L = 24.20;
    private static final double CO2_KG_PER_L = 2.31;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buildGraph();
        setupViews();
    }

    private void buildGraph() {
        graph = new Graph();
        String[] cities = {
            "Aguascalientes", "Ciudad de Mexico", "Guadalajara",
            "Leon", "Merida", "Monterrey", "Morelia",
            "Oaxaca", "Puebla", "Queretaro",
            "San Luis Potosi", "Toluca", "Veracruz"
        };
        for (String city : cities) graph.addNode(city);

        graph.addEdge("Ciudad de Mexico", "Puebla",          135);
        graph.addEdge("Ciudad de Mexico", "Toluca",           65);
        graph.addEdge("Ciudad de Mexico", "Queretaro",       220);
        graph.addEdge("Ciudad de Mexico", "Morelia",         310);
        graph.addEdge("Ciudad de Mexico", "Veracruz",        420);
        graph.addEdge("Puebla",           "Oaxaca",          340);
        graph.addEdge("Puebla",           "Veracruz",        280);
        graph.addEdge("Queretaro",        "Leon",            150);
        graph.addEdge("Queretaro",        "San Luis Potosi", 210);
        graph.addEdge("Queretaro",        "Morelia",         200);
        graph.addEdge("Leon",             "Aguascalientes",  100);
        graph.addEdge("Leon",             "Guadalajara",     185);
        graph.addEdge("San Luis Potosi",  "Aguascalientes",  120);
        graph.addEdge("San Luis Potosi",  "Monterrey",       440);
        graph.addEdge("Aguascalientes",   "Guadalajara",     200);
        graph.addEdge("Guadalajara",      "Morelia",         330);
        graph.addEdge("Morelia",          "Toluca",          150);
        graph.addEdge("Oaxaca",           "Veracruz",        350);
        graph.addEdge("Veracruz",         "Merida",          720);
        graph.addEdge("Monterrey",        "Merida",         1500);
    }

    private void setupViews() {
        spinnerOrigen   = findViewById(R.id.spinnerOrigen);
        spinnerDestino  = findViewById(R.id.spinnerDestino);
        MaterialButton btnCalcular = findViewById(R.id.btnCalcular);
        cardResultado   = findViewById(R.id.cardResultado);
        itineraryContainer = findViewById(R.id.itineraryContainer);
        tvDistanciaInfo = findViewById(R.id.tvDistanciaInfo);
        tvParadasInfo   = findViewById(R.id.tvParadasInfo);
        tvTiempoInfo    = findViewById(R.id.tvTiempoInfo);
        tvCostoInfo     = findViewById(R.id.tvCostoInfo);
        tvTipoCaminoInfo = findViewById(R.id.tvTipoCaminoInfo);
        tvCombustibleInfo = findViewById(R.id.tvCombustibleInfo);
        mapView         = findViewById(R.id.mapView);
        scrollView      = findViewById(R.id.main);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setupMap();

        List<String> cities = graph.getNodes();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigen.setAdapter(adapter);
        spinnerDestino.setAdapter(adapter);

        if (cities.size() > 1) spinnerDestino.setSelection(1);
        btnCalcular.setOnClickListener(v -> calcularRuta());
    }

    private void setupMap() {
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(5.5);
        mapController.setCenter(CityCoordinates.getMexicoCenterPoint());

        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                scrollView.requestDisallowInterceptTouchEvent(true);
            }
            return false;
        });
    }

    private void calcularRuta() {
        String origen  = (String) spinnerOrigen.getSelectedItem();
        String destino = (String) spinnerDestino.getSelectedItem();

        if (origen.equals(destino)) {
            cardResultado.setVisibility(View.GONE);
            return;
        }

        Graph.Result result = graph.dijkstra(origen, destino);
        if (result.path.isEmpty()) {
            cardResultado.setVisibility(View.GONE);
            return;
        }

        cardResultado.setVisibility(View.VISIBLE);
        tvDistanciaInfo.setText(result.totalDistance + " km");
        tvParadasInfo.setText(String.valueOf(result.path.size()));

        int segments = Math.max(1, result.path.size() - 1);
        int intermediateStops = Math.max(0, result.path.size() - 2);
        String roadType = estimateRoadType(result.totalDistance, segments);
        double travelHours = estimateTravelHours(result.totalDistance, intermediateStops);
        double fuelLiters = estimateFuelLiters(result.totalDistance);
        double tripCostMxn = estimateTripCost(result.totalDistance, roadType, fuelLiters);
        double co2Kg = estimateCo2Kg(fuelLiters);

        tvTiempoInfo.setText(formatDuration(travelHours));
        tvCostoInfo.setText(String.format(Locale.getDefault(), "$%.0f MXN", tripCostMxn));
        tvTipoCaminoInfo.setText(roadType);
        tvCombustibleInfo.setText(String.format(Locale.getDefault(), "%.1f L · %.1f kg CO2", fuelLiters, co2Kg));

        renderItineraryTimeline(result.path);
        drawRouteOnMap(result.path);
        
        // Scroll suave al resultado
        cardResultado.post(() -> scrollView.smoothScrollTo(0, cardResultado.getTop()));
    }

    private void renderItineraryTimeline(List<String> path) {
        itineraryContainer.removeAllViews();

        for (int i = 0; i < path.size(); i++) {
            String city = path.get(i);
            boolean isOrigin = i == 0;
            boolean isDestination = i == path.size() - 1;
            int dotColor = isOrigin
                    ? ContextCompat.getColor(this, R.color.primary)
                    : (isDestination
                    ? ContextCompat.getColor(this, R.color.secondary)
                    : ContextCompat.getColor(this, R.color.accent));

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            row.setLayoutParams(rowParams);
            if (!isDestination) {
                rowParams.bottomMargin = dp(12);
            }

            LinearLayout rail = new LinearLayout(this);
            rail.setOrientation(LinearLayout.VERTICAL);
            rail.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams railParams = new LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.MATCH_PARENT);
            rail.setLayoutParams(railParams);

            View dot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(12), dp(12));
            dotParams.topMargin = dp(14);
            dot.setLayoutParams(dotParams);
            dot.setBackground(createCircleDrawable(dotColor, Color.WHITE, dp(2)));
            rail.addView(dot);

            if (!isDestination) {
                View connector = new View(this);
                LinearLayout.LayoutParams connectorParams = new LinearLayout.LayoutParams(dp(2), dp(72));
                connectorParams.topMargin = dp(8);
                connector.setLayoutParams(connectorParams);
                connector.setBackgroundColor(adjustAlpha(ContextCompat.getColor(this, R.color.text_sub), 0.22f));
                rail.addView(connector);
            }

            MaterialCardView cityCard = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            cityCard.setLayoutParams(cardParams);
            cityCard.setRadius(dp(14));
            cityCard.setCardElevation(0f);
            cityCard.setStrokeWidth(dp(1));

            if (isOrigin) {
                cityCard.setCardBackgroundColor(Color.parseColor("#EEF2FF"));
                cityCard.setStrokeColor(Color.parseColor("#D2DAFF"));
            } else if (isDestination) {
                cityCard.setCardBackgroundColor(Color.parseColor("#FFF3E6"));
                cityCard.setStrokeColor(Color.parseColor("#FFE0BF"));
            } else {
                cityCard.setCardBackgroundColor(Color.WHITE);
                cityCard.setStrokeColor(Color.parseColor("#E6E9F2"));
            }

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(dp(14), dp(12), dp(14), dp(12));

            TextView cityName = new TextView(this);
            cityName.setText(city);
            cityName.setTextSize(21f / getResources().getDisplayMetrics().scaledDensity);
            cityName.setTypeface(Typeface.DEFAULT_BOLD);
            cityName.setTextColor(ContextCompat.getColor(this, R.color.text_main));
            content.addView(cityName);

            TextView chip = new TextView(this);
            chip.setText(isOrigin ? getString(R.string.tag_origen) : (isDestination ? getString(R.string.tag_destino) : getString(R.string.tag_parada)));
            chip.setTextSize(11f);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setTextColor(isDestination
                    ? ContextCompat.getColor(this, R.color.secondary)
                    : ContextCompat.getColor(this, R.color.primary));
            chip.setPadding(dp(8), dp(4), dp(8), dp(4));
            chip.setBackground(createPillDrawable(
                    isDestination ? Color.parseColor("#FFF0E1") : Color.parseColor("#E8EEFF"),
                    isDestination ? Color.parseColor("#FFD7B0") : Color.parseColor("#C9D8FF")
            ));
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            chipParams.topMargin = dp(8);
            chip.setLayoutParams(chipParams);
            content.addView(chip);

            if (!isDestination) {
                int segmentKm = graph.getDistanceBetween(path.get(i), path.get(i + 1));
                double segmentHours = estimateTravelHours(Math.max(segmentKm, 0), 0);

                TextView segmentInfo = new TextView(this);
                segmentInfo.setText(String.format(
                        Locale.getDefault(),
                        "%s: %d km · %s",
                        getString(R.string.label_siguiente_tramo),
                        segmentKm,
                        formatDuration(segmentHours)
                ));
                segmentInfo.setTextSize(12f);
                segmentInfo.setTextColor(ContextCompat.getColor(this, R.color.text_sub));
                LinearLayout.LayoutParams segmentParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                segmentParams.topMargin = dp(10);
                segmentInfo.setLayoutParams(segmentParams);
                content.addView(segmentInfo);
            }

            cityCard.addView(content);
            row.addView(rail);
            row.addView(cityCard);
            itineraryContainer.addView(row);
        }
    }

    private GradientDrawable createCircleDrawable(int fillColor, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(fillColor);
        drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    private GradientDrawable createPillDrawable(int fillColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(999));
        drawable.setColor(fillColor);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private void drawRouteOnMap(List<String> path) {
        mapView.getOverlays().clear();
        List<GeoPoint> routePoints = new ArrayList<>();
        
        for (int i = 0; i < path.size(); i++) {
            String cityName = path.get(i);
            GeoPoint point = CityCoordinates.getCoordinates(cityName);
            if (point != null) {
                routePoints.add(point);
                Marker marker = new Marker(mapView);
                marker.setPosition(point);
                marker.setTitle(cityName);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                
                if (i == 0) {
                    marker.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_input_add));
                } else if (i == path.size() - 1) {
                    marker.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_directions));
                }
                
                mapView.getOverlays().add(marker);
            }
        }

        // Dibujar la ruta realista usando el servicio de enrutamiento OSRM
        if (routePoints.size() > 1) {
            // Ejecutar en un hilo separado para no bloquear la UI
            new Thread(() -> {
                try {
                    RoadManager roadManager = new OSRMRoadManager(this, "LaRutaMasCorta/1.0");
                    
                    // Obtener la ruta completa desde OSRM que sigue las carreteras
                    Road road = roadManager.getRoad(new ArrayList<>(routePoints));
                    
                    // Actualizar la UI en el hilo principal
                    runOnUiThread(() -> {
                        if (road != null && road.mRouteHigh != null && road.mRouteHigh.size() > 0) {
                            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                            roadOverlay.getOutlinePaint().setColor(ContextCompat.getColor(this, R.color.primary));
                            roadOverlay.getOutlinePaint().setStrokeWidth(12f);
                            mapView.getOverlays().add(0, roadOverlay);
                        } else {
                            // Si falla el enrutamiento, dibujar línea geodésica como respaldo
                            Polyline line = new Polyline();
                            line.setPoints(routePoints);
                            line.setColor(ContextCompat.getColor(this, R.color.primary));
                            line.setWidth(12f);
                            line.setGeodesic(true);
                            mapView.getOverlays().add(0, line);
                        }
                        mapView.invalidate();
                    });
                } catch (Exception e) {
                    // En caso de error, dibujar línea geodésica como respaldo
                    runOnUiThread(() -> {
                        Polyline line = new Polyline();
                        line.setPoints(routePoints);
                        line.setColor(ContextCompat.getColor(this, R.color.primary));
                        line.setWidth(12f);
                        line.setGeodesic(true);
                        mapView.getOverlays().add(0, line);
                        mapView.invalidate();
                    });
                }
            }).start();
        }

        if (!routePoints.isEmpty()) {
            double minLat = 90, maxLat = -90, minLon = 180, maxLon = -180;
            for (GeoPoint p : routePoints) {
                if (p.getLatitude() < minLat) minLat = p.getLatitude();
                if (p.getLatitude() > maxLat) maxLat = p.getLatitude();
                if (p.getLongitude() < minLon) minLon = p.getLongitude();
                if (p.getLongitude() > maxLon) maxLon = p.getLongitude();
            }
            GeoPoint center = new GeoPoint((minLat + maxLat) / 2, (minLon + maxLon) / 2);
            IMapController mapController = mapView.getController();
            mapController.animateTo(center);
            
            double maxSpan = Math.max(maxLat - minLat, maxLon - minLon);
            if (maxSpan < 1) mapController.setZoom(9.0);
            else if (maxSpan < 3) mapController.setZoom(8.0);
            else if (maxSpan < 6) mapController.setZoom(7.0);
            else mapController.setZoom(6.0);
        }
        mapView.invalidate();
    }

    private double estimateTravelHours(int distanceKm, int intermediateStops) {
        double drivingTimeHours = distanceKm / AVG_SPEED_KMH;
        double stopTimeHours = (intermediateStops * STOP_MINUTES) / 60.0;
        return drivingTimeHours + stopTimeHours;
    }

    private String estimateRoadType(int distanceKm, int segments) {
        double averageSegmentDistance = distanceKm / (double) segments;
        if (averageSegmentDistance >= 260) {
            return "Autopista de largo recorrido";
        }
        if (averageSegmentDistance >= 160) {
            return "Mixta (autopista y federal)";
        }
        return "Carretera regional/libre";
    }

    private double estimateFuelLiters(int distanceKm) {
        return distanceKm / FUEL_EFFICIENCY_KM_PER_L;
    }

    private double estimateTripCost(int distanceKm, String roadType, double fuelLiters) {
        double tollPerKm;
        if (roadType.startsWith("Autopista")) {
            tollPerKm = 1.35;
        } else if (roadType.startsWith("Mixta")) {
            tollPerKm = 0.85;
        } else {
            tollPerKm = 0.35;
        }
        double fuelCost = fuelLiters * FUEL_PRICE_MXN_PER_L;
        double tollCost = distanceKm * tollPerKm;
        return fuelCost + tollCost;
    }

    private double estimateCo2Kg(double fuelLiters) {
        return fuelLiters * CO2_KG_PER_L;
    }

    private String formatDuration(double totalHours) {
        int totalMinutes = (int) Math.round(totalHours * 60);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours == 0) {
            return String.format(Locale.getDefault(), "%d min", minutes);
        }
        return String.format(Locale.getDefault(), "%dh %02dmin", hours, minutes);
    }

    @Override
    protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
}