package pt.ipca.tg_reservas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class ReservasFragment extends Fragment {
    DatePickerDialog datePickerDialog;
    EditText editTextNome, editTextNumeroPessoas, editTextData, editTextHora;

    Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR); // current year
    int mMonth = c.get(Calendar.MONTH); // current month
    int mDay = c.get(Calendar.DAY_OF_MONTH); // current day

    int numeroEmentas = 0;
    String ementaID = "-1";
    boolean existeReserva = false;

    String nome;
    int numeroReservas = 0;

    EmentaAdapter ementaAdapter;
    ListView listViewEmenta;
    List<Prato> pratosEmenta = new ArrayList<>();
    List<Boolean> pratosSelecionados = new ArrayList<>();

    Button btnReservar;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mUtilizadores = database.getReference("Utilizadores");
    DatabaseReference mEmentas = database.getReference("Ementas");
    DatabaseReference mNumeroEmentas = database.getReference("NumeroEmentas");
    DatabaseReference mReserva = database.getReference("Reservas");
    DatabaseReference mNumeroReservas = database.getReference("NumeroReservas");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reservas, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ReservasFragment.this.getView().setBackgroundColor(Color.WHITE);

        mAuth = FirebaseAuth.getInstance();

        editTextNome = (EditText) view.findViewById(R.id.editTextNome);
        editTextNumeroPessoas = (EditText) view.findViewById(R.id.editTextNumeroPessoas);
        editTextData = (EditText) view.findViewById(R.id.editTextData);
        editTextHora = (EditText) view.findViewById(R.id.editTextHora);
        listViewEmenta = (ListView) view.findViewById(R.id.listViewPratos);
        btnReservar = (Button) view.findViewById(R.id.btnReservar);

        editTextData.setText(String.format("%02d/%02d/%04d", mDay, (mMonth + 1), mYear));

        editTextData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                editTextData.setText(String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year));
                                ReservasFragment.this.getView().setBackgroundColor(Color.WHITE);
                                reloadDatabase();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        EditText editTextFromTime = (EditText) view.findViewById(R.id.editTextHora);
        final SetTime fromTime = new SetTime(editTextFromTime, ReservasFragment.this.getActivity());

        editTextHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });


        if (Build.VERSION.SDK_INT >= 11)
        {
            editTextData.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editTextData.setTextIsSelectable(true);
            editTextHora.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editTextHora.setTextIsSelectable(true);
        }
        else
        {
            editTextData.setRawInputType(InputType.TYPE_NULL);
            editTextData.setFocusable(true);
            editTextHora.setRawInputType(InputType.TYPE_NULL);
            editTextHora.setFocusable(true);
        }

        btnReservar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String listaPratos;
                boolean pratos;

                listaPratos = "";
                pratos = false;
                for (int i = 0; i < pratosEmenta.size(); i++)
                {
                    boolean selecionado = pratosSelecionados.get(i).booleanValue();

                    if (selecionado)
                    {
                        listaPratos += "-" + pratosEmenta.get(i).getNome();
                        pratos = true;
                    }
                }

                Reserva reserva = new Reserva(String.valueOf(numeroReservas), mAuth.getCurrentUser().getUid(),
                        editTextNome.getText().toString(), editTextNumeroPessoas.getText().toString(),
                        editTextData.getText().toString(), editTextHora.getText().toString(), listaPratos, 2, 1);

                String data = editTextData.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date strDate = null;
                try {
                    strDate = sdf.parse(data);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (editTextNome.getText().toString().equals(""))
                    Toast.makeText(ReservasFragment.this.getActivity(), "Erro! Insira um nome",Toast.LENGTH_SHORT).show();
                else if (editTextNumeroPessoas.getText().toString().equals(""))
                    Toast.makeText(ReservasFragment.this.getActivity(), "Erro! Insira o numero de pessoas",Toast.LENGTH_SHORT).show();
                else if (editTextData.getText().toString().equals(""))
                    Toast.makeText(ReservasFragment.this.getActivity(), "Erro! Insira a data",Toast.LENGTH_SHORT).show();
                else if (editTextHora.getText().toString().equals(""))
                    Toast.makeText(ReservasFragment.this.getActivity(), "Erro! Insira uma hora",Toast.LENGTH_SHORT).show();
                else if (new Date(System.currentTimeMillis()-24*60*60*1000).after(strDate))
                    Toast.makeText(ReservasFragment.this.getActivity(), "Erro! Não é possivel reservar uma data antiga",Toast.LENGTH_SHORT).show();
                else if (!pratos)
                    Toast.makeText(ReservasFragment.this.getActivity(), "Erro! Insira os pratos",Toast.LENGTH_SHORT).show();
                else if (existeReserva)
                    Toast.makeText(ReservasFragment.this.getActivity(), "Erro! Já existe uma reserva nesse dia",Toast.LENGTH_SHORT).show();
                else
                {
                    mReserva.child(String.valueOf(numeroReservas)).setValue(reserva);
                    Toast.makeText(ReservasFragment.this.getActivity(), "Reserva feita com sucesso",Toast.LENGTH_SHORT).show();

                    numeroReservas++;
                    mNumeroReservas.setValue(String.valueOf(numeroReservas));
                }
            }
        });

        mUtilizadores.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot ds : children) {
                    Utilizador utilizador = ds.getValue(Utilizador.class);

                    if (utilizador.getId().equals(mAuth.getCurrentUser().getUid()))
                    {
                        nome = utilizador.getNome();
                    }
                }

                editTextNome.setText(nome);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mNumeroReservas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String n = dataSnapshot.getValue(String.class);
                numeroReservas = Integer.parseInt(n);
                //Toast.makeText(ReservasFragment.this.getActivity(), "Foram carregadas " + numeroReservas + " reservas.",Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(ReservasFragment.this.getActivity(), "Foram carregadas " + numeroEmentas + " ementas.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ReservasFragment.this.getActivity(), "Erro! Por favor verifique a base de dados",Toast.LENGTH_SHORT).show();
            }
        });

        mEmentas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                pratosEmenta.clear();
                pratosSelecionados.clear();

                for (DataSnapshot ds : children) {
                    Ementa ementa = ds.getValue(Ementa.class);

                    if (ementa.getData().equals(editTextData.getText().toString()))
                    {
                        ementaID = ementa.getId();
                        pratosEmenta = ementa.getPratos();
                    }
                }


                for (int i = 0; i < pratosEmenta.size(); i++)
                {
                    pratosSelecionados.add(false);
                }

                if (ementaAdapter != null)
                    ementaAdapter.notifyDataSetChanged();
                else
                {
                    ementaAdapter = new EmentaAdapter();
                    listViewEmenta.setAdapter(ementaAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mReserva.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                existeReserva = false;

                for (DataSnapshot ds : children) {
                    Reserva reserva = ds.getValue(Reserva.class);

                    if (reserva.getActivo() == 1 && reserva.getAuthUtilizador().equals(mAuth.getCurrentUser().getUid()) && reserva.getData().equals(editTextData.getText().toString()))
                    {
                        existeReserva = true;
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
                pratosSelecionados.clear();

                for (DataSnapshot ds : children) {
                    Ementa ementa = ds.getValue(Ementa.class);

                    if (ementa.getData().equals(editTextData.getText().toString()))
                    {
                        ementaID = ementa.getId();
                        pratosEmenta = ementa.getPratos();
                    }
                }


                for (int i = 0; i < pratosEmenta.size(); i++)
                {
                    pratosSelecionados.add(false);
                }

                ementaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mReserva.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                existeReserva = false;

                for (DataSnapshot ds : children) {
                    Reserva reserva = ds.getValue(Reserva.class);

                    if (reserva.getActivo() == 1 && reserva.getAuthUtilizador().equals(mAuth.getCurrentUser().getUid()) && reserva.getData().equals(editTextData.getText().toString()))
                    {
                        existeReserva = true;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class EmentaAdapter extends BaseAdapter implements View.OnClickListener {

        LayoutInflater layoutInflater;
        CheckBox checkBox;

        public EmentaAdapter() {
            layoutInflater = (LayoutInflater) ReservasFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        public View getView(final int i, View view, ViewGroup viewGroup) {

            if (view==null){
                view=layoutInflater.inflate(R.layout.pratos_reservar_row,null);
            }

            checkBox = (CheckBox) view.findViewById(R.id.checkBox);

            checkBox.setTag(new Integer(i));
            checkBox.setText(pratosEmenta.get(i).getNome());

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    for (int i = 0; i < pratosEmenta.size(); i++)
                    {
                        if (pratosEmenta.get(i).getNome().equals(buttonView.getText().toString()))
                            pratosSelecionados.set(i, isChecked);
                    }
                    //Toast.makeText(ReservasFragment.this.getActivity(), "Checkbox " + buttonView.getText().toString() + " selected? " + isChecked,Toast.LENGTH_SHORT).show();
                }
            });

            view.setTag(new Integer(i));
            view.setClickable(true);
            view.setOnClickListener(this);

            return view;
        }

        @Override
        public void onClick(View view) {
            final int position = (Integer) view.getTag();
        }
    }

    class SetTime implements View.OnFocusChangeListener, TimePickerDialog.OnTimeSetListener {

        private EditText editText;
        private Calendar myCalendar;
        private Context ctx;

        public SetTime(EditText editText, Context ctx){
            this.editText = editText;
            this.editText.setOnFocusChangeListener(this);
            this.myCalendar = Calendar.getInstance();
            this.ctx = ctx;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // TODO Auto-generated method stub
            if(hasFocus){
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                new TimePickerDialog(ctx, this, hour, minute, true).show();
            }
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // TODO Auto-generated method stub
            this.editText.setText(String.format("%02d:%02d", hourOfDay, minute));
        }

    }
}
