package com.example.recetarioapp.repository;

import android.animation.RectEvaluator;
import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Database;

import com.example.recetarioapp.database.RecetaDAO;
import com.example.recetarioapp.database.RecetasBD;
import com.example.recetarioapp.models.Receta;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
    public void insertarReceta(Receta receta,OnRecetaGuardadaListener listener){
        RecetasBD.bdWriteExecutor.execute(() -> {
            try {
                //Comprobacion usuario autenticado
                FirebaseUser usuario = auth.getCurrentUser();
                if (usuario == null){
                    listener.onError("Usuario no autenticado");
                    return;
                }
                //Adignacion de Metadatos
                receta.setUsuarioId(usuario.getUid());
                receta.setFechaCreacion(new Date());
                receta.setFechaModificacion(new Date());

                //PRIMERO: Guardar en Room
                long localId = recetaDAO.insert(receta);
                receta.setId(localId);
                //LUEGO: Guardar en Firebase
                Map<String, Object> recetaMap = recetaToMap(receta);
                firestore.collection(RECETAS_COLECCION)
                        .add(recetaMap)
                        //si firebase responde correctamente
                        .addOnSuccessListener(documentReference -> {
                            String firebaseId = documentReference.getId();
                            receta.setFirebaseId(firebaseId);
                            //Actualizar en Room con FIREBASE ID
                            RecetasBD.bdWriteExecutor.execute(() ->{
                                recetaDAO.update(receta);
                                listener.onSuccess(receta);
                            });
                            Log.d(TAG, "Receta guardada con ID: " + firebaseId);
                        }) //si hay error al subir a firebase
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al guardar en Firebase", e);
                            listener.onError("Error al guardar: " + e.getMessage());
                        });
            } catch (Exception e){
                Log.e (TAG, "Error al insertar receta", e);
                listener.onError("Error: " + e.getMessage());
            }
        });
    }

    //SUBIR IMAGEN
    public void subirImagen(Uri imagenUri, OnImagenSubidaListener listener){
        if(imagenUri == null){
            listener.onError("URI de imagen inválida");
            return;
        }
        FirebaseUser usuario = auth.getCurrentUser();
        if(usuario == null){
            listener.onError("Usuario no autenticado");
            return;
        }
        //Generar nombre unico para la imagen
        String nombreArchivo = UUID.randomUUID().toString() + ".jpg";
        StorageReference imagenRef = storage.getReference()
                .child(RECETAS_STORAGE)
                .child(usuario.getUid())
                .child(nombreArchivo);
        imagenRef.putFile(imagenUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imagenRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                listener.onSuccess(uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al obtener URL", e);
                                listener.onError("Error al obtener URL: "+ e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al subir imagen", e);
                    listener.onError("Error al subir imagen: " + e.getMessage());
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    listener.onProgress((int) progress);
                });
    }

    //ELIMINAR RECETA
    public void eliminarReceta(Receta receta, OnRecetaEliminadaListener listener){
        RecetasBD.bdWriteExecutor.execute(() -> {
            try{
                //Eliminar de Room
                recetaDAO.delete(receta);
                //Eliminar de Firebase (si tiene firebaseid)
                if(receta.getFirebaseId() != null){
                    firestore.collection(RECETAS_COLECCION)
                            .document(receta.getFirebaseId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                listener.onSuccess();
                                Log.d(TAG, "Receta eliminada: " + receta.getFirebaseId());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al eliminar de Firebase", e);
                                listener.onError("Error al eliminar: " + e.getMessage());
                            });
                } else {
                    listener.onSuccess();
                }
            } catch (Exception e){
                Log.e(TAG, "Error al eliminar receta", e);
                listener.onError("Error: " + e.getMessage());
            }
        });
    }

    //ACTUALIZAR RECETA
    public void actualizarReceta(Receta receta, OnRecetaGuardadaListener listener){
        RecetasBD.bdWriteExecutor.execute(() ->{
            try{
                receta.setFechaModificacion(new Date());
                //Actualizar Room
                recetaDAO.update(receta);
                //actualizar Firebase (si tiene firebaseId)
                if (receta.getFirebaseId() != null){
                    Map<String, Object> recetaMap = recetaToMap(receta);
                    firestore.collection(RECETAS_COLECCION)
                            .document(receta.getFirebaseId())
                            .set(recetaMap)
                            .addOnSuccessListener(aVoid -> {
                                listener.onSuccess(receta);
                                Log.d(TAG, "Receta actualizada: " + receta.getFirebaseId());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al actualizar en Firebase", e);
                                listener.onError("Error al actualizar: " + e.getMessage());
                            });
                } else {
                    listener.onSuccess(receta);
                }
            }catch (Exception e) {
                Log.e(TAG, "Error al actualizar receta", e);
                listener.onError("Error: " + e.getMessage());
            }
        });
    }

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
    public interface OnRecetaGuardadaListener{
        void onSuccess(Receta receta);
        void onError(String mensaje);
    }
    public interface OnRecetaEliminadaListener{
        void onSuccess();
        void onError(String mensaje);
    }
    public interface OnImagenSubidaListener{
        void onSuccess(String url);
        void onError(String mensaje);
        void onProgress(int porcentaje);
    }
}