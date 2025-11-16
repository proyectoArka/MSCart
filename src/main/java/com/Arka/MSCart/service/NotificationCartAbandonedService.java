package com.Arka.MSCart.service;

import com.Arka.MSCart.client.AuthClient;
import com.Arka.MSCart.client.InventarioClient;
import com.Arka.MSCart.dto.notificationDto.CarritoAbandonado;
import com.Arka.MSCart.dto.notificationDto.EmailRequest;
import com.Arka.MSCart.dto.notificationDto.ProductoAbandonado;
import com.Arka.MSCart.repository.CartDetailRepository;
import com.Arka.MSCart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationCartAbandonedService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final AuthClient authClient;
    private final InventarioClient inventarioClient;

    @Value("${lambda.email.url}")
    private String lambdaEmailUrl;

    private static final String TEMPLATE_CARRITO =
            "<!DOCTYPE html><html><head><title>¡Tu Carrito Te Espera! - Arka</title><style>body{font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;background-color:#f8f8f8;margin:0;padding:0;}.container{max-width:600px;margin:30px auto;background-color:#ffffff;padding:0;border-radius:10px;box-shadow:0 4px 12px rgba(0,0,0,0.1);}.header{background-color:#3f51b5;color:#ffffff;padding:25px 0;text-align:center;border-radius:10px 10px 0 0;}.header h2{margin:0;font-size:24px;}.content{padding:30px 40px;text-align:center;color:#333;line-height:1.6;}.content p{margin-bottom:15px;font-size:16px;}.cta-button{background-color:#ff9800;color:#ffffff;padding:15px 35px;text-decoration:none;font-weight:700;border-radius:8px;display:inline-block;margin-top:30px;font-size:18px;transition:background-color 0.3s;}.cta-button:hover{background-color:#f57c00;}.product-list{margin:30px 0;border-top:2px solid #eeeeee;border-bottom:2px solid #eeeeee;padding:15px 0;background-color:#fafafa;}.product-list ul{list-style:none;padding:0;margin:0;text-align:left;}.product-list li{padding:10px 0;border-bottom:1px dashed #e0e0e0;font-size:14px;}.product-list li:last-child{border-bottom:none;}.footer{margin-top:0;padding:20px;font-size:12px;color:#777;text-align:center;background-color:#f4f4f4;border-radius:0 0 10px 10px;}</style></head><body><div class=\"container\"><div class=\"header\"><h2>🛒 ¡Hemos guardado tus productos!</h2></div><div class=\"content\"><p>Hola <strong>[NOMBRE_CLIENTE]</strong>,</p><p>Notamos que dejaste algunos artículos increíbles en tu carrito. **¡Están listos cuando tú lo estés!**</p><div class=\"product-list\"><p style=\"font-weight:bold; color:#3f51b5;\">Artículos que te esperan:</p><ul>[LISTA_PRODUCTOS]</ul></div><a href=\"[URL_CARRITO]\" class=\"cta-button\">INICIAR SESIÓN Y COMPLETAR MI COMPRA</a></div><div class=\"footer\"><p>Si tienes alguna duda, contáctanos. ¡Gracias por elegir Arka!</p><p style=\"margin-top:5px; font-size:10px;\">© [AÑO ACTUAL] Arka E-commerce</p></div></div></body></html>";

    private final RestTemplate restTemplate = new RestTemplate();

    public NotificationCartAbandonedService(CartRepository cartRepository,
                                            CartDetailRepository cartDetailRepository,
                                            AuthClient authClient,
                                            InventarioClient inventarioClient) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.authClient = authClient;
        this.inventarioClient = inventarioClient;
    }


    // Tarea programada para detectar carritos abandonados y notificar vía email
    @Scheduled(cron = "${lambda.email.cron-expression}")
    private void detectarYNotificarCarritosAbandonados() {
        System.out.println("Buscando carritos abandonados según la expresión Cron...");

        // Lógica de detección de carritos abandonados
        cartRepository.findAll()
                .filter(cart -> !cart.isEmailEnviado() && !cart.isEstado() && cart.getNumeroProductos() > 0)
                .flatMap(cart ->
                        // Obtener el usuario del carrito
                        authClient.consultarUsuario(cart.getUserId())
                                // Por cada usuario obtener la lista de productos del carrito
                                .flatMap(user ->
                                        cartDetailRepository.findAllByCarritoId(cart.getId())
                                                .flatMap(detail ->
                                                        inventarioClient.consultarProducto(detail.getProductoId())
                                                                .map(producto -> {
                                                                    ProductoAbandonado pa = new ProductoAbandonado();
                                                                    pa.setNombreProducto(producto.getNombre());
                                                                    pa.setPrecioUnitario(producto.getPrice());
                                                                    return pa;
                                                                })
                                                )
                                                .collectList()
                                                .map(productosAbandonados -> {
                                                    CarritoAbandonado carrito = new CarritoAbandonado();
                                                    carrito.setNombreCliente(user.getName());
                                                    carrito.setEmailCliente(user.getEmail());
                                                    carrito.setUrlLogin("http://localhost:8093/api/v1/gateway/auth/login");
                                                    carrito.setProductos(productosAbandonados);

                                                    cart.setEmailEnviado(true);
                                                    cartRepository.save(cart).subscribe();

                                                    construirJson(carrito);
                                                    return carrito;
                                                })
                                )
                )
                .doOnNext(carrito -> System.out.println("Carrito preparado para notificar a: " + carrito.getEmailCliente()))
                .doOnError(e -> System.err.println("Error detectando carritos: " + e.getMessage()))
                .subscribe();
    }

    // Construye el contenido del email en formato HTML y llama a la Lambda para envío
    private void construirJson(CarritoAbandonado carrito) {
        String html = TEMPLATE_CARRITO;
        html = html.replace("[NOMBRE_CLIENTE]", carrito.getNombreCliente());
        html = html.replace("[URL_CARRITO]", carrito.getUrlLogin());

        StringBuilder productosHtml = new StringBuilder();
        if (carrito.getProductos() != null) {
            for (ProductoAbandonado p : carrito.getProductos()) {
                productosHtml.append(String.format("<li><strong>%s</strong> - %s</li>", p.getNombreProducto(), p.getPrecioUnitario()));
            }
        }
        html = html.replace("[LISTA_PRODUCTOS]", productosHtml.toString());

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setDestination(carrito.getEmailCliente());
        emailRequest.setAsunto("¡Tu Carrito Te Espera! 🛒 - Arka");
        emailRequest.setCuerpoMensaje(html);
        emailRequest.setTipoEvento("CARRITO_ABANDONADO");

        llamarLambda(emailRequest);
    }

    // Llama a la función Lambda para enviar el email
    private void llamarLambda(EmailRequest request) {
        try {
            restTemplate.postForObject(lambdaEmailUrl, request, Void.class);
            System.out.println("Correo enviado con éxito a la Lambda para: " + request.getDestination());
        } catch (Exception e) {
            System.err.println("Error al llamar a la Lambda: " + e.getMessage());
        }
    }
}