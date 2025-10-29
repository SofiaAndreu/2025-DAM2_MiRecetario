package com.example.recetarioapp.repository.remote;


import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.repository.mappers.RecetaMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// ============================================================================
// FirebaseDataSource.java - Maneja Firebase
// ============================================================================
public class FirebaseDataSource {

    private static final String COLLECTION = "recetas";
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final RecetaMapper mapper;

    public FirebaseDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.mapper = new RecetaMapper();
    }

    public void guardarReceta(Receta receta, OnSuccessListener listener, OnErrorListener errorListener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorListener.onError("Usuario no autenticado");
            return;
        }

        receta.setUsuarioId(user.getUid());
        Map<String, Object> map = mapper.toMap(receta);

        firestore.collection(COLLECTION)
                .add(map)
                .addOnSuccessListener(doc -> listener.onSuccess(doc.getId()))
                .addOnFailureListener(e -> errorListener.onError(e.getMessage()));
    }

    public void actualizarReceta(Receta receta, Runnable onSuccess, OnErrorListener errorListener) {
        if (receta.getFirebaseId() == null) return;

        Map<String, Object> map = mapper.toMap(receta);

        firestore.collection(COLLECTION)
                .document(receta.getFirebaseId())
                .set(map)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> errorListener.onError(e.getMessage()));
    }

    public void eliminarReceta(String firebaseId, Runnable onSuccess, OnErrorListener errorListener) {
        firestore.collection(COLLECTION)
                .document(firebaseId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> errorListener.onError(e.getMessage()));
    }

    public void obtenerRecetasUsuario(OnRecetasListener listener, OnErrorListener errorListener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorListener.onError("Usuario no autenticado");
            return;
        }

        firestore.collection(COLLECTION)
                .whereEqualTo("usuarioId", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Receta> recetas = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Receta receta = mapper.fromMap(doc.getData());
                        receta.setFirebaseId(doc.getId());
                        recetas.add(receta);
                    }
                    listener.onRecetasLoaded(recetas);
                })
                .addOnFailureListener(e -> errorListener.onError(e.getMessage()));
    }

    // Interfaces
    public interface OnSuccessListener {
        void onSuccess(String firebaseId);
    }

    public interface OnRecetasListener {
        void onRecetasLoaded(List<Receta> recetas);
    }

    public interface OnErrorListener {
        void onError(String mensaje);
    }
}
