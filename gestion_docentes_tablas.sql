CREATE DATABASE gestion_docentes;

USE gestion_docentes;

CREATE TABLE docentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_profesor VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    departamento VARCHAR(100) NOT NULL,
    tipo_funcionario ENUM('CARRERA', 'PRACTICAS', 'INTERINO') NOT NULL,
    antiguedad_centro INT NOT NULL DEFAULT 0,
    nota_oposicion DECIMAL(4,2) NULL,
    rol ENUM('ADMIN', 'DOCENTE') NOT NULL DEFAULT 'DOCENTE',
    primer_acceso BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE horarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    docente_id INT NOT NULL,
    dia_semana ENUM('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES') NOT NULL,
    tramo_horario INT NOT NULL,
    grupo_aula VARCHAR(50) NOT NULL,
    FOREIGN KEY (docente_id) REFERENCES docentes(id) ON DELETE CASCADE
);

CREATE TABLE solicitudes_asuntos_propios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    docente_id INT NOT NULL,
    fecha_solicitada DATE NOT NULL,
    tipo_dia ENUM('TRIMESTRAL', 'NO_LECTIVO') NOT NULL,
    justificacion TEXT NULL,
    estado ENUM('PENDIENTE', 'APROBADA', 'DENEGADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (docente_id) REFERENCES docentes(id) ON DELETE CASCADE
);

CREATE TABLE ausencias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    docente_id INT NOT NULL,
    fecha DATE NOT NULL,
    tramo_horario INT NOT NULL,
    motivo VARCHAR(255) NULL,
    FOREIGN KEY (docente_id) REFERENCES docentes(id) ON DELETE CASCADE
);

CREATE TABLE guardias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ausencia_id INT NOT NULL UNIQUE,
    docente_guardia_id INT NOT NULL,
    estado ENUM('PENDIENTE', 'REALIZADA', 'INCIDENCIA') NOT NULL DEFAULT 'PENDIENTE',
    observaciones TEXT NULL,
    url_material_guardia VARCHAR(255) NULL,
    FOREIGN KEY (ausencia_id) REFERENCES ausencias(id) ON DELETE CASCADE,
    FOREIGN KEY (docente_guardia_id) REFERENCES docentes(id) ON DELETE CASCADE
);

INSERT INTO docentes (codigo_profesor, nombre, apellidos, email, password_hash, departamento, tipo_funcionario, antiguedad_centro, rol) 
VALUES ('DOC001', 'Carlos', 'Méndez Sandín', 'carlos@laboral.com', '123456', 'Informática', 'CARRERA', 24, 'ADMIN');

INSERT INTO docentes (codigo_profesor, nombre, apellidos, email, password_hash, departamento, tipo_funcionario, antiguedad_centro, rol) 
VALUES ('DOC002', 'Ana', 'García López', 'ana@laboral.com', '123456', 'Informática', 'INTERINO', 0, 'DOCENTE');

INSERT INTO docentes (codigo_profesor, nombre, apellidos, email, password_hash, departamento, tipo_funcionario, antiguedad_centro, rol) 
VALUES ('DOC003', 'Luis', 'Martínez Gómez', 'luis@laboral.com', '123456', 'FOL', 'CARRERA', 120, 'DOCENTE');

INSERT INTO horarios (docente_id, dia_semana, tramo_horario, grupo_aula) 
VALUES (1, 'LUNES', 1, '2DAW');