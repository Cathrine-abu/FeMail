package com.example.femail;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.femail.Mails.MailAdapter;
import com.example.femail.Mails.MailItem;
import com.example.femail.Mails.MailViewModel;
import com.example.femail.AuthPrefs;
import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private MailViewModel mailViewModel;
    private MailAdapter mailAdapter;
    private RecyclerView recyclerView;
    private EditText searchInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        searchInput = view.findViewById(R.id.edit_text_search);
        recyclerView = view.findViewById(R.id.recycler_view_search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mailAdapter = new MailAdapter(getContext(), new ArrayList<>(), "search");
        recyclerView.setAdapter(mailAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mailViewModel = new ViewModelProvider(requireActivity()).get(MailViewModel.class);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    mailViewModel.searchMails(query, AuthPrefs.getUserId(requireContext()))
                        .observe(getViewLifecycleOwner(), mails -> {
                            mailAdapter.setMailList(mails);
                        });
                } else {
                    mailAdapter.setMailList(new ArrayList<>());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
} 