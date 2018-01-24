package pt.ipca.tg_reservas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HistoryFragment extends Fragment {
    private FirebaseAuth mAuth;
    Button buttonLogout;
    ListView listViewHistorico;

    List<Reserva> reservas = new ArrayList<>();
    //{"red.png", "green.png", "orange.png", "blue.png"}
    List<String> listaEstados = new ArrayList<>();
    HistoricoAdapter historicoAdapter;

    int numeroReservas = 0;
    int numeroEmentas = 0;

    // Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mUtilizadores = database.getReference("Utilizadores");
    DatabaseReference mEmentas = database.getReference("Ementas");
    DatabaseReference mNumeroEmentas = database.getReference("NumeroEmentas");
    DatabaseReference mReserva = database.getReference("Reservas");
    DatabaseReference mNumeroReservas = database.getReference("NumeroReservas");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HistoryFragment.this.getView().setBackgroundColor(Color.WHITE);

        mAuth = FirebaseAuth.getInstance();
        buttonLogout = (Button) getView().findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent=new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                HistoryFragment.this.getActivity().finish();
            }
        });

        listViewHistorico = (ListView) view.findViewById(R.id.listViewHistorico);

        listaEstados.add("red.png");
        listaEstados.add("green.png");
        listaEstados.add("blue.png");
        listaEstados.add("orange.png");

        mNumeroReservas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String n = dataSnapshot.getValue(String.class);
                numeroReservas = Integer.parseInt(n);
                //Toast.makeText(HistoryFragment.this.getActivity(), "Foram carregadas " + numeroReservas + " reservas.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mNumeroEmentas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String n = dataSnapshot.getValue(String.class);
                numeroEmentas = Integer.parseInt(n);
                //Toast.makeText(HistoryFragment.this.getActivity(), "Foram carregadas " + numeroEmentas + " ementas.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HistoryFragment.this.getActivity(), "Erro! Por favor verifique a base de dados",Toast.LENGTH_SHORT).show();
            }
        });

        mReserva.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                reservas.clear();

                for (DataSnapshot ds : children) {
                    Reserva reserva = ds.getValue(Reserva.class);

                    if (reserva.getActivo() == 1 && reserva.getAuthUtilizador().equals(mAuth.getCurrentUser().getUid()))
                    {
                        reservas.add(reserva);
                    }
                }

                if (historicoAdapter != null)
                    historicoAdapter.notifyDataSetChanged();
                else
                {
                    historicoAdapter = new HistoricoAdapter();
                    listViewHistorico.setAdapter(historicoAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //reloadDatabase();
    }

    private void reloadDatabase()
    {
        mReserva.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                reservas.clear();

                for (DataSnapshot ds : children) {
                    Reserva reserva = ds.getValue(Reserva.class);

                    if (reserva.getActivo() == 1 && reserva.getAuthUtilizador().equals(mAuth.getCurrentUser().getUid()))
                    {
                        reservas.add(reserva);
                    }
                }
                historicoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class HistoricoAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener {

        LayoutInflater layoutInflater;

        public HistoricoAdapter() {
            layoutInflater = (LayoutInflater) HistoryFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return reservas.size();
        }

        @Override
        public Object getItem(int i) {
            return reservas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view==null){
                view=layoutInflater.inflate(R.layout.historico_row,null);
            }

            TextView textViewData = (TextView) view.findViewById(R.id.textViewData);
            TextView textViewHora = (TextView) view.findViewById(R.id.textViewHora);
            TextView textViewNumeroPessoas = (TextView) view.findViewById(R.id.textViewNumeroPessoas);
            TextView textViewPratos = (TextView) view.findViewById(R.id.textViewPratos);
            final ImageView imageView = (ImageView) view.findViewById(R.id.imageViewEstado);

            textViewData.setText(reservas.get(i).getData());
            textViewHora.setText(reservas.get(i).getHora() + " horas");
            textViewNumeroPessoas.setText(reservas.get(i).getNumeroPessoas() + " pessoa(s)");

            String listaPratos = reservas.get(i).getListaPratos();
            String[] split = listaPratos.split("-");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < split.length; j++)
            {
                sb.append(split[j]);
                if (j != split.length - 1 && j != 0)
                {
                    sb.append("\n");
                }
            }
            String joined = sb.toString();
            textViewPratos.setText(joined);

            int estado = reservas.get(i).getAceite();

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageReference = mStorageRef.child("images/" + listaEstados.get(estado).toString());
            imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(HistoryFragment.this.getActivity()).load(uri).resize(10, 10).into(imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(HistoryFragment.this.getActivity(), "Erro a carregar a imagem!",Toast.LENGTH_SHORT).show();
                }
            });

            view.setTag(new Integer(i));
            view.setClickable(true);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            return view;
        }

        @Override
        public void onClick(View view) {
            final int position = (Integer) view.getTag();
        }

        @Override
        public boolean onLongClick(View v) {
            final int position = (Integer) v.getTag();
            final AlertDialog.Builder builder = new AlertDialog.Builder(HistoryFragment.this.getActivity());

            builder.setCancelable(true);
            builder.setTitle("Eliminar RESERVA");
            builder.setMessage("Deseja eliminar a sua reserva?");
            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String data = reservas.get(position).getData();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date strDate = null;
                    try {
                        strDate = sdf.parse(data);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (new Date(System.currentTimeMillis()-24*60*60*1000).after(strDate))
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(HistoryFragment.this.getActivity());
                        alert.setCancelable(true);
                        alert.setTitle(" ERRO ");
                        alert.setMessage("Não é possivel rejeitar uma reserva antiga");
                        alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        AlertDialog alertDialog = alert.create();
                        alertDialog.show();
                    }
                    else
                    {
                        Reserva alterarReserva;

                        alterarReserva = new Reserva(reservas.get(position).getId(), reservas.get(position).getAuthUtilizador(),
                                reservas.get(position).getNome(), reservas.get(position).getNumeroPessoas(), reservas.get(position).getData(),
                                reservas.get(position).getHora(), reservas.get(position).listaPratos, reservas.get(position).getAceite(), 0);

                        mReserva.child(reservas.get(position).getId()).setValue(alterarReserva);
                        Toast.makeText(HistoryFragment.this.getActivity(), "Reserva rejeitada com sucesso",Toast.LENGTH_SHORT).show();

                        notifyDataSetChanged();
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return false;
        }
    }
}
