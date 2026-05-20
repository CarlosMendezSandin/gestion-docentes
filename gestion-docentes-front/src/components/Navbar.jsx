import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { usuario, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const nombreCompleto = [usuario?.nombre, usuario?.apellidos].filter(Boolean).join(' ');
  const rolLabel = isAdmin() ? 'Administrador' : 'Docente';

  return (
    <nav className="navbar navbar-dark bg-primary shadow-sm px-3 px-md-4 py-2">
      {/* Título — menu burger en movil */}
      <span className="navbar-brand fw-bold mb-0 fs-6">
        <span className="d-none d-sm-inline">🎓 CIFP La Laboral</span>
        <span className="d-inline d-sm-none">🎓 La Laboral</span>
      </span>

      <div className="d-flex align-items-center gap-2">
        {/* Badge de rol siempre visible */}
        <span className={`badge ${isAdmin() ? 'bg-warning text-dark' : 'bg-light text-primary'}`}>
          {rolLabel}
        </span>
        {/* Nombre solo en pantallas medianas+ */}
        <span className="text-white small d-none d-md-inline">{nombreCompleto}</span>
        <button
          className="btn btn-outline-light btn-sm"
          onClick={handleLogout}
        >
          <span className="d-none d-sm-inline">Cerrar sesión</span>
          <span className="d-inline d-sm-none">✕</span>
        </button>
      </div>
    </nav>
  );
}
