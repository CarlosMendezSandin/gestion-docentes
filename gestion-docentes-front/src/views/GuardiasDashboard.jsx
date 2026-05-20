import React, { useState, useEffect } from 'react';
import axios from 'axios';

const GuardiasDashboard = () => {
    const [docentes, setDocentes] = useState([]);
    const [guardias, setGuardias] = useState([]);
    const [form, setForm] = useState({ docenteId: '', fecha: '', tramo: 1, motivo: '' });
    const [asignado, setAsignado] = useState(null);

    const API_URL = import.meta.env.VITE_API_URL;

    useEffect(() => {
        fetchDocentes();
        fetchGuardias();
    }, []);

    const fetchDocentes = async () => {
        const res = await axios.get(`${API_URL}/docentes`);
        setDocentes(res.data);
    };

    const fetchGuardias = async () => {
        const res = await axios.get(`${API_URL}/guardias`);
        setGuardias(res.data);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const res = await axios.post(`${API_URL}/ausencias`, form);
            setAsignado(res.data.docenteGuardia?.nombre || "Nadie disponible");
            fetchGuardias();
        } catch (err) {
            alert("Error al tramitar la ausencia o cupo de asuntos propios excedido");
        }
    };

    return (
        <div className="container mt-4">
            <style>
                {`
                    @media print {
                        .no-print { display: none !important; }
                        .card { border: none !important; }
                        table { width: 100%; border: 1px solid #000; }
                    }
                `}
            </style>
            
            <div className="no-print card p-4 mb-4 shadow-sm">
                <h3>Registrar Ausencia</h3>
                <form onSubmit={handleSubmit} className="row g-3">
                    <div className="col-md-4">
                        <label>Docente</label>
                        <select className="form-select" onChange={e => setForm({...form, docenteId: e.target.value})} required>
                            <option value="">Seleccione...</option>
                            {docentes.map(d => <option key={d.id} value={d.id}>{d.nombre}</option>)}
                        </select>
                    </div>
                    <div className="col-md-3">
                        <label>Fecha</label>
                        <input type="date" className="form-control" onChange={e => setForm({...form, fecha: e.target.value})} required />
                    </div>
                    <div className="col-md-2">
                        <label>Tramo</label>
                        <input type="number" min="1" max="6" className="form-control" onChange={e => setForm({...form, tramo: e.target.value})} required />
                    </div>
                    <div className="col-md-3 d-flex align-items-end">
                        <label>Motivo</label>
                        <input type="text" className="form-control" onChange={e => setForm({...form, motivo: e.target.value})} required />
                    </div>
                    <div className="col-md-3 d-flex align-items-end">

                        <button type="submit" className="btn btn-primary w-100">Asignar Guardia</button>
                    </div>
                </form>
                {asignado && <div className="alert alert-info mt-3">Sustituto asignado: <strong>{asignado}</strong></div>}
            </div>

            <div className="d-flex justify-content-between align-items-center mb-3">
                <h2>Cuadrante de Guardias</h2>
                <button className="btn btn-dark no-print" onClick={() => window.print()}>Imprimir Parte</button>
            </div>

            <table className="table table-hover table-bordered shadow-sm">
                <thead className="table-light">
                    <tr>
                        <th>Fecha</th>
                        <th>Tramo</th>
                        <th>Prof. Ausente</th>
                        <th>Sustituto</th>
                        <th className="no-print">Estado</th>
                    </tr>
                </thead>
                <tbody>
                    {guardias.map(g => (
                        <tr key={g.id}>
                            <td>{g.ausencia.fecha}</td>
                            <td>{g.ausencia.tramoHorario}</td>
                            <td>{g.ausencia.docente.nombre}</td>
                            <td className="table-primary">{g.docenteGuardia?.nombre || 'SIN ASIGNAR'}</td>
                            <td className="no-print">
                                <select className="form-select form-select-sm" defaultValue={g.estado}>
                                    <option value="PENDIENTE">PENDIENTE</option>
                                    <option value="REALIZADA">REALIZADA</option>
                                    <option value="INCIDENCIA">INCIDENCIA</option>
                                </select>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default GuardiasDashboard;