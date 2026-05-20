import React, { useState, useEffect } from 'react';
import axios from 'axios';

const AsuntosPropiosView = () => {
    const [solicitudes, setSolicitudes] = useState([]);
    const user = JSON.parse(localStorage.getItem('user'));
    const [newSolicitud, setNewSolicitud] = useState({ fechaSolicitada: '', tipoDia: 'MOSCOSO', justificacion: '' });
    const API_URL = `${import.meta.env.VITE_API_URL}/asuntos-propios`;

    useEffect(() => { fetchSolicitudes(); }, []);

    const fetchSolicitudes = async () => {
        const res = await axios.get(user.rol === 'ADMIN' ? API_URL : `${API_URL}/docente/${user.id}`);
        setSolicitudes(res.data);
    };

    const handleAction = async (id, nuevoEstado) => {
        try {
            await axios.put(`${API_URL}/${id}`, { estado: nuevoEstado });
            fetchSolicitudes();
        } catch (err) {
            alert(err.response.data.message || "Error al actualizar");
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("¿Anular solicitud?")) {
            await axios.delete(`${API_URL}/${id}`);
            fetchSolicitudes();
        }
    };

    const handleCreateSolicitud = async (e) => {
        e.preventDefault();
        try {
            await axios.post(API_URL, {
                ...newSolicitud,
                docente: { id: user.id } // Asigna el docente logueado
            });
            setNewSolicitud({ fechaSolicitada: '', tipoDia: 'MOSCOSO', justificacion: '' });
            fetchSolicitudes();
            alert("Solicitud creada con éxito.");
        } catch (err) {
            alert(err.response?.data?.message || "Error al crear la solicitud.");
        }
    };

    return (
        <div className="container mt-4">
            <h2>Gestión de Asuntos Propios</h2>

            {user.rol === 'PROFESOR' && (
                <div className="card p-4 mb-4 shadow-sm">
                    <h3>Solicitar Día de Asuntos Propios</h3>
                    <form onSubmit={handleCreateSolicitud} className="row g-3">
                        <div className="col-md-4">
                            <label className="form-label">Fecha Solicitada</label>
                            <input 
                                type="date" 
                                className="form-control" 
                                value={newSolicitud.fechaSolicitada} 
                                onChange={e => setNewSolicitud({...newSolicitud, fechaSolicitada: e.target.value})} 
                                required 
                            />
                        </div>
                        <div className="col-md-3">
                            <label className="form-label">Tipo de Día</label>
                            <select 
                                className="form-select" 
                                value={newSolicitud.tipoDia} 
                                onChange={e => setNewSolicitud({...newSolicitud, tipoDia: e.target.value})}
                            >
                                <option value="MOSCOSO">Moscoso</option>
                                <option value="INDISPOSICION">Indisposición</option>
                            </select>
                        </div>
                        <div className="col-md-5 d-flex align-items-end">
                            <button type="submit" className="btn btn-success w-100">Enviar Solicitud</button>
                        </div>
                    </form>
                </div>
            )}

            <table className="table mt-4 shadow-sm">
                <thead className="table-dark">
                    <tr>
                        {user.rol === 'ADMIN' && <th>Docente</th>}
                        <th>Fecha</th>
                        <th>Tipo</th>
                        <th>Estado</th>
                        <th>Justificación</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    {solicitudes.map(s => (
                        <tr key={s.id}>
                            {user.rol === 'ADMIN' && <td>{s.docente.nombre}</td>}
                            <td>{s.fechaSolicitada}</td>
                            <td>{s.tipoDia}</td>
                            <td>
                                <span className={`badge bg-${s.estado === 'APROBADA' ? 'success' : s.estado === 'PENDIENTE' ? 'warning' : 'danger'}`}>
                                    {s.estado}
                                </span>
                            </td>
                            <td>
                                {user.rol === 'ADMIN' && s.estado === 'PENDIENTE' && (
                                    <>
                                        <button className="btn btn-sm btn-success me-2" onClick={() => handleAction(s.id, 'APROBADA')}>Aprobar</button>
                                        <button className="btn btn-sm btn-danger" onClick={() => handleAction(s.id, 'DENEGADA')}>Denegar</button>
                                    </>
                                )}
                                {user.rol === 'PROFESOR' && s.estado === 'PENDIENTE' && (
                                    <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(s.id)}>Cancelar</button>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};
export default AsuntosPropiosView;