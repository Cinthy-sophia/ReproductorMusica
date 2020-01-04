package com.cinthyasophia.reproductormusica;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class ReproductorService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "ReproductorService";
    private final int CANTIDAD_CANCIONES = 5;
    private ArrayList<Cancion> canciones;
    private final IBinder mBinder= new MusicBinder();
    private MediaPlayer reproductor;
    private int posicionCancion;
    private int progress;

    public ReproductorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
        canciones = leerCanciones();
        posicionCancion = 0;
        progress=0;
        reproductor = MediaPlayer.create(ReproductorService.this,canciones.get(posicionCancion).getId());
        reproductor.setOnCompletionListener(ReproductorService.this);
        reproductor.setOnPreparedListener(ReproductorService.this);
        reproductor.setOnErrorListener(ReproductorService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        reproductor.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        return START_STICKY;
    }

    public void seekToPos(int msec){
        reproductor.seekTo(msec*1000);
    }

    public void play() {
        if (posicionCancion == canciones.size()){
            posicionCancion = 0;
        }

        AssetFileDescriptor afd = this.getResources().openRawResourceFd(canciones.get(posicionCancion).getId());

        try {
            reproductor.reset();
            reproductor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            reproductor.prepare();
            afd.close();
            if (progress!=0){
                reproductor.seekTo(progress);
            }
            reproductor.start();


            if(posicionCancion == 0){
                posicionCancion = canciones.size()-1;
            }


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


    public ArrayList<Cancion> getCanciones() {
        return canciones;
    }
    public int getDuracionCancion(){
        return reproductor.getDuration();
    }
    public int getCurrentPosition(){
        if (reproductor.getCurrentPosition()== reproductor.getDuration()){
            posicionCancion++;
        }
        return reproductor.getCurrentPosition();
    }
    public void nextOrPrevSong(String accion){

        switch (accion){
            case "Next":
                posicionCancion++;

                break;
            case "Prev":
                posicionCancion--;

                break;

            default:

                break;
        }

        play();
    }
    public void pauseSong(int progress){
        reproductor.pause();
        this.progress = progress *1000;

    }
    public void repSong(Cancion cancion){
        for (Cancion c : canciones) {
            if (c.getId() == cancion.getId()) {
                posicionCancion = canciones.indexOf(c);
            }
        }
        progress = 0;
        play();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    private ArrayList<Cancion> leerCanciones(){
        MediaPlayer mp;
        ArrayList<Cancion> canciones = new ArrayList<>();
        int idCancion;
        for (int i =1; i <=CANTIDAD_CANCIONES ; i++) {
            idCancion = getResources().getIdentifier("raw/"+"s"+i,null,getPackageName());
            mp = MediaPlayer.create(this,idCancion);
            canciones.add(new Cancion(idCancion,"s"+i,mp.getDuration()));
        }
        return canciones;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        play();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        play();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG,"onPrepared");
    }

    public class MusicBinder extends Binder {
        public ReproductorService getService(){
            return ReproductorService.this;
        }
    }
}
