package com.example.larutamascorta;

import java.util.*;

/**
 * Grafo no dirigido con pesos que implementa el algoritmo de Dijkstra
 * para encontrar la ruta más corta entre dos nodos.
 */
public class Graph {

    private final Map<String, List<Edge>> adjacencyList = new LinkedHashMap<>();

    public void addNode(String node) {
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    /** Agrega una arista bidireccional con distancia en km. */
    public void addEdge(String from, String to, int distanceKm) {
        adjacencyList.get(from).add(new Edge(to, distanceKm));
        adjacencyList.get(to).add(new Edge(from, distanceKm));
    }

    /**
     * Ejecuta Dijkstra desde {@code start} hasta {@code end}.
     * @return Result con el camino y la distancia total, o camino vacío si no hay ruta.
     */
    public Result dijkstra(String start, String end) {
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        // PriorityQueue: [nombre_nodo, distancia_acumulada]
        PriorityQueue<String[]> pq = new PriorityQueue<>(
                Comparator.comparingInt(a -> Integer.parseInt(a[1])));

        for (String node : adjacencyList.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        pq.offer(new String[]{start, "0"});

        while (!pq.isEmpty()) {
            String[] curr = pq.poll();
            String u = curr[0];
            int d = Integer.parseInt(curr[1]);

            if (u.equals(end)) break;
            if (d > dist.getOrDefault(u, Integer.MAX_VALUE)) continue;

            for (Edge edge : adjacencyList.getOrDefault(u, Collections.emptyList())) {
                int newDist = dist.get(u) + edge.weight;
                if (newDist < dist.getOrDefault(edge.to, Integer.MAX_VALUE)) {
                    dist.put(edge.to, newDist);
                    prev.put(edge.to, u);
                    pq.offer(new String[]{edge.to, String.valueOf(newDist)});
                }
            }
        }

        // Sin ruta alcanzable
        if (!dist.containsKey(end) || dist.get(end) == Integer.MAX_VALUE) {
            return new Result(Collections.emptyList(), -1);
        }

        // Reconstruir camino
        LinkedList<String> path = new LinkedList<>();
        String cursor = end;
        while (cursor != null) {
            path.addFirst(cursor);
            cursor = prev.get(cursor);
        }

        if (path.isEmpty() || !path.getFirst().equals(start)) {
            return new Result(Collections.emptyList(), -1);
        }

        return new Result(new ArrayList<>(path), dist.get(end));
    }

    /** Devuelve todos los nodos ordenados alfabéticamente. */
    public List<String> getNodes() {
        List<String> nodes = new ArrayList<>(adjacencyList.keySet());
        Collections.sort(nodes);
        return nodes;
    }

    // -------------------------------------------------------------------------

    public static class Edge {
        final String to;
        final int weight;

        Edge(String to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    public static class Result {
        public final List<String> path;
        public final int totalDistance;

        Result(List<String> path, int totalDistance) {
            this.path = path;
            this.totalDistance = totalDistance;
        }
    }
}
