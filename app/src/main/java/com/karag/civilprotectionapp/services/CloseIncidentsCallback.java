package com.karag.civilprotectionapp.services;

import com.karag.civilprotectionapp.models.MyIncident;

import java.util.List;

public interface CloseIncidentsCallback {
    void onCloseIncidentsFound(List<MyIncident> closeIncidents);
}
