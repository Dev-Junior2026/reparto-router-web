package com.repartorouter.reparto_router_web.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.repartorouter.reparto_router_web.model.Ruta;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    public String enviarNotificacionRuta(String tokenFcm, Ruta ruta) {
        Message message = Message.builder()
                .setToken(tokenFcm)
                .setNotification(Notification.builder()
                        .setTitle("Nueva ruta asignada")
                        .setBody("Tienes la ruta #" + ruta.getId() + " lista para repartir")
                        .build())
                .putData("rutaId", String.valueOf(ruta.getId()))
                .build();

        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException("Error enviando notificación: " + e.getMessage(), e);
        }
    }
}