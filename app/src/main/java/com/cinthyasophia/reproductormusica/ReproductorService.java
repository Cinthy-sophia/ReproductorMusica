package com.cinthyasophia.reproductormusica;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ReproductorService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "ReproductorService";
    private ArrayList<Cancion> canciones;
    private final IBinder mBinder= new MusicBinder();
    private MediaPlayer reproductor;
    private int posicionCancion;
    private int progress;
    private Cancion cancionActual;

    public ReproductorService() {
    }

    //Inicializamos los atributos necesarios para que funcione el servicio.
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
        canciones = leerCanciones(); // Inicializo el array con las canciones a reproducir
        posicionCancion = 0;
        progress=0;
        reproductor = MediaPlayer.create(ReproductorService.this,canciones.get(posicionCancion).getId());
        reproductor.setOnCompletionListener(ReproductorService.this);
        reproductor.setOnPreparedListener(ReproductorService.this);
        reproductor.setOnErrorListener(ReproductorService.this);
        cancionActual = new Cancion();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        reproductor.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK); // Para mantener la pantalla encendida
        return START_STICKY;
    }

    /**
     * Mueve la cancionActual en reproducion al punto que le indiquen los milisegundos que recibe
     * @param msec
     */
    public void seekToPos(int msec){
        reproductor.seekTo(msec*1000);
    }

    /**
     * Se encarga de reproducir la cancionActual
     */
    public void play() {
        //Leemos el archivo
        AssetFileDescriptor afd = this.getResources().openRawResourceFd(canciones.get(posicionCancion).getId());

        try {
            //Paramos el reproductor, y luego lo reiniciamos
            reproductor.stop();
            reproductor.reset();
            //Con el AssetFileDescriptor de arriba le indicamos los datos necesarios al reproductor para que pueda reproducir la cancionActual
            reproductor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            reproductor.prepare();
            if (progress!=0){
                //Si el reproductor ha sido pausado vuelve a iniciar la reproduccion en el punto indicado
                reproductor.seekTo(progress);
            }

            afd.close();
            reproductor.start(); //Inicia la reproduccion
            cancionActual = canciones.get(posicionCancion);

        }
        catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException: " + e.getMessage());
        }
        catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException: " + e.getMessage());
        }
        catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    /**
     * Devuelve la lista de canciones
     * @return
     */
    public ArrayList<Cancion> getCanciones() {
        return canciones;
    }

    /**
     * Devuelve la duracion de la cancionActual reproduciendose
     * @return
     */
    public int getDuracionCancion(){
        return reproductor.getDuration();
    }

    /**
     * Devuelve la posicion actual del reproductor
     * @return
     */
    public int getCurrentPosition(){
        return reproductor.getCurrentPosition();
    }

    /**
     * Segun el String que recibe cambia el atributo de posicion aumentandolo o disminuyendo,
     * y luego llama a play() para la reproduccion
     * @param accion
     */
    public void nextOrPrevSong(String accion){

        switch (accion){
            case "Next":
                if (posicionCancion == canciones.size()-1){
                    posicionCancion = 0;
                }else{
                    posicionCancion++;
                }

                break;
            case "Prev":
                if(posicionCancion == 0){
                    posicionCancion = canciones.size()-1;
                }else{
                    posicionCancion--;
                }
                break;

            default:

                break;
        }

        play();
    }

    /**
     * Para la cancionActual y actualiza el atributo de progreso a el entero que recibe
     * @param progress
     */
    public void pauseSong(int progress){
        reproductor.pause();
        this.progress = progress *1000;

    }
    public Cancion cancionActual(){
        return cancionActual;
    }

    /**
     * Reproduce la cancionActual recibida, indicada por el usuario
     * @param cancion
     */
    public void repSong(Cancion cancion){
        for (Cancion c : canciones) {
            if (c.getId() == cancion.getId()) {
                posicionCancion = canciones.indexOf(c);
            }
        }
        progress = 0;
        play();
    }

    /**
     * Nos permite comunicarnos con el servicio
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    /**
     * Lee las canciones de la carpeta raw y las guarda en el arraylist
     * @return
     */
    private ArrayList<Cancion> leerCanciones(){
        Field[] fields = R.raw.class.getFields();
        MediaPlayer mp;
        ArrayList<Cancion> canciones = new ArrayList<>();
        int idCancion=0;
        for (int i =0; i < fields.length; i++) {
            try {
                idCancion = fields[i].getInt(fields[i]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            mp = MediaPlayer.create(this,idCancion);
            canciones.add(new Cancion(idCancion,getResources().getResourceEntryName(idCancion),mp.getDuration()));
        }
        return canciones;
    }

    /**
     * Al terminar la cancionActual en reproduccion aumenta el atributo de posicion y
     * y comprueba que no se encuentra al final o al inicio del array y hace las
     * acciones correspondientes en cada caso
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        posicionCancion++;
        if(posicionCancion == 0){
            posicionCancion = canciones.size()-1;
        }else  if (posicionCancion == canciones.size()){
            posicionCancion = 0;
        }
        play();
    }

    /**
     * En caso de error ejecuta este método
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        play();
        return false;
    }

    /**
     *En cuanto el reproductor esté listo ejecuta este método
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG,"onPrepared");

    }

    /**
     * Retorna una instancia del servicio
     */
    public class MusicBinder extends Binder {
        public ReproductorService getService(){
            return ReproductorService.this;
        }
    }
}
