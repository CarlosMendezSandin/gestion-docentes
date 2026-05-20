import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API = import.meta.env.VITE_API_URL;

//  Helpers
const badge = (estado) => {
    const map = {
        APROBADA: 'bg-success', REALIZADA: 'bg-success',
        DENEGADA: 'bg-danger',  INCIDENCIA: 'bg-danger',
        PENDIENTE: 'bg-secondary',
    };
    return `badge ${map[estado] ?? 'bg-secondary'}`;
};

// Seccion: docentes
function SeccionDocentes() {
    const [docentes, setDocentes] = useState([]);
    const [form, setForm] = useState(null);
    const [errorForm, setErrorForm] = useState('');

    const cargar = async () => {
        const { data } = await axios.get(`${API}/docentes`);
        setDocentes(data);
    };

    useEffect(() => { cargar(); }, []);

    const guardar = async () => {
        setErrorForm('');
        try {
            const payload = { ...form, primerAcceso: form.id ? form.primerAcceso : 1 };
            if (form.id) {
                await axios.put(`${API}/docentes/${form.id}`, payload);
            } else {
                await axios.post(`${API}/docentes`, payload);
            }
            setForm(null);
            cargar();
        } catch (err) {
            setErrorForm(err.response?.data || err.message || 'Error al guardar');
        }
    };

    const eliminar = async (id) => {
        if (!confirm('¿Eliminar este docente?')) return;
        await axios.delete(`${API}/docentes/${id}`);
        cargar();
    };

    const campo = (key, label, type = 'text') => (
        <div className="col-md-4 mb-2" key={key}>
            <label className="form-label small fw-semibold text-muted">{label}</label>
            <input
                type={type}
                className="form-control form-control-sm"
                value={form[key] ?? ''}
                onChange={e => setForm({ ...form, [key]: e.target.value })}
            />
        </div>
    );

    return (
        <div className="mb-5">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h5 className="fw-bold mb-0">👥 Gestión de Docentes</h5>
                <button className="btn btn-sm btn-primary"
                    onClick={() => setForm({ rol: 'DOCENTE', tipoFuncionario: 'CARRERA', antiguedadCentro: 0 })}>
                    + Nuevo Docente
                </button>
            </div>

            {form && (
                <div className="card border-0 shadow-sm mb-3">
                    <div className="card-body">
                        <h6 className="mb-3 text-secondary">{form.id ? 'Editar Docente' : 'Nuevo Docente'}</h6>
                        <div className="row">
                            {campo('nombre', 'Nombre')}
                            {campo('apellidos', 'Apellidos')}
                            {campo('email', 'Email', 'email')}
                            {campo('codigoProfesor', 'Código Profesor')}
                            {campo('passwordHash', 'Contraseña', 'password')}
                            {campo('departamento', 'Departamento')}
                            <div className="col-md-4 mb-2">
                                <label className="form-label small fw-semibold text-muted">Tipo Funcionario</label>
                                <select className="form-select form-select-sm"
                                    value={form.tipoFuncionario ?? ''}
                                    onChange={e => setForm({ ...form, tipoFuncionario: e.target.value })}>
                                    <option value="CARRERA">Carrera</option>
                                    <option value="PRACTICAS">Prácticas</option>
                                    <option value="INTERINO">Interino</option>
                                </select>
                            </div>
                            <div className="col-md-4 mb-2">
                                <label className="form-label small fw-semibold text-muted">Rol</label>
                                <select className="form-select form-select-sm"
                                    value={form.rol ?? 'DOCENTE'}
                                    onChange={e => setForm({ ...form, rol: e.target.value })}>
                                    <option value="DOCENTE">Docente</option>
                                    <option value="ADMIN">Admin</option>
                                </select>
                            </div>
                            {campo('antiguedadCentro', 'Antigüedad (meses)', 'number')}
                            {campo('notaOposicion', 'Nota oposición', 'number')}
                        </div>
                        {errorForm && (
                            <div className="alert alert-danger py-2 small mt-2">{errorForm}</div>
                        )}
                        <div className="text-end mt-2">
                            <button className="btn btn-sm btn-secondary me-2" onClick={() => { setForm(null); setErrorForm(''); }}>Cancelar</button>
                            <button className="btn btn-sm btn-success" onClick={guardar}>Guardar</button>
                        </div>
                    </div>
                </div>
            )}

            <div className="table-responsive">
                <table className="table table-hover table-sm align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>Código</th><th>Nombre</th>
                            <th className="d-none d-md-table-cell">Departamento</th>
                            <th>Tipo</th><th>Rol</th>
                            <th className="d-none d-lg-table-cell">Antigüedad</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {docentes.map(d => (
                            <tr key={d.id}>
                                <td className="fw-bold text-muted">{d.codigoProfesor}</td>
                                <td>{d.nombre} {d.apellidos}</td>
                                <td className="d-none d-md-table-cell">{d.departamento}</td>
                                <td><span className="badge bg-info text-dark">{d.tipoFuncionario}</span></td>
                                <td><span className={`badge ${d.rol === 'ADMIN' ? 'bg-primary' : 'bg-secondary'}`}>{d.rol}</span></td>
                                <td className="d-none d-lg-table-cell">{d.antiguedadCentro} meses</td>
                                <td>
                                    <button className="btn btn-xs btn-outline-secondary btn-sm me-1"
                                        onClick={() => setForm(d)}>✏️</button>
                                    <button className="btn btn-xs btn-outline-danger btn-sm"
                                        onClick={() => eliminar(d.id)}>🗑️</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

// Sección: ausencias
function SeccionAusencias() {
    const [ausencias, setAusencias] = useState([]);
    const [docentes, setDocentes] = useState([]);
    const [form, setForm] = useState(null);

    const cargar = async () => {
        const [a, d] = await Promise.all([
            axios.get(`${API}/ausencias`),
            axios.get(`${API}/docentes`),
        ]);
        setAusencias(a.data);
        setDocentes(d.data);
    };

    useEffect(() => { cargar(); }, []);

    const guardar = async () => {
        await axios.post(`${API}/ausencias`, {
            docente: { id: parseInt(form.docenteId) },
            fecha: form.fecha,
            tramoHorario: parseInt(form.tramoHorario),
            motivo: form.motivo,
        });
        setForm(null);
        cargar();
    };

    return (
        <div className="mb-5">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h5 className="fw-bold mb-0">📋 Registro de Ausencias</h5>
                <button className="btn btn-sm btn-primary" onClick={() => setForm({})}>+ Nueva Ausencia</button>
            </div>

            {form && (
                <div className="card border-0 shadow-sm mb-3">
                    <div className="card-body">
                        <h6 className="mb-3 text-secondary">Nueva Ausencia — se asignará guardia automáticamente</h6>
                        <div className="row g-2">
                            <div className="col-md-4">
                                <label className="form-label small fw-semibold text-muted">Docente Ausente</label>
                                <select className="form-select form-select-sm"
                                    value={form.docenteId ?? ''}
                                    onChange={e => setForm({ ...form, docenteId: e.target.value })}>
                                    <option value="">Selecciona...</option>
                                    {docentes.map(d => (
                                        <option key={d.id} value={d.id}>{d.nombre} {d.apellidos}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="col-md-3">
                                <label className="form-label small fw-semibold text-muted">Fecha</label>
                                <input type="date" className="form-control form-control-sm"
                                    value={form.fecha ?? ''} onChange={e => setForm({ ...form, fecha: e.target.value })} />
                            </div>
                            <div className="col-md-2">
                                <label className="form-label small fw-semibold text-muted">Tramo (1-6)</label>
                                <input type="number" min="1" max="6" className="form-control form-control-sm"
                                    value={form.tramoHorario ?? ''} onChange={e => setForm({ ...form, tramoHorario: e.target.value })} />
                            </div>
                            <div className="col-md-3">
                                <label className="form-label small fw-semibold text-muted">Motivo</label>
                                <input type="text" className="form-control form-control-sm"
                                    value={form.motivo ?? ''} onChange={e => setForm({ ...form, motivo: e.target.value })} />
                            </div>
                        </div>
                        <div className="text-end mt-2">
                            <button className="btn btn-sm btn-secondary me-2" onClick={() => setForm(null)}>Cancelar</button>
                            <button className="btn btn-sm btn-success" onClick={guardar}
                                disabled={!form.docenteId || !form.fecha || !form.tramoHorario}>
                                Registrar y asignar guardia
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <div className="table-responsive">
                <table className="table table-hover table-sm align-middle">
                    <thead className="table-light">
                        <tr><th>ID</th><th>Docente Ausente</th><th>Fecha</th><th>Tramo</th><th>Motivo</th></tr>
                    </thead>
                    <tbody>
                        {ausencias.map(a => (
                            <tr key={a.id}>
                                <td className="text-muted">#{a.id}</td>
                                <td>{a.docente?.nombre} {a.docente?.apellidos}</td>
                                <td>{a.fecha}</td>
                                <td>{a.tramoHorario}</td>
                                <td className="text-muted small">{a.motivo || '—'}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

// Sección: guardias
function SeccionGuardias() {
    const hoy = new Date().toISOString().split('T')[0];
    const [fecha, setFecha] = useState(hoy);
    const [guardias, setGuardias] = useState([]);
    const [cargando, setCargando] = useState(false);
    const [editando, setEditando] = useState(null);

    const cargar = async (f = fecha) => {
        setCargando(true);
        try {
            const { data } = await axios.get(`${API}/guardias/fecha/${f}`);
            setGuardias(data.sort((a, b) => (a.ausencia?.tramoHorario ?? 0) - (b.ausencia?.tramoHorario ?? 0)));
        } catch { setGuardias([]); }
        finally { setCargando(false); }
    };

    useEffect(() => { cargar(); }, []);

    const actualizarEstado = async (id, estado, observaciones) => {
        await axios.patch(`${API}/guardias/${id}/estado`, { estado, observaciones });
        setEditando(null);
        cargar();
    };

    const imprimir = () => window.print();

    const TRAMOS = ['', '08:00-09:00', '09:00-10:00', '10:00-11:00', '11:30-12:30', '12:30-13:30', '13:30-14:30'];

    return (
        <div>
            {/* Controles */}
            <div className="d-flex flex-wrap gap-2 align-items-end mb-4 no-print">
                <div>
                    <label className="form-label fw-semibold small mb-1">Fecha del cuadrante</label>
                    <input type="date" className="form-control form-control-sm"
                        value={fecha} onChange={e => setFecha(e.target.value)} />
                </div>
                <button className="btn btn-primary btn-sm" onClick={() => cargar(fecha)}>
                    🔍 Ver cuadrante
                </button>
                <button className="btn btn-outline-secondary btn-sm ms-auto" onClick={imprimir}>
                    🖨️ Imprimir
                </button>
            </div>

            {/* Cabecera impresion */}
            <div className="text-center mb-3 print-only" style={{ display: 'none' }}>
                <h5 className="fw-bold mb-0">CIFP La Laboral — Cuadrante de Guardias</h5>
                <p className="text-muted mb-0">{fecha}</p>
            </div>

            {cargando ? (
                <div className="text-center py-4 text-muted">Cargando…</div>
            ) : guardias.length === 0 ? (
                <div className="alert alert-info">No hay guardias registradas para el día {fecha}.</div>
            ) : (
                <div className="table-responsive">
                    <table className="table table-bordered table-hover align-middle">
                        <thead className="table-dark">
                            <tr>
                                <th>Tramo</th>
                                <th>Docente Ausente</th>
                                <th>Motivo</th>
                                <th>Docente de Guardia</th>
                                <th>Estado</th>
                                <th>Observaciones</th>
                                <th className="no-print">Acción</th>
                            </tr>
                        </thead>
                        <tbody>
                            {guardias.map(g => (
                                <tr key={g.id}>
                                    <td className="text-center fw-bold">
                                        <span className="d-block">T{g.ausencia?.tramoHorario}</span>
                                        <span className="text-muted small fw-normal">
                                            {TRAMOS[g.ausencia?.tramoHorario] ?? ''}
                                        </span>
                                    </td>
                                    <td>{g.ausencia?.docente?.nombre} {g.ausencia?.docente?.apellidos}</td>
                                    <td className="text-muted small">{g.ausencia?.motivo || '—'}</td>
                                    <td className="fw-semibold text-primary">
                                        {g.docenteGuardia?.nombre} {g.docenteGuardia?.apellidos}
                                    </td>
                                    <td><span className={badge(g.estado)}>{g.estado}</span></td>
                                    <td className="text-muted small">{g.observaciones || '—'}</td>
                                    <td className="no-print">
                                        {editando?.id === g.id ? (
                                            <div className="d-flex flex-column gap-1">
                                                <select className="form-select form-select-sm"
                                                    value={editando.estado}
                                                    onChange={e => setEditando({ ...editando, estado: e.target.value })}>
                                                    <option value="PENDIENTE">Pendiente</option>
                                                    <option value="REALIZADA">Realizada</option>
                                                    <option value="INCIDENCIA">Incidencia</option>
                                                </select>
                                                <input className="form-control form-control-sm"
                                                    placeholder="Observación…"
                                                    value={editando.observaciones ?? ''}
                                                    onChange={e => setEditando({ ...editando, observaciones: e.target.value })} />
                                                <div className="d-flex gap-1">
                                                    <button className="btn btn-sm btn-success flex-fill"
                                                        onClick={() => actualizarEstado(g.id, editando.estado, editando.observaciones)}>✓</button>
                                                    <button className="btn btn-sm btn-secondary flex-fill"
                                                        onClick={() => setEditando(null)}>✕</button>
                                                </div>
                                            </div>
                                        ) : (
                                            <button className="btn btn-sm btn-outline-secondary"
                                                onClick={() => setEditando({ id: g.id, estado: g.estado, observaciones: g.observaciones })}>
                                                ✏️ Editar
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

// Sección: asuntos propios (admin)
function SeccionAsuntosPropios() {
    const [solicitudes, setSolicitudes] = useState([]);
    const [maxDia, setMaxDia] = useState(3);
    const [nuevoMax, setNuevoMax] = useState(3);
    const [guardandoConfig, setGuardandoConfig] = useState(false);
    const [msgConfig, setMsgConfig] = useState('');

    const cargar = async () => {
        const [{ data: sols }, { data: cfg }] = await Promise.all([
            axios.get(`${API}/asuntos-propios`),
            axios.get(`${API}/configuracion`),
        ]);
        setSolicitudes([...sols].sort((a, b) => b.id - a.id));
        setMaxDia(cfg.maxAsuntosPropiosDia);
        setNuevoMax(cfg.maxAsuntosPropiosDia);
    };

    useEffect(() => { cargar(); }, []);

    const cambiarEstado = async (id, estado) => {
        try {
            await axios.patch(`${API}/asuntos-propios/${id}/estado`, { estado });
            cargar();
        } catch (err) {
            alert(err.response?.data || 'Error al cambiar el estado');
        }
    };

    const guardarConfig = async () => {
        setGuardandoConfig(true);
        setMsgConfig('');
        try {
            await axios.put(`${API}/configuracion`, { maxAsuntosPropiosDia: Number(nuevoMax) });
            setMaxDia(Number(nuevoMax));
            setMsgConfig('✓ Configuración guardada');
        } catch (e) {
            setMsgConfig('Error: ' + (e.response?.data || e.message));
        } finally {
            setGuardandoConfig(false);
        }
    };

    return (
        <div>
            {/*  Maximo por dia */}
            <div className="card border-0 bg-light mb-4 p-3 d-flex flex-row flex-wrap align-items-center gap-3">
                <div>
                    <span className="fw-semibold small">Máx. docentes con asunto propio el mismo día:</span>
                    <span className="badge bg-primary ms-2 fs-6">{maxDia}</span>
                </div>
                <div className="d-flex align-items-center gap-2">
                    <input type="number" min="1" max="20" className="form-control form-control-sm"
                        style={{ width: 70 }} value={nuevoMax}
                        onChange={e => setNuevoMax(e.target.value)} />
                    <button className="btn btn-sm btn-outline-primary" onClick={guardarConfig} disabled={guardandoConfig}>
                        {guardandoConfig ? '…' : 'Actualizar'}
                    </button>
                    {msgConfig && <span className="small text-success">{msgConfig}</span>}
                </div>
            </div>

            <h5 className="fw-bold mb-3">📅 Solicitudes de Asuntos Propios</h5>
            <div className="table-responsive">
                <table className="table table-hover table-sm align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>ID</th><th>Docente</th>
                            <th className="d-none d-lg-table-cell">Email</th>
                            <th>Fecha</th><th>Tipo</th>
                            <th className="d-none d-md-table-cell">Justificación</th>
                            <th>Estado</th><th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        {solicitudes.map(s => (
                            <tr key={s.id}>
                                <td className="text-muted">#{s.id}</td>
                                <td>{s.docente?.nombre} {s.docente?.apellidos}</td>
                                <td className="d-none d-lg-table-cell text-muted small">{s.docente?.email || '—'}</td>
                                <td>{s.fechaSolicitada}</td>
                                <td>
                                    <span className={`badge ${s.tipoDia === 'TRIMESTRAL' ? 'bg-info text-dark' : 'bg-warning text-dark'}`}>
                                        {s.tipoDia}
                                    </span>
                                </td>
                                <td className="d-none d-md-table-cell text-muted small">{s.justificacion || '—'}</td>
                                <td><span className={badge(s.estado)}>{s.estado}</span></td>
                                <td>
                                    {s.estado === 'PENDIENTE' && (
                                        <div className="d-flex gap-1">
                                            <button className="btn btn-sm btn-success"
                                                onClick={() => cambiarEstado(s.id, 'APROBADA')}>✓ Aprobar</button>
                                            <button className="btn btn-sm btn-danger"
                                                onClick={() => cambiarEstado(s.id, 'DENEGADA')}>✕ Denegar</button>
                                        </div>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

// Sección: importar CSV 
function SeccionImportar() {
    const [fileDocentes, setFileDocentes] = useState(null);
    const [fileHorarios, setFileHorarios] = useState(null);
    const [resDocentes, setResDocentes] = useState(null);
    const [resHorarios, setResHorarios] = useState(null);
    const [cargando, setCargando] = useState({ docentes: false, horarios: false });

    const subir = async (tipo, file, setRes) => {
        if (!file) return;
        setCargando(p => ({ ...p, [tipo]: true }));
        setRes(null);
        const form = new FormData();
        form.append('file', file);
        try {
            const { data } = await axios.post(`${API}/importar/${tipo}`, form, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setRes({ ok: true, ...data });
        } catch (e) {
            setRes({ ok: false, error: e.response?.data?.error || 'Error al importar' });
        } finally {
            setCargando(p => ({ ...p, [tipo]: false }));
        }
    };

    const bloque = (titulo, icono, ejemplo, file, setFile, res, onSubir, carg) => (
        <div className="col-12 col-lg-6">
            <div className="card border-0 shadow-sm h-100">
                <div className="card-header bg-light fw-bold">
                    {icono} {titulo}
                </div>
                <div className="card-body">
                    <p className="text-muted small mb-2">Formato CSV esperado (primera fila = cabecera):</p>
                    <pre className="bg-light border rounded p-2 small text-wrap" style={{ fontSize: '0.72rem' }}>
                        {ejemplo}
                    </pre>
                    <div className="mb-3">
                        <input
                            type="file"
                            accept=".csv"
                            className="form-control form-control-sm"
                            onChange={e => setFile(e.target.files[0])}
                        />
                    </div>
                    <button
                        className="btn btn-primary btn-sm w-100"
                        onClick={onSubir}
                        disabled={!file || carg}
                    >
                        {carg ? 'Importando…' : '📤 Importar'}
                    </button>
                    {res && (
                        <div className={`alert mt-3 mb-0 py-2 alert-${res.ok ? 'success' : 'danger'}`}>
                            {res.ok ? (
                                <>
                                    <strong>✓ {res.importados} registros importados</strong>
                                    {res.errores?.length > 0 && (
                                        <ul className="mb-0 mt-1 small">
                                            {res.errores.map((e, i) => <li key={i}>{e}</li>)}
                                        </ul>
                                    )}
                                </>
                            ) : (
                                <span>{res.error}</span>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );

    return (
        <div>
            <h5 className="fw-bold mb-4">📥 Importación masiva desde CSV</h5>
            <div className="row g-4">
                {bloque(
                    'Importar Docentes', '👥',
                    'codigoProfesor,nombre,apellidos,email,departamento,tipoFuncionario,antiguedadCentro,notaOposicion,rol,password\nDOC004,María,Pérez,maria@laboral.com,Matemáticas,INTERINO,2,7.5,DOCENTE,1234',
                    fileDocentes, setFileDocentes, resDocentes,
                    () => subir('docentes', fileDocentes, setResDocentes),
                    cargando.docentes
                )}
                {bloque(
                    'Importar Horarios', '📅',
                    'codigoProfesor,diaSemana,tramoHorario,grupoAula\nDOC001,LUNES,1,1DAW-A\nDOC001,MARTES,3,2DAW-B',
                    fileHorarios, setFileHorarios, resHorarios,
                    () => subir('horarios', fileHorarios, setResHorarios),
                    cargando.horarios
                )}
            </div>
            <div className="alert alert-info mt-4 small mb-0">
                <strong>Notas:</strong> tipoFuncionario = CARRERA | PRACTICAS | INTERINO &nbsp;·&nbsp;
                rol = ADMIN | DOCENTE &nbsp;·&nbsp;
                diaSemana = LUNES | MARTES | MIERCOLES | JUEVES | VIERNES &nbsp;·&nbsp;
                tramoHorario = número (1-6). Si el código de docente ya existe, sus datos se actualizan.
            </div>
        </div>
    );
}

// Componente principal
export default function AdminDashboard() {
    const [seccion, setSeccion] = useState('docentes');

    const secciones = [
        { key: 'docentes', label: '👥 Docentes' },
        { key: 'ausencias', label: '📋 Ausencias' },
        { key: 'guardias', label: '🛡️ Guardias' },
        { key: 'asuntos', label: '📅 Asuntos Propios' },
        { key: 'importar', label: '📥 Importar CSV' },
    ];

    return (
        <div className="min-vh-100 bg-light">
            <div className="container-fluid py-3 py-md-4 px-2 px-md-4">
                {/* Movil: desplegable */}
                <div className="d-block d-md-none mb-3">
                    <select
                        className="form-select fw-semibold"
                        value={seccion}
                        onChange={e => setSeccion(e.target.value)}
                    >
                        {secciones.map(s => (
                            <option key={s.key} value={s.key}>{s.label}</option>
                        ))}
                    </select>
                </div>

                {/* Tablet/Escritorio: tabs normales */}
                <div className="d-none d-md-block overflow-auto mb-3">
                    <ul className="nav nav-tabs flex-nowrap">
                        {secciones.map(s => (
                            <li className="nav-item" key={s.key}>
                                <button
                                    className={`nav-link text-nowrap ${seccion === s.key ? 'active fw-bold' : ''}`}
                                    onClick={() => setSeccion(s.key)}
                                >
                                    {s.label}
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="card border-0 shadow-sm p-3 p-md-4">
                    {seccion === 'docentes' && <SeccionDocentes />}
                    {seccion === 'ausencias' && <SeccionAusencias />}
                    {seccion === 'guardias' && <SeccionGuardias />}
                    {seccion === 'asuntos' && <SeccionAsuntosPropios />}
                    {seccion === 'importar' && <SeccionImportar />}
                </div>
            </div>
        </div>
    );
}
