package com.example.larutamascorta;

import org.osmdroid.util.GeoPoint;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que mapea nombres de ciudades mexicanas a sus coordenadas geográficas.
 */
public class CityCoordinates {
    
    private static final Map<String, GeoPoint> coordinates = new HashMap<>();
    
    static {
        // Coordenadas aproximadas de ciudades mexicanas (latitud, longitud)
        coordinates.put("Aguascalientes", new GeoPoint(21.8853, -102.2916));
        coordinates.put("Ciudad de Mexico", new GeoPoint(19.4326, -99.1332));
        coordinates.put("Guadalajara", new GeoPoint(20.6597, -103.3496));
        coordinates.put("Leon", new GeoPoint(21.1250, -101.6860));
        coordinates.put("Merida", new GeoPoint(20.9674, -89.5926));
        coordinates.put("Monterrey", new GeoPoint(25.6866, -100.3161));
        coordinates.put("Morelia", new GeoPoint(19.7059, -101.1949));
        coordinates.put("Oaxaca", new GeoPoint(17.0732, -96.7266));
        coordinates.put("Puebla", new GeoPoint(19.0414, -98.2063));
        coordinates.put("Queretaro", new GeoPoint(20.5888, -100.3899));
        coordinates.put("San Luis Potosi", new GeoPoint(22.1565, -100.9855));
        coordinates.put("Toluca", new GeoPoint(19.2827, -99.6557));
        coordinates.put("Veracruz", new GeoPoint(19.1738, -96.1342));
    }
    
    /**
     * Obtiene las coordenadas de una ciudad.
     * @param cityName Nombre de la ciudad
     * @return GeoPoint con las coordenadas, o null si no existe
     */
    public static GeoPoint getCoordinates(String cityName) {
        return coordinates.get(cityName);
    }
    
    /**
     * Obtiene el centro geográfico aproximado de México.
     * @return GeoPoint con las coordenadas del centro de México
     */
    public static GeoPoint getMexicoCenterPoint() {
        return new GeoPoint(23.6345, -102.5528);
    }
}
