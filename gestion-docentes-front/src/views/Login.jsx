import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
    const [codigo, setCodigo] = useState('');
    const [pass, setPass] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setCargando(true);
        try {
            const res = await axios.post(`${import.meta.env.VITE_API_URL}/docentes/login`, {
                codigoProfesor: codigo,
                passwordHash: pass
            });
            login(res.data);
            if (res.data.primerAcceso === 1) {
                navigate('/cambiar-password', { replace: true });
            } else {
                navigate(res.data.rol === 'ADMIN' ? '/admin' : '/asuntos-propios', { replace: true });
            }
        } catch (err) {
            setError(err.response?.data || 'Error al conectar con el servidor');
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="min-vh-100 bg-light d-flex align-items-center justify-content-center p-3">
            <div className="card p-4 shadow-sm border-0 rounded-3 w-100" style={{ maxWidth: '420px' }}>
                <div className="text-center mb-4">
                    <div className="bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-3"
                        style={{ width: 56, height: 56, fontSize: '1.6rem' }}>
                        🎓
                    </div>
                    <h4 className="fw-bold mb-0">Gestión Docente</h4>
                    <p className="text-muted small">Accede con tu código de profesor</p>
                </div>

                {error && (
                    <div className="alert alert-danger py-2 small mb-3">{error}</div>
                )}

                <form onSubmit={handleLogin}>
                    <div className="mb-3">
                        <label className="form-label fw-semibold small">Código Profesor</label>
                        <input
                            type="text"
                            className="form-control"
                            value={codigo}
                            onChange={e => setCodigo(e.target.value)}
                            placeholder="Ej: DOC001"
                            autoFocus
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="form-label fw-semibold small">Contraseña</label>
                        <input
                            type="password"
                            className="form-control"
                            value={pass}
                            onChange={e => setPass(e.target.value)}
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        className="btn btn-primary w-100 fw-semibold"
                        disabled={cargando}
                    >
                        {cargando ? 'Entrando...' : 'Entrar'}
                    </button>
                </form>
            </div>
        </div>
    );
}
