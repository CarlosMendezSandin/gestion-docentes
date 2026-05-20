import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [usuario, setUsuario] = useState(() => {
        try {
            const stored = localStorage.getItem('user');
            return stored ? JSON.parse(stored) : null;
        } catch {
            return null;
        }
    });

    const login = (userData) => {
        setUsuario(userData);
        localStorage.setItem('user', JSON.stringify(userData));
    };

    const logout = () => {
        setUsuario(null);
        localStorage.removeItem('user');
    };

    const isAdmin = () => usuario?.rol === 'ADMIN';

    return (
        <AuthContext.Provider value={{ usuario, login, logout, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
    return ctx;
}
