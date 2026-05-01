package com.memoria.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthConfirmationController {

    @GetMapping(value = "/confirmed", produces = MediaType.TEXT_HTML_VALUE)
    public String confirmed() {
        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Cuenta confirmada - memorIA</title>
                  <style>
                    body {
                      margin: 0;
                      min-height: 100vh;
                      display: grid;
                      place-items: center;
                      font-family: Arial, sans-serif;
                      background: #f6fbf8;
                      color: #20352c;
                    }
                    main {
                      width: min(520px, calc(100% - 32px));
                      padding: 32px;
                      border: 1px solid #cfe1d5;
                      border-radius: 8px;
                      background: #ffffff;
                    }
                    h1 {
                      margin: 0 0 12px;
                      color: #3b6f55;
                    }
                    p {
                      line-height: 1.5;
                    }
                  </style>
                </head>
                <body>
                  <main>
                    <h1>Cuenta confirmada</h1>
                    <p>Tu correo ya esta validado en memorIA.</p>
                    <p>Vuelve a la aplicacion e inicia sesion con tu email y contrasena.</p>
                  </main>
                </body>
                </html>
                """;
    }
}
