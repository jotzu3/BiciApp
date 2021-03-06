package app.admin.com.biciapp.ui.procesando_retiro;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import app.admin.com.biciapp.ui.bicicletas_estacion.EstacionBicicletasActivity;
import app.admin.com.biciapp.ui.instrucciones_devolucion.InstruccionesDevolucionActivity;
import app.admin.com.biciapp.ui.instrucciones_retiro.InstruccionRetiroViewModelFactory;
import app.admin.com.biciapp.ui.procesando_entrega.ProcesandoEntregaViewModelFactory;
import app.admin.com.biciapp.R;
import app.admin.com.biciapp.datos.modelos.BiciCandado;
import app.admin.com.biciapp.datos.modelos.Bicicleta;
import app.admin.com.biciapp.datos.modelos.Candado;
import app.admin.com.biciapp.datos.modelos.Estacion;
import app.admin.com.biciapp.datos.modelos.Reserva;
import app.admin.com.biciapp.ui.RodandoBiciActivity;
import app.admin.com.biciapp.ui.instrucciones_retiro.InstruccionesRetiroViewModel;
import app.admin.com.biciapp.ui.login.LoginClienteView;
import app.admin.com.biciapp.ui.procesando_entrega.ProcesandoEntregaViewModel;

import static app.admin.com.biciapp.ui.login.LoginActivity.CLIENT_VIEW;
import static app.admin.com.biciapp.ui.map_estaciones.MapsActivity.ESTACION_VIEW;
import static app.admin.com.biciapp.ui.reserva.ReservaActivity.RESERVA_VIEW;

public class ProcesandoRetiroActivity extends AppCompatActivity {

    private LoginClienteView clienteView;
    private Candado candadoView;
    private Bicicleta bicicletaView;
    ProcesandoEntregaViewModel viewModel;
    private Estacion estacionView;
    private Reserva reservaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procesando_retiro);

        Intent intent = getIntent();
        clienteView = (LoginClienteView) intent.getSerializableExtra(CLIENT_VIEW);
        bicicletaView = (Bicicleta) intent.getSerializableExtra(EstacionBicicletasActivity.BICICLETA_VIEW);
        estacionView = (Estacion) intent.getSerializableExtra(ESTACION_VIEW);
        reservaView = (Reserva) intent.getSerializableExtra(RESERVA_VIEW);
        candadoView = (Candado)intent.getSerializableExtra(InstruccionesDevolucionActivity.CANDADO_VIEW);

        viewModel = ViewModelProviders.of(this, new ProcesandoEntregaViewModelFactory())
                .get(ProcesandoEntregaViewModel.class);

        viewModel.checkLastTransaccion(bicicletaView.getId());
        viewModel.getLastTransaccion().observe(this, new Observer<BiciCandado>() {
            @Override
            public void onChanged(@Nullable BiciCandado biciCandado) {
                if (biciCandado != null && biciCandado.getEntregaRetiro() && biciCandado.getError() == null) {
                    viewModel.getLastTransaccion().removeObserver(this);
                    BiciCandado transaccionParcial = new BiciCandado();
                    transaccionParcial.setError("Retiro parcial");
                    transaccionParcial.setIdCandado(candadoView.getId());
                    transaccionParcial.setEntregaRetiro(true);
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String sdt = df.format(new Date(System.currentTimeMillis()));
                    transaccionParcial.setFechaHora(sdt);
                    transaccionParcial.setFechaHora(sdt);
                    transaccionParcial.setIdBici(bicicletaView.getId());
                    viewModel.realizarTransaccionParcial(transaccionParcial);

                }else if(!biciCandado.getEntregaRetiro() && !reservaView.isActiva()) {

                    new AlertDialog.Builder(ProcesandoRetiroActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Confirmación")
                            .setMessage("La bicicleta que intenta retirar aún no ha sido entregada o ha tenido un problema. ¿Desea que se le asigne otra bicicleta?")
                            .setPositiveButton("Cambiar de bicicleta", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    handler.removeCallbacks(runnable);
                                    Toast.makeText(ProcesandoRetiroActivity.this, "Implementar", Toast.LENGTH_LONG).show();

                                }
                            })
                            .setNegativeButton("Anular reserva", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    handler.removeCallbacks(runnable);
                                    Toast.makeText(ProcesandoRetiroActivity.this, "Implementar", Toast.LENGTH_LONG).show();
                                }
                            })
                            .show();
                }
                else{
                    viewModel.getLastTransaccion().removeObserver(this);
                    /*handler.postDelayed(runnable = new Runnable() {
                        public void run() {
                            if(!intetar_retirar_bici()){
                                contador++;
                                if (contador >= 40) {
                                    handler.removeCallbacks(runnable); //parar el handler cuando ha intentando por un tiempo
                                    Toast.makeText(getApplicationContext(), "No se ha podido retirar la bicicleta", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                                handler.postDelayed(runnable, delay);
                            }
                        }
                    }, delay);
                     */
                }
            }
        });


        viewModel.getTransaccion().observe(this, new Observer<BiciCandado>() {
            @Override
            public void onChanged(@Nullable BiciCandado biciCandado) {
                if (biciCandado != null) {
                    if (biciCandado.getError() != null || biciCandado.getEntregaRetiro()) {
                        viewModel.getTransaccion().removeObserver(this);
                        handler.postDelayed(runnable = new Runnable() {
                            public void run() {
                                if(!intetar_retirar_bici()){
                                    contador++;
                                    if (contador >= 40) {
                                        handler.removeCallbacks(runnable); //parar el handler cuando ha intentando por un tiempo
                                        Toast.makeText(getApplicationContext(), "No se ha podido retirar la bicicleta", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                    handler.postDelayed(runnable, delay);
                                }

                            }
                        }, delay);

                    }
                }else{
                    viewModel.getTransaccion().removeObserver(this);
                }
            }
        });
    }


    Handler handler = new Handler();
    Runnable runnable;
    int delay = 2 * 1000; //Delay for 2 seconds.  One second = 1000 milliseconds.
    int contador = 0;
    boolean respuesta = false;

    private synchronized boolean intetar_retirar_bici() {

        final InstruccionesRetiroViewModel viewModel = ViewModelProviders.of(this, new InstruccionRetiroViewModelFactory())
                .get(InstruccionesRetiroViewModel.class);
        viewModel.obtenerBiciEstacion(bicicletaView.getId());
        viewModel.getBiciEstacion().observe(this, new Observer<BiciCandado>() {
            @Override
            public void onChanged(@Nullable BiciCandado biciCandado) {
                if(biciCandado!= null) {
                    if (!biciCandado.getEntregaRetiro() && biciCandado.getError() == null && biciCandado.getStatusEntregaRecepcion()) {
                        viewModel.getBiciEstacion().removeObserver(this);
                        Toast.makeText(getApplicationContext(), "Bicicleta retirada con éxito!", Toast.LENGTH_LONG).show();
                        handler.removeCallbacks(runnable);
                        Log.d("Testeando","ando");
                        respuesta = true;
                        finish();
                        Intent intent = new Intent(getApplicationContext(), RodandoBiciActivity.class);
                        intent.putExtra(ESTACION_VIEW, estacionView);
                        intent.putExtra(CLIENT_VIEW, clienteView);
                        intent.putExtra(RESERVA_VIEW,  reservaView);
                        intent.putExtra(EstacionBicicletasActivity.BICICLETA_VIEW, bicicletaView);
                        startActivity(intent);
                    }else{
                        viewModel.getBiciEstacion().removeObserver(this);
                    }
                }else{
                    viewModel.getBiciEstacion().removeObserver(this);
                }

            }
        });
        return respuesta;
    }


// If onPause() is not included the threads will double up when you
// reload the activity

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }


}
