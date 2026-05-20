import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Login from './views/Login';
import CambiarPassword from './views/CambiarPassword';
import AdminDashboard from './views/AdminDashboard';
import DocenteDashboard from './views/DocenteDashboard';

function RutaProtegida({ children, soloAdmin = false }) {
    const { usuario, isAdmin } = useAuth();
    if (!usuario) return <Navigate to="/login" replace />;
    if (soloAdmin && !isAdmin()) return <Navigate to="/asuntos-propios" replace />;
    return (
        <>
            <Navbar />
            {children}
        </>
    );
}

function RutaRaiz() {
    const { usuario, isAdmin } = useAuth();
    if (!usuario) return <Navigate to="/login" replace />;
    return <Navigate to={isAdmin() ? '/admin' : '/asuntos-propios'} replace />;
}

export default function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/cambiar-password" element={
                        <RutaProtegida>
                            <CambiarPassword />
                        </RutaProtegida>
                    } />
                    <Route path="/" element={<RutaRaiz />} />
                    <Route path="/admin" element={
                        <RutaProtegida soloAdmin>
                            <AdminDashboard />
                        </RutaProtegida>
                    } />
                    <Route path="/asuntos-propios" element={
                        <RutaProtegida>
                            <DocenteDashboard />
                        </RutaProtegida>
                    } />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
