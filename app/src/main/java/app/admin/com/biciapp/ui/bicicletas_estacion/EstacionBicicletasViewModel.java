package app.admin.com.biciapp.ui.bicicletas_estacion;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import app.admin.com.biciapp.datos.repos.BicicletasRepository;
import app.admin.com.biciapp.datos.modelos.Bicicleta;

public class EstacionBicicletasViewModel extends ViewModel {
    private LiveData<List<Bicicleta>> bicicletas;
    private BicicletasRepository bicicletasRepo;

    public EstacionBicicletasViewModel(BicicletasRepository bicicletasRepo) {
        this.bicicletasRepo = bicicletasRepo;
        this.bicicletas = bicicletasRepo.getData();
    }

    //Para llamar
    public void obtenerBicicletas(String idEstacion) {
        bicicletas = bicicletasRepo.getBicicletas(idEstacion);
    }

    //Para observar
    public LiveData<List<Bicicleta>> observarBicicletas() {
        return this.bicicletas;
    }

}
