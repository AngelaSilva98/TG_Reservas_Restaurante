package pt.ipca.tg_reservas;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class PratosFragment extends Fragment {
    DatePickerDialog datePickerDialog;
    EditText editTextDataEmenta;

    Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR); // current year
    int mMonth = c.get(Calendar.MONTH); // current month
    int mDay = c.get(Calendar.DAY_OF_MONTH); // current day

    int numeroEmentas = 0;
    String ementaID = "-1";

    EmentaAdapter ementaAdapter;
    ListView listViewEmenta;
    List<Prato> pratosEmenta = new ArrayList<>();

    // Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mEmentas = database.getReference("Ementas");
    DatabaseReference mNumeroEmentas = database.getReference("NumeroEmentas");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pratos, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PratosFragment.this.getView().setBackgroundColor(Color.WHITE);

        editTextDataEmenta = (EditText) view.findViewById(R.id.editTextDataEmenta);
        listViewEmenta = (ListView) view.findViewById(R.id.listViewEmenta);

        editTextDataEmenta.setText(String.format("%02d/%02d/%04d", mDay, (mMonth + 1), mYear));

        editTextDataEmenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                editTextDataEmenta.setText(String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year));
                                PratosFragment.this.getView().setBackgroundColor(Color.WHITE);
                                reloadDatabase();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


        if (Build.VERSION.SDK_INT >= 11)
        {
            editTextDataEmenta.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editTextDataEmenta.setTextIsSelectable(true);
        }
        else
        {
            editTextDataEmenta.setRawInputType(InputType.TYPE_NULL);
            editTextDataEmenta.setFocusable(true);
        }

        mNumeroEmentas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String n = dataSnapshot.getValue(String.class);
                numeroEmentas = Integer.parseInt(n);
                //Toast.makeText(PratosFragment.this.getActivity(), "Foram carregadas " + numeroEmentas + " ementas.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PratosFragment.this.getActivity(), "Erro! Por favor verifique a base de dados",Toast.LENGTH_SHORT).show();
            }
        });

        mEmentas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                pratosEmenta.clear();
                ementaID = "-1";

                for (DataSnapshot ds : children) {
                    Ementa ementa = ds.getValue(Ementa.class);

                    if (ementa.getData().equals(editTextDataEmenta.getText().toString()))
                    {
                        ementaID = ementa.getId();
                        pratosEmenta = ementa.getPratos();
                    }
                }

                if (!ementaID.equals("-1"))
                {
                    if (ementaAdapter != null)
                        ementaAdapter.notifyDataSetChanged();
                    else
                    {
                        ementaAdapter = new EmentaAdapter();
                        listViewEmenta.setAdapter(ementaAdapter);
                    }
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
        mEmentas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                pratosEmenta.clear();
                ementaID = "-1";

                for (DataSnapshot ds : children) {
                    Ementa ementa = ds.getValue(Ementa.class);

                    if (ementa.getData().equals(editTextDataEmenta.getText().toString()))
                    {
                        ementaID = ementa.getId();
                        pratosEmenta = ementa.getPratos();
                    }
                }

                if (!ementaID.equals("-1"))
                {
                    ementaAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class EmentaAdapter extends BaseAdapter implements View.OnClickListener {

        LayoutInflater layoutInflater;

        public EmentaAdapter() {
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return pratosEmenta.size();
        }

        @Override
        public Object getItem(int i) {
            return pratosEmenta.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view==null){
                view=layoutInflater.inflate(R.layout.pratos_row,null);
            }

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageReference = mStorageRef.child("images/" + pratosEmenta.get(i).getId());

            TextView textViewNome = (TextView) view.findViewById(R.id.textViewNomePrato);
            TextView textViewPreco = (TextView) view.findViewById(R.id.textViewPreco);
            TextView textViewDescricao = (TextView) view.findViewById(R.id.textViewDescricao);
            final ImageView imageView = (ImageView) view.findViewById(R.id.imageViewPrato);

            textViewNome.setText(pratosEmenta.get(i).getNome());
            textViewPreco.setText("€" + pratosEmenta.get(i).getPreco());
            textViewDescricao.setText(pratosEmenta.get(i).getDescricao());

            imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(PratosFragment.this.getActivity()).load(uri).fit().centerCrop().into(imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(PratosFragment.this.getActivity(), "Erro a carregar a imagem!",Toast.LENGTH_SHORT).show();
                }
            });

            view.setTag(new Integer(i));
            view.setClickable(true);
            view.setOnClickListener(this);

            return view;
        }

        @Override
        public void onClick(View view) {
            final int position = (Integer) view.getTag(); // Posicao do cliente escolhido/clicado no array
        }
    }
}