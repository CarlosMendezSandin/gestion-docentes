package gestion_docentes_back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionService {

    private int maxAsuntosPropiosDia;

    public ConfiguracionService(@Value("${app.max-asuntos-dia:3}") int maxAsuntosPropiosDia) {
        this.maxAsuntosPropiosDia = maxAsuntosPropiosDia;
    }

    public int getMaxAsuntosPropiosDia() {
        return maxAsuntosPropiosDia;
    }

    public void setMaxAsuntosPropiosDia(int max) {
        if (max < 1) throw new IllegalArgumentException("El máximo debe ser al menos 1");
        this.maxAsuntosPropiosDia = max;
    }
}
