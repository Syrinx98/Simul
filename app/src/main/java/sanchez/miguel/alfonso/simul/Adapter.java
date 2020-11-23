package sanchez.miguel.alfonso.simul;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    List<String> nomi;
    List<Integer> immagini;
    LayoutInflater inflater;

    public Adapter(Context ctx, List<String> nomi, List<Integer> immagini){
        this.nomi = nomi;
        this.immagini = immagini;
        this.inflater = LayoutInflater.from(ctx);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.lobby_item_grid_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nome.setText(nomi.get(position));
        holder.immagine.setImageResource(immagini.get(position));
    }

    @Override
    public int getItemCount() {
        return nomi.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView nome;
        ImageView immagine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.lobby_grid_item_nick);
            immagine = itemView.findViewById(R.id.lobby_grid_item_img);
        }
    }
}
