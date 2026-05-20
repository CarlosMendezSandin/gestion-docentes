import { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function CambiarPassword() {
    const { usuario, login } = useAuth();
    const navigate = useNavigate();
    const [nueva, setNueva] = useState('');
    const [confirmar, setConfirmar] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (nueva.length < 6) {
            setError('La contraseña debe tener al menos 6 caracteres.');
            return;
        }
        if (nueva !== confirmar) {
            setError('Las contraseñas no coinciden.');
            return;
        }
        setCargando(true);
        try {
            const { data } = await axios.put(
                `${import.meta.env.VITE_API_URL}/docentes/${usuario.id}/cambiar-password`,
                { nuevaPassword: nueva }
            );
            login(data);
            navigate(data.rol === 'ADMIN' ? '/admin' : '/asuntos-propios', { replace: true });
        } catch (err) {
            setError(err.response?.data || 'Error al cambiar la contraseña.');
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="min-vh-100 bg-light d-flex align-items-center justify-content-center p-3">
            <div className="card border-0 shadow-sm rounded-3 w-100" style={{ maxWidth: 440 }}>
                <div className="card-body p-4">
                    <div className="text-center mb-4">
                        <div
                            className="bg-warning text-dark rounded-circle d-inline-flex align-items-center justify-content-center mb-3"
                            style={{ width: 56, height: 56, fontSize: '1.6rem' }}
                        >
                            🔑
                        </div>
                        <h5 className="fw-bold mb-1">Cambio de contraseña obligatorio</h5>
                        <p className="text-muted small mb-0">
                            Es tu primer acceso. Elige una contraseña nueva para continuar.
                        </p>
                    </div>

                    {error && (
                        <div className="alert alert-danger py-2 small">{error}</div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label className="form-label fw-semibold small">Nueva contraseña</label>
                            <input
                                type="password"
                                className="form-control"
                                value={nueva}
                                onChange={e => setNueva(e.target.value)}
                                placeholder="Mínimo 6 caracteres"
                                autoFocus
                                required
                            />
                        </div>
                        <div className="mb-4">
                            <label className="form-label fw-semibold small">Confirmar contraseña</label>
                            <input
                                type="password"
                                className="form-control"
                                value={confirmar}
                                onChange={e => setConfirmar(e.target.value)}
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            className="btn btn-primary w-100 fw-semibold"
                            disabled={cargando}
                        >
                            {cargando ? 'Guardando…' : 'Guardar y continuar'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}
