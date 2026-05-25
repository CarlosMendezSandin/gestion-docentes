import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

function DocenteDashboard() {
  const [solicitudes, setSolicitudes] = useState([]);
  const [fecha, setFecha] = useState('');
  const [tipoDia, setTipoDia] = useState('TRIMESTRAL');
  const [justificacion, setJustificacion] = useState('');
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [mensaje, setMensaje] = useState({ texto: '', tipo: '' });
  const { usuario } = useAuth();

  const API_URL = import.meta.env.VITE_API_URL;

  const cargarSolicitudes = async () => {
    try {
      const response = await axios.get(`${API_URL}/asuntos-propios/docente/${usuario.id}`);
      setSolicitudes([...response.data].sort((a, b) => b.id - a.id));
    } catch (error) {
      console.error("Error al cargar solicitudes del docente:", error);
    }
  };

  useEffect(() => {
    if (usuario?.id) {
      cargarSolicitudes();
    }
  }, [usuario?.id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensaje({ texto: '', tipo: '' });

    const nuevaSolicitud = {
      docente: { id: usuario.id },
      fechaSolicitada: fecha,
      tipoDia: tipoDia,
      justificacion: justificacion,
      estado: 'PENDIENTE'
    };

    try {
      await axios.post(`${API_URL}/asuntos-propios`, nuevaSolicitud);
      setMensaje({ texto: 'Solicitud enviada con éxito. Queda en espera de revisión.', tipo: 'success' });
      setFecha('');
      setJustificacion('');
      setMostrarFormulario(false);
      await cargarSolicitudes();
    } catch (error) {
      console.error("Error al crear la solicitud:", error);
      const msg = error.response?.data || 'No se pudo registrar tu solicitud. Inténtalo de nuevo.';
      setMensaje({ texto: msg, tipo: 'danger' });
    }
  };

  const handleSubirMaterial = (solId) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.onchange = async (e) => {
      const file = e.target.files[0];
      if (!file) return;
      const form = new FormData();
      form.append('file', file);
      try {
        await axios.post(`${API_URL}/asuntos-propios/${solId}/material`, form, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        setMensaje({ texto: '✓ Material subido correctamente.', tipo: 'success' });
      } catch (err) {
        setMensaje({ texto: 'No se pudo subir el material: ' + (err.response?.data || err.message), tipo: 'danger' });
      }
    };
    input.click();
  };

  // Nombre completo usando nombre + apellidos
  const nombreCompleto = [usuario?.nombre, usuario?.apellidos].filter(Boolean).join(' ') || 'Usuario';
  const inicial = usuario?.nombre ? usuario.nombre.charAt(0).toUpperCase() : '?';

  return (
    <div className="container-fluid container-lg mt-3 mt-md-4 px-3 px-md-4">
      {/* Banner de bienvenida */}
      <div className="alert alert-white border shadow-sm mb-4 d-flex align-items-center p-3 rounded-3">
        <div
          className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-3"
          style={{ width: '50px', height: '50px', fontSize: '1.5rem', flexShrink: 0 }}
        >
          {inicial}
        </div>
        <div>
          <h5 className="mb-0 fw-bold">Bienvenido/a, {nombreCompleto}</h5>
          <p className="mb-0 text-muted small">
            Departamento de <strong>{usuario?.departamento || 'Sin asignar'}</strong> | Condición:{' '}
            <strong>{usuario?.tipoFuncionario || 'Docente'}</strong>
          </p>
        </div>
      </div>

      <div className="card shadow-sm border-0 rounded-3">
        <div className="card-header bg-primary text-white py-3 d-flex justify-content-between align-items-center rounded-top-3">
          <h4 className="m-0 fw-bold">Mis Solicitudes de Asuntos Propios</h4>
          <button
            className={`btn btn-sm fw-bold ${mostrarFormulario ? 'btn-light text-primary' : 'btn-outline-light'}`}
            onClick={() => setMostrarFormulario(!mostrarFormulario)}
          >
            {mostrarFormulario ? '✕ Cancelar' : '+ Nueva Solicitud'}
          </button>
        </div>

        <div className="card-body p-4">
          {mensaje.texto && (
            <div className={`alert alert-${mensaje.tipo} alert-dismissible fade show fw-semibold rounded-2`} role="alert">
              {mensaje.texto}
              <button type="button" className="btn-close" onClick={() => setMensaje({ texto: '', tipo: '' })}></button>
            </div>
          )}

          {mostrarFormulario && (
            <div className="bg-light p-4 rounded-3 border mb-4">
              <h5 className="mb-3 fw-bold text-secondary">Formulario de Tramitación</h5>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label fw-semibold text-muted small">Fecha Solicitada</label>
                  <input
                    type="date"
                    className="form-control rounded-2"
                    value={fecha}
                    onChange={(e) => setFecha(e.target.value)}
                    min={new Date().toISOString().split('T')[0]}
                    required
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold text-muted small">Tipo de Día</label>
                  <select
                    className="form-select rounded-2"
                    value={tipoDia}
                    onChange={(e) => setTipoDia(e.target.value)}
                  >
                    <option value="TRIMESTRAL">Asunto Propio Trimestral</option>
                    <option value="NO_LECTIVO">Día No Lectivo Adicional</option>
                  </select>
                </div>
                <div className="col-12">
                  <label className="form-label fw-semibold text-muted small">Justificación / Motivo</label>
                  <textarea
                    className="form-control rounded-2"
                    rows="3"
                    placeholder="Detalles de la causa..."
                    value={justificacion}
                    onChange={(e) => setJustificacion(e.target.value)}
                  />
                </div>
                <div className="col-12 text-end">
                  <button
                    type="button"
                    className="btn btn-primary px-4 rounded-2 fw-semibold"
                    onClick={handleSubmit}
                    disabled={!fecha}
                  >
                    Enviar Solicitud
                  </button>
                </div>
              </div>
            </div>
          )}

          {solicitudes.length === 0 ? (
            <div className="text-center py-4 text-muted fst-italic bg-light rounded-3 border">
              Aún no has tramitado ninguna solicitud de asuntos propios este curso.
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle m-0">
                <thead className="table-light">
                  <tr>
                    <th>ID</th>
                    <th>Fecha Solicitada</th>
                    <th>Tipo</th>
                    <th>Justificación</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {solicitudes.map((sol) => (
                    <tr key={sol.id}>
                      <td className="fw-bold text-muted">#{sol.id}</td>
                      <td className="fw-semibold text-secondary">{sol.fechaSolicitada}</td>
                      <td>
                        <span className={`badge ${sol.tipoDia === 'TRIMESTRAL' ? 'bg-info text-dark' : 'bg-warning text-dark'}`}>
                          {sol.tipoDia}
                        </span>
                      </td>
                      <td className="text-muted small">
                        {sol.justificacion || <span className="fst-italic text-black-50">Sin especificar</span>}
                      </td>
                      <td>
                        <div className="d-flex align-items-center gap-2">
                          <span className={`badge ${
                            sol.estado === 'APROBADA' ? 'bg-success' :
                            sol.estado === 'DENEGADA' ? 'bg-danger' : 'bg-secondary'
                          }`}>
                            {sol.estado || 'PENDIENTE'}
                          </span>
                          {sol.estado === 'APROBADA' && (
                            <button
                              className="btn btn-sm btn-outline-primary py-0"
                              onClick={() => handleSubirMaterial(sol.id)}
                            >
                              📁 Subir Material
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default DocenteDashboard;
