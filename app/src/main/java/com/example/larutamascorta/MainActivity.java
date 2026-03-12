package com.example.larutamascorta;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.api.IMapController;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Graph graph;
    private Spinner spinnerOrigen, spinnerDestino;
    private LinearLayout cardResultado;
    private TextView tvResultado;
    private MapView mapView;
    private ScrollView scrollView;

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

    // -------------------------------------------------------------------------
    // Construcción del grafo con ciudades mexicanas y distancias reales (km)
    // -------------------------------------------------------------------------

    private void buildGraph() {
        graph = new Graph();

        String[] cities = {
            "Aguascalientes", "Ciudad de Mexico", "Guadalajara",
            "Leon", "Merida", "Monterrey", "Morelia",
            "Oaxaca", "Puebla", "Queretaro",
            "San Luis Potosi", "Toluca", "Veracruz"
        };

        for (String city : cities) {
            graph.addNode(city);
        }

        // Conexiones con distancias aproximadas en kilómetros
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

    // -------------------------------------------------------------------------
    // Configuración de la UI
    // -------------------------------------------------------------------------

    private void setupViews() {
        spinnerOrigen   = findViewById(R.id.spinnerOrigen);
        spinnerDestino  = findViewById(R.id.spinnerDestino);
        Button btnCalcular = findViewById(R.id.btnCalcular);
        cardResultado   = findViewById(R.id.cardResultado);
        tvResultado     = findViewById(R.id.tvResultado);
        mapView         = findViewById(R.id.mapView);
        scrollView      = findViewById(R.id.main);

        // Configurar OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setupMap();

        List<String> cities = graph.getNodes();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerOrigen.setAdapter(adapter);
        spinnerDestino.setAdapter(adapter);

        // Selección inicial diferente para origen y destino
        if (cities.size() > 1) {
            spinnerDestino.setSelection(1);
        }

        btnCalcular.setOnClickListener(v -> calcularRuta());
    }

    private void setupMap() {
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(5.5);
        // Centrar en México
        GeoPoint mexicoCenter = CityCoordinates.getMexicoCenterPoint();
        mapController.setCenter(mexicoCenter);
        
        // Desactivar scroll del ScrollView cuando se toca el mapa
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        // Desactivar scroll del ScrollView
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Reactivar scroll del ScrollView
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Lógica de cálculo y presentación de resultados
    // -------------------------------------------------------------------------

    private void calcularRuta() {
        String origen  = (String) spinnerOrigen.getSelectedItem();
        String destino = (String) spinnerDestino.getSelectedItem();

        cardResultado.setVisibility(View.VISIBLE);

        if (origen.equals(destino)) {
            tvResultado.setText("El origen y el destino son el mismo lugar.");
            return;
        }

        Graph.Result result = graph.dijkstra(origen, destino);

        if (result.path.isEmpty()) {
            tvResultado.setText("No existe ruta entre " + origen + " y " + destino + ".");
            return;
        }

        // Construir texto del recorrido
        StringBuilder sb = new StringBuilder();
        sb.append("Ruta mas corta:\n\n");

        for (int i = 0; i < result.path.size(); i++) {
            sb.append("  [ ").append(result.path.get(i)).append(" ]");
            if (i < result.path.size() - 1) {
                sb.append("\n       |\n       v\n");
            }
        }

        sb.append("\n\n");
        sb.append("Distancia total : ").append(result.totalDistance).append(" km\n");
        sb.append("Paradas totales : ").append(result.path.size());

        if (result.path.size() > 2) {
            sb.append("\nEscalas        : ");
            for (int i = 1; i < result.path.size() - 1; i++) {
                sb.append(result.path.get(i));
                if (i < result.path.size() - 2) sb.append(", ");
            }
        }

        tvResultado.setText(sb.toString());
        
        // Dibujar ruta en el mapa
        drawRouteOnMap(result.path);
    }

    private void drawRouteOnMap(List<String> path) {
        // Limpiar overlays anteriores
        mapView.getOverlays().clear();
        
        if (path.isEmpty()) {
            mapView.invalidate();
            return;
        }

        // Crear lista de puntos geográficos
        List<GeoPoint> routePoints = new ArrayList<>();
        
        for (String cityName : path) {
            GeoPoint point = CityCoordinates.getCoordinates(cityName);
            if (point != null) {
                routePoints.add(point);
                
                // Agregar marcador para cada ciudad
                Marker marker = new Marker(mapView);
                marker.setPosition(point);
                marker.setTitle(cityName);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
            }
        }

        // Dibujar la línea de la ruta
        if (routePoints.size() > 1) {
            Polyline line = new Polyline();
            line.setPoints(routePoints);
            line.setColor(0xFF1565C0); // Color azul
            line.setWidth(5f);
            mapView.getOverlays().add(0, line); // Agregar al inicio para que esté debajo de los marcadores
        }

        // Ajustar el zoom para mostrar toda la ruta
        if (!routePoints.isEmpty()) {
            // Calcular el centro y ajustar el zoom
            double minLat = routePoints.stream().mapToDouble(GeoPoint::getLatitude).min().orElse(0);
            double maxLat = routePoints.stream().mapToDouble(GeoPoint::getLatitude).max().orElse(0);
            double minLon = routePoints.stream().mapToDouble(GeoPoint::getLongitude).min().orElse(0);
            double maxLon = routePoints.stream().mapToDouble(GeoPoint::getLongitude).max().orElse(0);
            
            GeoPoint center = new GeoPoint(
                (minLat + maxLat) / 2,
                (minLon + maxLon) / 2
            );
            
            IMapController mapController = mapView.getController();
            mapController.setCenter(center);
            
            // Calcular zoom basado en la distancia
            double latSpan = maxLat - minLat;
            double lonSpan = maxLon - minLon;
            double maxSpan = Math.max(latSpan, lonSpan);
            
            if (maxSpan < 1) {
                mapController.setZoom(9.0);
            } else if (maxSpan < 3) {
                mapController.setZoom(7.5);
            } else if (maxSpan < 6) {
                mapController.setZoom(6.5);
            } else {
                mapController.setZoom(5.5);
            }
        }

        mapView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
}