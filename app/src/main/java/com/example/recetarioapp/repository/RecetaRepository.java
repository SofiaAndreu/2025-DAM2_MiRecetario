package com.example.recetarioapp.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.recetarioapp.database.RecetaDAO;
import com.example.recetarioapp.database.RecetasBD;
import com.example.recetarioapp.models.Receta;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Manejo de operaciones de datos
 * Conexion entre Firebase (nube) y Room (local)
 */
public class RecetaRepository {

    //Componentes
    private static final String TAG = "RecetaRepository";
    private static final String RECETAS_COLECCION = "recetas";
    private static final String RECETAS_STORAGE = "imagenes_recetas"; //carpeta de imagenes en Firebase

    //Dependencias
    private final RecetaDAO recetaDAO;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;
    private final LiveData<List<Receta>> todasLasRecetas;

    //Inicializacion
    public RecetaRepository(Application application) {
        //BD
        RecetasBD bd = RecetasBD.getInstance(application);
        recetaDAO = bd.recetaDAO();
        todasLasRecetas = recetaDAO.getAllRecetas();
        //Firebase
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        //sincronizacion
        sincronizarFBaLocal();
    }

    //---------------------------------------------------------
    //               OPERACIONES LOCALES (ROOM)
    //---------------------------------------------------------

    //GET TODAS LAS RECETAS
    public LiveData<List<Receta>> getAllRecetas(){
        return todasLasRecetas;
    }
    //GET RECETA POR ID
    public LiveData<Receta> getRecetaById(long id){
        return recetaDAO.getRecetaById(id);
    }
    //GET FAVORITAS
    public LiveData<List<Receta>> getFavs(){
        return recetaDAO.getFavs();
    }
    //GET RECETA POR CATEGORIA
    public LiveData<List<Receta>> getRecetasPorCategoria(String categoria){
        return recetaDAO.getRecetasPorCategoria(categoria);
    }
    //GET RECETA POR DIFICULTAD
    public LiveData<List<Receta>> getRecetasPorDificultad(String dificultad){
        return recetaDAO.getRecetasPorDificultad(dificultad);
    }
    //GET RECETA POR TIEMPO
    public LiveData<List<Receta>> getRecetasPorTiempo(int tiempo){
        return recetaDAO.getRecetasPorTiempo(tiempo);
    }
    //GET CATEGORIAS
    public LiveData<List<String>> getCategorias(){
        return recetaDAO.getCategorias();
    }
    //BUSCAR POR NOMBRE
    public LiveData<List<Receta>> buscarPorNombre(String query){
        return recetaDAO.buscarPorNombre(query);
    }
    //Marcar si es FAVORITA (solo LOCAL)
    public void establecerFavorita(long id, boolean isFav){
        RecetasBD.bdWriteExecutor.execute(() ->
                recetaDAO.actualizarFavorita(id, isFav));
    }


    //---------------------------------------------------------
    //                  OPERACIONES FIREBASE
    //---------------------------------------------------------

    //GUARDAR NUEVA RECETA (EN FIREBASE + ROOM)

    //SUBIR IMAGEN

    //ELIMINAR RECETA

    //ACTUALIZAR RECETA

    //SINCRONIZAR RECETAS de FIREBASE -> ROOM
    public void sincronizarFBaLocal() {
        FirebaseUser ussuario = auth.getCurrentUser();
        //Si no hay usuario -> no sincroniza
        if (ussuario == null) {
            Log.d(TAG, "Ningún usuario autenticado. Omitiendo sincronización");
            return;
        }
        //Si hay usuario
        firestore.collection(RECETAS_COLECCION)
                .whereEqualTo("usuarioId", ussuario.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Receta> recetasFirebase = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Receta receta = mapToReceta(doc.getData());
                        receta.setFirebaseId(doc.getId());
                        recetasFirebase.add(receta);
                    }
                    //Guardar en ROOM
                    RecetasBD.bdWriteExecutor.execute(() -> {
                        recetaDAO.insertAll(recetasFirebase);
                        Log.d(TAG, "Sincronizadas " + recetasFirebase.size() + " recetas.");
                    });
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error en el proceso de sincronización", e);
                });
    }

    //---------------------------------------------------------
    //                      METODOS HELPER
    //---------------------------------------------------------

    //DE MAP a RECETA
    private Receta mapToReceta(Map<String, Object> map){
        Receta receta = new Receta();
        receta.setNombre((String) map.get("nombre"));
        receta.setDescripcion((String) map.get("descripcion"));
        receta.setImagenPortadaURL((String) map.get("imagenPortadaUrl"));

        if (map.get("tiempoPreparacion") != null){
            receta.setTiempoPreparacion(((Long) map.get("tiempoPreparacion")).intValue());
        } if (map.get("porciones") != null){
            receta.setPorciones(((Long) map.get("porciones")).intValue());
        }

        receta.setDificultad((String) map.get("dificultad"));
        receta.setCategoria((String) map.get("categoria"));
        receta.setOrigen((String) map.get("origen"));
        receta.setUsuarioId((String) map.get("usuarioId"));

        if (map.get("isFav") != null) {
            receta.setFav((Boolean) map.get("Fav"));
        }

        return receta;
    }

    //de RECETA -> MAP
    private Map<String,Object> recetaToMap(Receta receta){
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", receta.getNombre());
        map.put("descripcion", receta.getDescripcion());
        map.put("imagenPortadaUrl", receta.getImagenPortadaURL());
        map.put("tiempoPreparacion", receta.getTiempoPreparacion());
        map.put("porciones", receta.getPorciones());
        map.put("dificultad", receta.getDificultad());
        map.put("categoria", receta.getCategoria());
        map.put("origen", receta.getOrigen());
        map.put("ingredientes", receta.getIngredientes());
        map.put("pasos", receta.getPasos());
        map.put("etiquetas", receta.getTags());
        map.put("fechaCreacion", receta.getFechaCreacion());
        map.put("fechaModificacion", receta.getFechaModificacion());
        map.put("esFavorita", receta.isFav());
        map.put("userId", receta.getUsuarioId());
        return map;
    }

    //---------------------------------------------------------
    //                     INTERFACES DE CALLBACKS
    //---------------------------------------------------------
    //Comunicar resultados de operaciones en Interfaz



}

