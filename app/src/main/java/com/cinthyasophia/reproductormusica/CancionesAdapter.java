package com.cinthyasophia.reproductormusica;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CancionesAdapter extends RecyclerView.Adapter<CancionesAdapter.CancionesHolder> implements View.OnClickListener
{
    private ArrayList<Cancion> canciones;
    private View.OnClickListener listener;

    public CancionesAdapter(ArrayList<Cancion> canciones) {
        this.canciones = canciones;
    }

    @NonNull
    @Override
    public CancionesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.cancion_item,parent,false);
        item.setOnClickListener(this);
        return new CancionesHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull CancionesHolder holder, int position) {
        Cancion cancion = canciones.get(position);
        holder.bindCancion(cancion);

    }

    @Override
    public int getItemCount() {
        return canciones.size();
    }
    public void swap(ArrayList<Cancion> canciones) {
        this.canciones = canciones;
        notifyDataSetChanged();
    }
    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener!=null){
            listener.onClick(v);
        }

    }

    public class CancionesHolder extends RecyclerView.ViewHolder{
        private TextView tvNombreCancion;

        public CancionesHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCancion = itemView.findViewById(R.id.tvNombreCancion);

        }

        public void bindCancion(Cancion cancion){
            tvNombreCancion.setText(cancion.getNombre());
        }


    }
}
